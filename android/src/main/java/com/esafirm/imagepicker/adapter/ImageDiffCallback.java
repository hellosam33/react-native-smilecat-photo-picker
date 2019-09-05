package com.esafirm.imagepicker.adapter;

import androidx.recyclerview.widget.DiffUtil;

import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.model.ImageGroup;

import java.util.List;


public class ImageDiffCallback extends DiffUtil.Callback {

    private final List<Image> images;
    private final List<Image> newImages;

    public ImageDiffCallback(List<Image> images, List<Image> newImages) {
        this.images = images;
        this.newImages = newImages;
    }

    @Override
    public int getOldListSize() {
        return images.size();
    }

    @Override
    public int getNewListSize() {
        return newImages.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return images.get(oldItemPosition).getId() == newImages.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return images.get(oldItemPosition).equals(newImages.get(newItemPosition));
    }
}
