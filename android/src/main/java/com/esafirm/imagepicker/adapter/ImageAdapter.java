package com.esafirm.imagepicker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.listeners.OnImageSelectedListener;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;


public class ImageAdapter extends BaseListAdapter<ImageAdapter.ImageViewHolder> {

    private List<Image> images = new ArrayList<>();
    private List<Image> selectedImages;
    private final OnImageClickListener itemClickListener;
    private OnImageSelectedListener imageSelectedListener;


    public ImageAdapter(Context context, ImageLoader imageLoader, List<Image> selectedImages, OnImageClickListener itemClickListener) {
        super(context, imageLoader);
        this.selectedImages = selectedImages;
        this.itemClickListener = itemClickListener;
    }

    public void setData(final List<Image> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View imageView = getInflater().inflate(R.layout.ef_imagepicker_item_image, parent, false);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder viewHolder, int position) {
        final Image image = images.get(position);
        final boolean isSelected = isSelected(image);

        getImageLoader().loadImage(
                image.getImageUri(),
                viewHolder.imageView,
                ImageType.GALLERY
        );

        viewHolder.fileTypeIndicator.setVisibility(View.GONE);
        viewHolder.alphaView.setAlpha(isSelected ? 0.5f : 0f);

        viewHolder.itemView.setOnClickListener(v -> {
            boolean shouldSelect = itemClickListener.onImageClick(
                    isSelected
            );

            if (isSelected) {
                removeSelectedImage(image, position);
            } else if (shouldSelect) {
                addSelected(image, position);
            }
        });


        final Drawable selectIcon = ContextCompat.getDrawable(getContext(), isSelected ? R.drawable.photo_checked : R.drawable.photo_unchecked);
        viewHolder.imageSelectIcon.setImageDrawable(selectIcon);
    }

    private boolean isSelected(final Image image) {
        for (final Image selectedImage : selectedImages) {
            if (selectedImage.getId() == image.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return this.images.size();
    }

    public void setImageSelectedListener(OnImageSelectedListener imageSelectedListener) {
        this.imageSelectedListener = imageSelectedListener;
    }


    private void addSelected(final Image image, final int position) {
        mutateSelection(() -> {
            selectedImages.add(image);
            notifyItemChanged(position);
        });
    }

    private void removeSelectedImage(final Image image, final int position) {
        mutateSelection(() -> {
            selectedImages.remove(image);
            notifyItemChanged(position);
        });
    }

    private void mutateSelection(Runnable runnable) {
        runnable.run();
        if (imageSelectedListener != null) {
            imageSelectedListener.onSelectionUpdate(getSelectedItems());
        }
    }


    private List<Image> getSelectedItems() {
        final List<Image> selectedItems = new ArrayList<>();

        for (final Image image : images) {
            if (isSelected(image)) {
                selectedItems.add(image);
            }
        }
        return selectedItems;
    }

    public void updateImages(List<Image> newImages) {
        final ImageDiffCallback imageDiffCallback = new ImageDiffCallback(this.images, newImages);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(imageDiffCallback);
        this.images.clear();
        this.images.addAll(newImages);
        diffResult.dispatchUpdatesTo(this);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageView imageSelectIcon;
        private final View alphaView;
        private final TextView fileTypeIndicator;
        private FrameLayout container;

        ImageViewHolder(View itemView) {
            super(itemView);

            container = (FrameLayout) itemView;
            imageView = itemView.findViewById(R.id.image_view);
            imageSelectIcon = itemView.findViewById(R.id.select_icon);
            alphaView = itemView.findViewById(R.id.view_alpha);
            fileTypeIndicator = itemView.findViewById(R.id.ef_item_file_type_indicator);
        }
    }
}
