package com.esafirm.imagepicker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.helper.imagegroup.ImageGroupLevel;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.listeners.OnImageSelectedListener;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.model.ImageGroup;
import com.esafirm.imagepicker.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ImageGroupAdapter extends BaseListAdapter<ImageGroupAdapter.ImageGroupViewHolder> {

    private final List<Image> selectedImages;
    private final OnImageClickListener onImageClickListener;
    private OnImageSelectedListener onImageSelectedListener;
    private List<ImageGroup> imageGroups = new ArrayList<>();
    private GridSpacingItemDecoration itemOffsetDecoration;
    private ImageGroupLevel groupLevel = ImageGroupLevel.DAY;

    public ImageGroupAdapter(Context context, ImageLoader imageLoader, List<Image> selectedImages, OnImageClickListener onImageClickListener, OnImageSelectedListener onImageSelectedListener) {
        super(context, imageLoader);
        this.selectedImages = selectedImages;
        this.onImageClickListener = onImageClickListener;
        this.onImageSelectedListener = onImageSelectedListener;
    }

    @NonNull
    @Override
    public ImageGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View imageGroup = getInflater().inflate(R.layout.ef_imagepicker_item_image_group, parent, false);
        return new ImageGroupViewHolder(imageGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageGroupViewHolder holder, int position) {
        final ImageGroup imageGroup = imageGroups.get(position);

        final boolean isGroupSelected = isGroupSelected(imageGroup);
        toggleGroupSelectImage(holder, isGroupSelected);
        holder.textView.setText(imageGroup.getId());

        holder.textView.setOnClickListener(v -> {
            final boolean isSelected = isGroupSelected(imageGroup);
            onImageClickListener.onImageClick(isSelected);

            if (isSelected) {
                removeSelected(imageGroup, position);
            } else {
                addSelected(imageGroup, position);
            }
            imageGroup.setSelected(!isSelected);
        });

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(holder.recyclerView.getContext(), groupLevel.getColumnSize());

        ImageAdapter imageAdapter = new ImageAdapter(getContext(), getImageLoader(), selectedImages, onImageClickListener);
        imageGroup.setImageAdapter(imageAdapter);

        final OnImageSelectedListener onImageSelectedListener = selectedImages -> {
            final boolean shouldSelectGroup = isGroupSelected(imageGroup);
            imageGroup.setSelected(shouldSelectGroup);
            toggleGroupSelectImage(holder, shouldSelectGroup);
            this.onImageSelectedListener.onSelectionUpdate(selectedImages);
        };

        imageAdapter.setImageSelectedListener(onImageSelectedListener);
        imageAdapter.setData(imageGroup.getImages());

        setGridLayoutItemDecoration(holder.recyclerView);

        final RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
        holder.recyclerView.setRecycledViewPool(recycledViewPool);
        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setLayoutManager(gridLayoutManager);
        holder.recyclerView.setNestedScrollingEnabled(false);
        holder.recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageGroupViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            Bundle o = (Bundle) payloads.get(0);
            for (String key : o.keySet()) {
                if (key.equals("id")) {
                    holder.textView.setText(o.getString(key));
                }
                if (key.equals("columnSize")) {
                    final int columnSize = o.getInt(key);
                    final GridLayoutManager gridLayoutManager = new GridLayoutManager(holder.recyclerView.getContext(), columnSize);
                    setGridLayoutItemDecoration(holder.recyclerView);
                    holder.recyclerView.setLayoutManager(gridLayoutManager);
                }
                if (key.equals("images")) {
                    final ArrayList<Image> images = o.getParcelableArrayList("images");
                    final ImageAdapter adapter = (ImageAdapter) holder.recyclerView.getAdapter();
                    if (images != null && !images.isEmpty() && adapter != null) {
                        adapter.updateImages(images);
                    }
                }
            }
        }
    }

    private void toggleGroupSelectImage(@NonNull ImageGroupViewHolder holder, boolean isGroupSelected) {
        final Drawable selectImage = getContext().getResources().getDrawable(isGroupSelected ? R.drawable.group_checked : R.drawable.group_unchecked);
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(selectImage, null, null, null);
    }


    private boolean isGroupSelected(ImageGroup imageGroup) {
        final List<Image> images = imageGroup.getImages();

        for (final Image image : images) {
            if (!selectedImages.contains(image)) {
                return false;
            }
        }
        return true;
    }

    private void addSelected(final ImageGroup imageGroup, final int position) {
        mutateSelection(() -> {
            for (final Image image : imageGroup.getImages()) {
                if (!selectedImages.contains(image)) {
                    selectedImages.add(image);
                }
            }
            notifyItemChanged(position);
        });
    }

    private void removeSelected(final ImageGroup imageGroup, final int position) {
        mutateSelection(() -> {
            for (final Image image : imageGroup.getImages()) {
                selectedImages.remove(image);
            }
            notifyItemChanged(position);
        });
    }

    private void mutateSelection(Runnable runnable) {
        runnable.run();
        if (onImageSelectedListener != null) {
            onImageSelectedListener.onSelectionUpdate(selectedImages);
        }
    }

    @Override
    public int getItemCount() {
        return imageGroups.size();
    }

    private void setGridLayoutItemDecoration(final RecyclerView recyclerView) {
        if (itemOffsetDecoration != null) {
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        }
        final int dimensionPixelSize = getContext().getResources().getDimensionPixelSize(R.dimen.ef_grid_layout_item_padding);
        itemOffsetDecoration = new GridSpacingItemDecoration(dimensionPixelSize);
        final int itemDecorationCount = recyclerView.getItemDecorationCount();

        if (itemDecorationCount == 0) {
            recyclerView.addItemDecoration(itemOffsetDecoration);
        }
    }

    public List<Image> getSelectedImages() {
        return selectedImages;
    }


    public void setData(List<ImageGroup> imageGroups) {
        this.imageGroups = imageGroups;
    }

    public void updateGroups(List<ImageGroup> newImageGroups) {
        final ImageGroupDiffCallback imageGroupDiffCallback = new ImageGroupDiffCallback(this.imageGroups, newImageGroups);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(imageGroupDiffCallback);
        diffResult.dispatchUpdatesTo(this);
        this.imageGroups.clear();
        this.imageGroups.addAll(newImageGroups);
    }

    public void setGroupLevel(ImageGroupLevel groupLevel) {
        this.groupLevel = groupLevel;
    }

    static class ImageGroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final RecyclerView recyclerView;

        ImageGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.image_group_name);
            this.recyclerView = itemView.findViewById(R.id.image_group_recycler_view);
        }
    }
}
