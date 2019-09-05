package com.esafirm.imagepicker.adapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.esafirm.imagepicker.helper.imagegroup.ImageGroupLevel;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.model.ImageGroup;

import java.util.ArrayList;
import java.util.List;


public class ImageGroupDiffCallback extends DiffUtil.Callback {

    private final List<ImageGroup> oldImageGroups;
    private final List<ImageGroup> newImageGroups;

    public ImageGroupDiffCallback(List<ImageGroup> oldImageGroups, List<ImageGroup> newImageGroups) {
        this.oldImageGroups = oldImageGroups;
        this.newImageGroups = newImageGroups;
    }

    @Override
    public int getOldListSize() {
        return oldImageGroups.size();
    }

    @Override
    public int getNewListSize() {
        return newImageGroups.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

        final boolean b = oldImageGroups.get(oldItemPosition).getPosition() == newImageGroups.get(newItemPosition).getPosition();

        Log.d("test", "oldItemPosition : " + oldItemPosition + " newItemPosition : " + newItemPosition);
        Log.d("test", "areItemsTheSame : " + b);
        return b;
//        return oldImageGroups.get(oldItemPosition).getId().equals(newImageGroups.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final int oldSize = oldImageGroups.get(oldItemPosition).getImages().size();
        final int newSize = newImageGroups.get(newItemPosition).getImages().size();
        final boolean b = oldSize == newSize;
        Log.d("test", "areContentsTheSame : " + b);
        Log.d("test",
                "old position : " + oldItemPosition + " id : " + oldImageGroups.get(oldItemPosition).getId() + " size : " + oldSize +
                " | new position : " + newItemPosition + " id : " + newImageGroups.get(newItemPosition).getId()  + " size : " + newSize);
        return false;
    }


    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {

        final ImageGroup oldImageGroup = oldImageGroups.get(oldItemPosition);
        final ImageGroup newImageGroup = newImageGroups.get(newItemPosition);

        Log.d("test", "getChangePayload .... oldItemPosition : " + oldItemPosition + " newItemPosition : " + newItemPosition);
        Log.d("test", "getChangePayload .... oldImageGroups : " + oldImageGroups.size() + " newImageGroups : " + newImageGroups.size());

        final ImageGroupLevel newGroupLevel = newImageGroup.getImageGroupLevel();
        final Bundle diff = new Bundle();

        if (!oldImageGroup.getId().equals(newImageGroup.getId())) {
            diff.putString("id", newImageGroup.getId());
            diff.putInt("columnSize", newGroupLevel.getColumnSize());
            final ArrayList<Image> images = new ArrayList<>(newImageGroup.getImages());
            diff.putParcelableArrayList("images", images);
        }

        if (diff.size() == 0) {
            return null;
        }
        Log.d("test", "getChangePayload diff found. return diff.");
        return diff;
    }
}
