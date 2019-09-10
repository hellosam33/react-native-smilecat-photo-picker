package com.esafirm.imagepicker.features;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.camera.CameraHelper;
import com.esafirm.imagepicker.features.camera.DefaultCameraModule;
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig;
import com.esafirm.imagepicker.features.common.BaseConfig;
import com.esafirm.imagepicker.helper.imagegroup.ImageGroupLevel;
import com.esafirm.imagepicker.helper.imagegroup.ImageGroupSeparator;
import com.esafirm.imagepicker.listeners.gesture.ImagePickerPinchListener;
import com.esafirm.imagepicker.features.recyclers.RecyclerViewManager;
import com.esafirm.imagepicker.helper.ConfigUtils;
import com.esafirm.imagepicker.helper.ImagePickerPreferences;
import com.esafirm.imagepicker.helper.IpCrasher;
import com.esafirm.imagepicker.helper.IpLogger;
import com.esafirm.imagepicker.listeners.OnFolderClickListener;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.listeners.OnImageSelectedListener;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.model.ImageGroup;
import com.esafirm.imagepicker.view.SnackBarView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.esafirm.imagepicker.helper.ImagePickerPreferences.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;

public class ImagePickerFragment extends Fragment implements ImagePickerView {
    private static final String STATE_KEY_CAMERA_MODULE = "Key.CameraModule";
    private static final String STATE_KEY_RECYCLER = "Key.Recycler";
    private static final String STATE_KEY_SELECTED_IMAGES = "Key.SelectedImages";

    private static final int RC_CAPTURE = 2000;

    private static final int RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 23;
    private static final int RC_PERMISSION_REQUEST_CAMERA = 24;

    private IpLogger logger = IpLogger.getInstance();

    private RecyclerView recyclerView;
    private SnackBarView snackBarView;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private RecyclerViewManager recyclerViewManager;

    private ImagePickerPresenter presenter;
    private ImagePickerPreferences preferences;
    private ImagePickerConfig config;
    private ImagePickerInteractionListener interactionListener;

    private Handler handler;
    private ContentObserver observer;

    private List<Image> currentFolderImages = new ArrayList<>();
    private boolean isFolderMode = false;
    private boolean folderMode;

    private ScaleGestureDetector scaleGestureDetector;
    private Bundle outState;
    private Parcelable recyclerViewState;

    public ImagePickerFragment() {
        // Required empty public constructor.
    }

    static ImagePickerFragment newInstance(@Nullable ImagePickerConfig config,
                                           @Nullable CameraOnlyConfig cameraOnlyConfig) {
        ImagePickerFragment fragment = new ImagePickerFragment();
        Bundle args = new Bundle();
        if (config != null) {
            args.putParcelable(ImagePickerConfig.class.getSimpleName(), config);
        }
        if (cameraOnlyConfig != null) {
            args.putParcelable(CameraOnlyConfig.class.getSimpleName(), cameraOnlyConfig);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupComponents();

        if (interactionListener == null) {
            throw new RuntimeException("ImagePickerFragment needs an " +
                    "ImagePickerInteractionListener. This will be set automatically if the " +
                    "activity implements ImagePickerInteractionListener, and can be set manually " +
                    "with fragment.setInteractionListener(listener).");
        }

        if (savedInstanceState != null) {
            presenter.setCameraModule((DefaultCameraModule) savedInstanceState.getSerializable(STATE_KEY_CAMERA_MODULE));
        }

        ImagePickerConfig config = getImagePickerConfig();
        if (config == null) {
            IpCrasher.openIssue();
        }
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(new ContextThemeWrapper(getActivity(), config.getTheme()));

        // inflate the layout using the cloned inflater, not default inflater
        View result = localInflater.inflate(R.layout.ef_fragment_image_picker, container, false);
        setupView(result);

        if (savedInstanceState == null) {
            setupRecyclerView(config, config.getSelectedImages());
        } else {
            setupRecyclerView(config, savedInstanceState.getParcelableArrayList(STATE_KEY_SELECTED_IMAGES));
            recyclerViewManager.onRestoreState(savedInstanceState.getParcelable(STATE_KEY_RECYCLER));
        }
        interactionListener.selectionChanged(recyclerViewManager.getSelectedImages());
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startContentObserver();
    }

    private BaseConfig getBaseConfig() {
        return getImagePickerConfig();
    }

    @Nullable
    private ImagePickerConfig getImagePickerConfig() {
        if (config == null) {
            Bundle bundle = getArguments();
            if (bundle == null) {
                IpCrasher.openIssue();
            }
            boolean hasImagePickerConfig = bundle.containsKey(ImagePickerConfig.class.getSimpleName());
            boolean hasCameraOnlyConfig = bundle.containsKey(ImagePickerConfig.class.getSimpleName());

            if (!hasCameraOnlyConfig && !hasImagePickerConfig) {
                IpCrasher.openIssue();
            }
            config = bundle.getParcelable(ImagePickerConfig.class.getSimpleName());
        }
        return config;
    }

    private CameraOnlyConfig getCameraOnlyConfig() {
        return getArguments().getParcelable(CameraOnlyConfig.class.getSimpleName());
    }

    private void setupView(View rootView) {
        progressBar = rootView.findViewById(R.id.progress_bar);
        emptyTextView = rootView.findViewById(R.id.tv_empty_images);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        snackBarView = rootView.findViewById(R.id.ef_snackbar);
    }

    private void setupRecyclerView(ImagePickerConfig config, ArrayList<Image> selectedImages) {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerViewManager = new RecyclerViewManager(
                recyclerView,
                config
        );

        final OnImageSelectedListener onImageSelectedListener = selectedImage -> {
            updateTitle();
            interactionListener.selectionChanged(recyclerViewManager.getSelectedImages());
            if (ConfigUtils.shouldReturn(config, false) && !selectedImage.isEmpty()) {
                onDone();
            }
        };
        final OnImageClickListener onImageClickListener = isSelected -> recyclerViewManager.selectImage(isSelected);
        final OnFolderClickListener onFolderClickListener = bucket -> {
            this.currentFolderImages = bucket.getImages();
            this.folderMode = false;
            interactionListener.setTitle(bucket.getFolderName());
            setImageAdapter(bucket.getImages());
        };

        recyclerViewManager.setupAdapters(
                selectedImages,
                onImageClickListener,
                onFolderClickListener,
                onImageSelectedListener
        );
    }

    private void setupComponents() {
        preferences = new ImagePickerPreferences(getActivity());
        presenter = new ImagePickerPresenter(new ImageFileLoader(getActivity()));
        presenter.attachView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataWithPermission();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_KEY_CAMERA_MODULE, presenter.getCameraModule());
        outState.putParcelable(STATE_KEY_RECYCLER, recyclerViewManager.getRecyclerState());
        outState.putParcelableArrayList(STATE_KEY_SELECTED_IMAGES, (ArrayList<? extends Parcelable>)
                recyclerViewManager.getSelectedImages());

    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    void setImageAdapter(List<Image> images) {
        final List<ImageGroup> imageGroups = new ImageGroupSeparator(images).separate(recyclerViewManager.getImageGroupLevel());
        animateLayout(false);
        initControls(images);
        recyclerViewManager.setImageGroupAdapter(imageGroups);
        updateTitle();
    }

    void setFolderAdapter(List<Folder> folders) {
        animateLayout(true);
        recyclerViewManager.setFolderAdapter(folders);
        updateTitle();
    }

    public List<Image> getCurrentFolderImages() {
        return this.currentFolderImages;
    }

    private void animateLayout(boolean dropDown) {
        final RelativeLayout layout = (RelativeLayout) recyclerView.getParent();

        final int fromYDelta = dropDown ? layout.getHeight() : -(layout.getHeight());
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                fromYDelta,  // fromYDelta
                0);                // toYDelta
        animate.setDuration(200);
        animate.setFillAfter(true);

        layout.startAnimation(animate);
    }

    private void updateTitle() {
        interactionListener.setPhotoCount(recyclerViewManager.getTitle());
    }

    /**
     * On finish selected image
     * Get all selected images then return image to caller activity
     */
    public void onDone() {
        final int limit = config.getLimit();
        if (recyclerViewManager.getSelectedImages().size() > limit) {
            Toast.makeText(getContext(), "한번에 " + limit + " 장이 넘는 이미지는 선택 할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.onDoneSelectImages(getContext(), recyclerViewManager.getSelectedImages());
    }

    /**
     * Config recyclerView when configuration changed
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (recyclerViewManager != null) {
            // recyclerViewManager can be null here if we use cameraOnly mode
            recyclerViewManager.init();
        }
    }

    /**
     * Check permission
     */
    private void getDataWithPermission() {
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            getData();
        } else {
            requestWriteExternalPermission();
        }
    }

    private void getData() {
        presenter.abortLoad();
        ImagePickerConfig config = getImagePickerConfig();
        if (config != null) {
            presenter.loadImages(config);
        }
    }

    /**
     * Request for permission
     * If permission denied or app is first launched, request for permission
     * If permission denied and user choose 'Never Ask Again', show snackbar with an action that navigate to app settings
     */
    private void requestWriteExternalPermission() {
        logger.w("Write External permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            final String permission = PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                requestPermissions(permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                snackBarView.show(R.string.ef_msg_no_write_external_permission, v -> openAppSettings());
            }
        }
    }

    private void requestCameraPermissions() {
        logger.w("Write External permission is not granted. Requesting permission");

        ArrayList<String> permissions = new ArrayList<>(2);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (checkForRationale(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), RC_PERMISSION_REQUEST_CAMERA);
        } else {
            final String permission = ImagePickerPreferences.PREF_CAMERA_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                requestPermissions(permissions.toArray(new String[permissions.size()]), RC_PERMISSION_REQUEST_CAMERA);
            } else {
                snackBarView.show(R.string.ef_msg_no_camera_permission, v -> openAppSettings());
            }
        }
    }

    private boolean checkForRationale(List<String> permissions) {
        for (int i = 0, size = permissions.size(); i < size; i++) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Write External permission granted");
                    getData();
                    return;
                }
                logger.e("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                interactionListener.cancel();
            }
            break;
            case RC_PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Camera permission granted");
                    captureImage();
                    return;
                }
                logger.e("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                interactionListener.cancel();
                break;
            }
            default: {
                logger.d("Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    /**
     * Open app settings screen
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getActivity().getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Check if the captured image is stored successfully
     * Then reload data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAPTURE) {
            if (resultCode == RESULT_OK) {
                presenter.finishCaptureImage(getActivity(), data, getBaseConfig());
            } else if (resultCode == RESULT_CANCELED) {
                presenter.abortCaptureImage();
                interactionListener.cancel();
            }
        }
    }

    /**
     * Request for camera permission
     */
    public void captureImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final boolean isCameraGranted = ActivityCompat
                    .checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            final boolean isWriteGranted = ActivityCompat
                    .checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (isCameraGranted && isWriteGranted) {
                captureImage();
            } else {
                logger.w("Camera permission is not granted. Requesting permission");
                requestCameraPermissions();
            }
        } else {
            captureImage();
        }
    }

    /**
     * Start camera intent
     * Create a temporary file and pass file Uri to camera intent
     */
    private void captureImage() {
        if (!CameraHelper.checkCameraAvailability(getActivity())) {
            return;
        }
        presenter.captureImage(this, getBaseConfig(), RC_CAPTURE);
    }

    private void startContentObserver() {
        if (handler == null) {
            handler = new Handler();
        }
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                getData();
            }
        };

        getActivity().getContentResolver()
                .registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.abortLoad();
            presenter.detachView();
        }

        if (observer != null) {
            getActivity().getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    // Returns true if this Fragment can do anything with a "back" event, such as the containing
    // Activity receiving onBackPressed(). Returns false if the containing Activity should handle
    // it.
    // This Fragment might handle a "back" event by, for example, going back to the list of folders.
    // Or it might have no "back" to go, and return false.


    boolean handleBack() {
        // Handled.
        return recyclerViewManager.handleBack();
    }

    public boolean isShowDoneButton() {
        return recyclerViewManager.isShowDoneButton();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ImagePickerInteractionListener) {
            interactionListener = (ImagePickerInteractionListener) context;
        }
    }

    public void setInteractionListener(ImagePickerInteractionListener listener) {
        interactionListener = listener;
    }

    /* --------------------------------------------------- */
    /* > View Methods */
    /* --------------------------------------------------- */

    @Override
    public void finishPickImages(List<Image> images) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(IpCons.EXTRA_SELECTED_IMAGES, (ArrayList<? extends Parcelable>) images);
        interactionListener.finishPickImages(data);
    }

    @Override
    public void showCapturedImage() {
        getDataWithPermission();
    }

    @Override
    public void showFetchCompleted(List<Image> images, List<Folder> folders) {
        this.currentFolderImages = images;
        ImagePickerConfig config = getImagePickerConfig();
        if (config != null) {
            setAdapters(images, folders);
            initControls(images);
        }
    }

    private void setAdapters(List<Image> images, List<Folder> folders) {
        recyclerViewManager.setFolderAdapter(folders);
        final List<ImageGroup> imageGroups = new ImageGroupSeparator(images).separate(recyclerViewManager.getImageGroupLevel());
        recyclerViewManager.setImageGroupAdapter(imageGroups);
        updateTitle();
    }

    private void initControls(List<Image> images) {
        final ImagePickerPinchListener imagePickerPinchListener = new ImagePickerPinchListener(images, recyclerViewManager);
        this.scaleGestureDetector = new ScaleGestureDetector(getContext(), imagePickerPinchListener);
    }

    @Override
    public void showError(Throwable throwable) {
        String message = "Unknown Error";
        if (throwable != null && throwable instanceof NullPointerException) {
            message = "Images do not exist";
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    @Override
    public void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
    }

    public boolean isFolderMode() {
        return folderMode;
    }

    public void setFolderMode(boolean folderMode) {
        this.folderMode = folderMode;
    }

    public void toggleMode() {
        this.folderMode = !folderMode;
    }

    void onTouchEvent(MotionEvent event) {
        if (scaleGestureDetector != null) {
            scaleGestureDetector.onTouchEvent(event);
        }
    }
}
