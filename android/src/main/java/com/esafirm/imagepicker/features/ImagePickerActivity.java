package com.esafirm.imagepicker.features;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig;
import com.esafirm.imagepicker.features.common.Constants;
import com.esafirm.imagepicker.helper.ConfigUtils;
import com.esafirm.imagepicker.helper.IpLogger;
import com.esafirm.imagepicker.helper.LocaleManager;
import com.esafirm.imagepicker.helper.ViewUtils;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;

import java.util.List;

public class ImagePickerActivity extends AppCompatActivity implements ImagePickerInteractionListener, ImagePickerView {

    private ActionBar actionBar;
    private ImagePickerFragment imagePickerFragment;

    private ImagePickerConfig config;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.updateResources(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        /* This should not happen */
        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            IpLogger.getInstance().e("This should not happen. Please open an issue!");
            finish();
            return;
        }
        config = getIntent().getExtras().getParcelable(ImagePickerConfig.class.getSimpleName());
        CameraOnlyConfig cameraOnlyConfig = getIntent().getExtras().getParcelable(CameraOnlyConfig.class.getSimpleName());

        boolean isCameraOnly = cameraOnlyConfig != null;

        // TODO extract camera only function so we don't have to rely to Fragment
        if (!isCameraOnly) {
            setTheme(config.getTheme());
            setContentView(R.layout.ef_activity_image_picker);
            setupView();
        } else {
            setContentView(createCameraLayout());
        }

        // select album
        final TextView selectButton = findViewById(R.id.toolbar_select_album);
        selectButton.setOnClickListener(e -> {
            imagePickerFragment.toggleMode();
            final boolean isFolderMode = imagePickerFragment.isFolderMode();

            if (isFolderMode) {
                imagePickerFragment.setFolderAdapter(null);
            } else {
                final List<Image> currentFolderImages = imagePickerFragment.getCurrentFolderImages();
                if (!currentFolderImages.isEmpty()) {
                    imagePickerFragment.setImageAdapter(currentFolderImages);
                }
            }

            final Drawable dropDownImage = getResources().getDrawable(isFolderMode ? R.drawable.dropdown_up : R.drawable.dropdown_down);
            selectButton.setCompoundDrawablesWithIntrinsicBounds(null, null, dropDownImage, null);
        });

        if (savedInstanceState != null) {
            // The fragment has been restored.
            imagePickerFragment = (ImagePickerFragment) getSupportFragmentManager().findFragmentById(R.id.ef_imagepicker_fragment_placeholder);
        } else {
            imagePickerFragment = ImagePickerFragment.newInstance(config, cameraOnlyConfig);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.ef_imagepicker_fragment_placeholder, imagePickerFragment);
            ft.commit();
        }

        showImagePickerTutorial();
    }

    private void showImagePickerTutorial() {
        final View tutorialView = findViewById(R.id.tutorialView);

        final int tutorialCloseCount = PreferenceManager.getDefaultSharedPreferences(ImagePickerActivity.this).getInt(Constants.IMAGE_PICKER_MAIN_GUIDE.getKey(), 0);
        final boolean tutorialShown = tutorialCloseCount >= 2;

        if (!tutorialShown) {
            final ImageView guideGif = findViewById(R.id.guide_gif_image);
            Glide.with(tutorialView).asGif().load(R.drawable.guide_photoselect).into(guideGif);
            tutorialView.setVisibility(View.VISIBLE);
            tutorialView.setOnClickListener(v -> {
                v.setVisibility(View.INVISIBLE);
                PreferenceManager.getDefaultSharedPreferences(ImagePickerActivity.this).edit().putInt(Constants.IMAGE_PICKER_MAIN_GUIDE.getKey(), tutorialCloseCount + 1).apply();
            });
        } else {
            tutorialView.setVisibility(View.GONE);
        }

    }

    private FrameLayout createCameraLayout() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(R.id.ef_imagepicker_fragment_placeholder);
        return frameLayout;
    }

    /**
     * Create option menus.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ef_image_picker_menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuCamera = menu.findItem(R.id.menu_camera);
        if (menuCamera != null) {
            if (config != null) {
                menuCamera.setVisible(config.isShowCamera());
            }
        }

        MenuItem menuDone = menu.findItem(R.id.menu_done);
        if (menuDone != null) {
            menuDone.setTitle(ConfigUtils.getDoneButtonText(this, config));
            menuDone.setVisible(true); // always show done button
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handle option menu's click event
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.menu_done) {
            imagePickerFragment.onDone();
            return true;
        }
        if (id == R.id.menu_camera) {
            imagePickerFragment.captureImageWithPermission();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!imagePickerFragment.handleBack()) {
            super.onBackPressed();
        }
    }

    private void setupView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            final Drawable arrowDrawable = ViewUtils.getArrowIcon(this);
            final int arrowColor = config.getArrowColor();
            if (arrowColor != ImagePickerConfig.NO_COLOR && arrowDrawable != null) {
                arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_ATOP);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(arrowDrawable);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    /* --------------------------------------------------- */
    /* > ImagePickerInteractionListener Methods */
    /* --------------------------------------------------- */

    @Override
    public void setTitle(String title) {
        final TextView titleTextView = findViewById(R.id.toolbar_select_album);
        titleTextView.setVisibility(View.VISIBLE);
        if (title != null && !title.isEmpty()) {
            titleTextView.setText(title);
        }
        final Drawable dropDownImage = getResources().getDrawable(R.drawable.dropdown_down);
        titleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, dropDownImage, null);
    }

    @Override
    public void setPhotoCount(String title) {
        supportInvalidateOptionsMenu();
        final TextView photoCount = findViewById(R.id.toolbar_photo_count);
        photoCount.setText(title);
    }

    @Override
    public void cancel() {
        finish();
    }

    @Override
    public void selectionChanged(List<Image> imageList) {
        // Do nothing when the selection changes.
    }

    @Override
    public void finishPickImages(Intent result) {
        setResult(RESULT_OK, result);
        finish();
    }

    /* --------------------------------------------------- */
    /* > View Methods  */
    /* --------------------------------------------------- */

    @Override
    public void showLoading(boolean isLoading) {
        imagePickerFragment.showLoading(isLoading);
    }

    @Override
    public void showFetchCompleted(List<Image> images, List<Folder> folders) {
        imagePickerFragment.showFetchCompleted(images, folders);
    }

    @Override
    public void showError(Throwable throwable) {
        imagePickerFragment.showError(throwable);
    }

    @Override
    public void showEmpty() {
        imagePickerFragment.showEmpty();
    }

    @Override
    public void showCapturedImage() {
        imagePickerFragment.showCapturedImage();
    }

    @Override
    public void finishPickImages(List<Image> images) {
        imagePickerFragment.finishPickImages(images);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        imagePickerFragment.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }


}
