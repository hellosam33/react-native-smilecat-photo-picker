package com.esafirm.imagepicker.features.recyclers;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.adapter.FolderPickerAdapter;
import com.esafirm.imagepicker.adapter.ImageGroupAdapter;
import com.esafirm.imagepicker.features.ImagePickerConfig;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.helper.ConfigUtils;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.helper.imagegroup.ImageGroupLevel;
import com.esafirm.imagepicker.listeners.OnFolderClickListener;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.listeners.OnImageSelectedListener;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.model.ImageGroup;

import java.util.ArrayList;
import java.util.List;

import static com.esafirm.imagepicker.features.IpCons.MODE_SINGLE;

public class RecyclerViewManager {

    private final Context context;
    private Parcelable state;
    private final RecyclerView recyclerView;
    private final ImagePickerConfig config;

    private LinearLayoutManager layoutManager;

    private ImageGroupAdapter imageGroupAdapter;
    private FolderPickerAdapter folderAdapter;

    private Parcelable foldersState;

    private final ImageGroupDividerItemDecoration imageGroupDividerItemDecoration;
    private boolean isScrollEnabled = true;
    private ImageGroupLevel imageGroupLevel = ImageGroupLevel.DAY;

    public RecyclerViewManager(RecyclerView recyclerView, ImagePickerConfig config) {
        this.recyclerView = recyclerView;
        this.config = config;
        this.context = recyclerView.getContext();
        imageGroupDividerItemDecoration = new ImageGroupDividerItemDecoration(context, R.drawable.image_group_divider);
        init();
    }

    public void onRestoreState(Parcelable recyclerState) {
        layoutManager.onRestoreInstanceState(recyclerState);
    }

    public Parcelable getRecyclerState() {
        return layoutManager.onSaveInstanceState();
    }

    public void init() {
        // prevent auto scroll to recycler view..
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public Parcelable onSaveInstanceState() {
                return super.onSaveInstanceState();
            }



            @Override
            public void onRestoreInstanceState(Parcelable state) {
                super.onRestoreInstanceState(state);

            }

            @Override
            public boolean canScrollVertically() {
                return isScrollEnabled && super.canScrollVertically();
            }

            @Override
            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child, @NonNull Rect rect, boolean immediate) {
                return false;
            }
            @Override
            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child, @NonNull Rect rect, boolean immediate, boolean focusedChildVisible) {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);
    }

    public LinearLayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void setupAdapters(ArrayList<Image> selectedImages, OnImageClickListener onImageClickListener, OnFolderClickListener onFolderClickListener, OnImageSelectedListener onImageSelectedListener) {
        if (config.getMode() == MODE_SINGLE && selectedImages != null && selectedImages.size() > 1) {
            selectedImages = null;
        }
        /* Init folder and image adapter */
        final ImageLoader imageLoader = config.getImageLoader();
        imageGroupAdapter = new ImageGroupAdapter(context, imageLoader, selectedImages, onImageClickListener, onImageSelectedListener);


        final OnFolderClickListener folderClickListener = bucket -> {
            foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
            onFolderClickListener.onFolderClick(bucket);
        };

        folderAdapter = new FolderPickerAdapter(context, imageLoader, folderClickListener);
    }

    // Returns true if a back action was handled by going back a folder; false otherwise.
    public boolean handleBack() {
        if (!isDisplayingFolderView()) {
            setFolderAdapter(null);
            return true;
        }
        return false;
    }

    private boolean isDisplayingFolderView() {
        return recyclerView.getAdapter() == null || recyclerView.getAdapter() instanceof FolderPickerAdapter;
    }

    public String getTitle() {
        if (config.getMode() == MODE_SINGLE) {
            return ConfigUtils.getImageTitle(context, config);
        }

        final int imageSize = imageGroupAdapter.getSelectedImages().size();

        final boolean useDefaultTitle = !ImagePickerUtils.isStringEmpty(config.getImageTitle()) && imageSize == 0;

        if (useDefaultTitle) {
            return ConfigUtils.getImageTitle(context, config);
        }

        if (imageSize <= 0) {
            return "";
        }

        return String.format(context.getString(R.string.ef_selected), imageSize);
    }

    public void setImageGroupAdapter(List<ImageGroup> imageGroups) {
        imageGroupAdapter.setData(imageGroups);
        recyclerView.removeItemDecoration(imageGroupDividerItemDecoration);
        recyclerView.setAdapter(imageGroupAdapter);
    }

    public void setImageAdapterData(List<ImageGroup> imageGroups, ImageGroupLevel imageGroupLevel) {
        imageGroupAdapter.setGroupLevel(imageGroupLevel);
        imageGroupAdapter.updateGroups(imageGroups);
        recyclerView.removeItemDecoration(imageGroupDividerItemDecoration);
    }

    public void setFolderAdapter(List<Folder> folders) {
        folderAdapter.setData(folders);
        recyclerView.addItemDecoration(imageGroupDividerItemDecoration);
        recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
    }

    public void setImageGroupLevel(ImageGroupLevel imageGroupLevel) {
        this.imageGroupLevel = imageGroupLevel;
    }

    public ImageGroupLevel getImageGroupLevel() {
        return imageGroupLevel;
    }
    /* --------------------------------------------------- */
    /* > Images */
    /* --------------------------------------------------- */

    private void checkAdapterIsInitialized() {
        if (imageGroupAdapter == null) {
            throw new IllegalStateException("Must call setupAdapters first!");
        }
    }

    public List<Image> getSelectedImages() {
        checkAdapterIsInitialized();
        return imageGroupAdapter.getSelectedImages();
    }

    public boolean selectImage(boolean isSelected) {
        if (imageGroupAdapter.getSelectedImages().size() >= config.getLimit() && !isSelected) {
            Toast.makeText(context, R.string.ef_msg_limit_images, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean isShowDoneButton() {
        return !isDisplayingFolderView()
                && !imageGroupAdapter.getSelectedImages().isEmpty()
                && (config.getReturnMode() != ReturnMode.ALL && config.getReturnMode() != ReturnMode.GALLERY_ONLY);
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        isScrollEnabled = scrollEnabled;
    }
}
