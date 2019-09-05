package com.esafirm.imagepicker.listeners.gesture;

import android.view.ScaleGestureDetector;

import com.esafirm.imagepicker.features.recyclers.RecyclerViewManager;
import com.esafirm.imagepicker.helper.imagegroup.ImageGroupLevel;
import com.esafirm.imagepicker.helper.imagegroup.ImageGroupSeparator;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.model.ImageGroup;

import java.util.List;

public class ImagePickerPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private static final int LEVEL_INCREASE_SCALE_FACTOR = 2;
    private static final float LEVEL_DECREASE_SCALE_FACTOR = 0.4f;

    private final List<Image> images;
    private final RecyclerViewManager recyclerViewManager;
    private int currentImagePickerGroupLevel;

    public ImagePickerPinchListener(List<Image> images, RecyclerViewManager recyclerViewManager) {
        this.images = images;
        this.recyclerViewManager = recyclerViewManager;
        this.currentImagePickerGroupLevel = recyclerViewManager.getImageGroupLevel().getLevel();
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        recyclerViewManager.setScrollEnabled(false);
        return super.onScaleBegin(detector);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        calculateGroupLevel(detector);
        recyclerViewManager.setScrollEnabled(true);
        super.onScaleEnd(detector);
    }

    private void calculateGroupLevel(ScaleGestureDetector detector) {
        final float scaleFactor = detector.getScaleFactor();

        final int previousGroupLevel = currentImagePickerGroupLevel;

        if (ImageGroupLevel.MONTH.getLevel() > currentImagePickerGroupLevel && scaleFactor < LEVEL_DECREASE_SCALE_FACTOR) {
            currentImagePickerGroupLevel += 1;
        } else if (ImageGroupLevel.DAY.getLevel() < currentImagePickerGroupLevel && scaleFactor > LEVEL_INCREASE_SCALE_FACTOR) {
            currentImagePickerGroupLevel -= 1;
        }

        if (previousGroupLevel != currentImagePickerGroupLevel) {
            final ImageGroupSeparator imageGroupSeparator = new ImageGroupSeparator(images);
            final ImageGroupLevel imageGroupLevel = ImageGroupLevel.valueOf(currentImagePickerGroupLevel);
            recyclerViewManager.setImageGroupLevel(imageGroupLevel);

            if (imageGroupLevel != null) {
                final List<ImageGroup> imageGroups = imageGroupSeparator.separate(imageGroupLevel);
                this.recyclerViewManager.setImageAdapterData(imageGroups, imageGroupLevel);
            }
        }
    }
}
