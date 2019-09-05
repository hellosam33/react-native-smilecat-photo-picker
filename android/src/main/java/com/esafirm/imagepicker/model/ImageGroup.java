package com.esafirm.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.esafirm.imagepicker.adapter.ImageAdapter;
import com.esafirm.imagepicker.helper.imagegroup.ImageGroupLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageGroup implements Parcelable {

    private final int position;
    private final String id;
    private boolean selected = false;
    private final List<Image> images;
    private ImageAdapter imageAdapter;
    private ImageGroupLevel imageGroupLevel = ImageGroupLevel.UNKNOWN;

    public ImageGroup(int position, String id, ImageGroupLevel imageGroupLevel) {
        this.position = position;
        this.id = id;
        this.images = new ArrayList<>();
        this.imageGroupLevel = imageGroupLevel;
    }

    public void setImages(List<Image> images) {
        this.images.clear();
        this.images.addAll(images);
    }

    public void addImage(final Image image) {
        this.images.add(image);
    }

    public int getPosition() {
        return position;
    }

    public String getId() {
        return id;
    }

    public ImageGroupLevel getImageGroupLevel() {
        return imageGroupLevel;
    }

    public List<Image> getImages() {
        return images;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.position);
        dest.writeString(this.id);
        dest.writeList(this.images);

    }

    protected ImageGroup(Parcel in) {
        this.position = in.readInt();
        this.id = in.readString();
        this.images = new ArrayList<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageGroup)) return false;
        ImageGroup that = (ImageGroup) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public static final Creator<ImageGroup> CREATOR = new Creator<ImageGroup>() {
        @Override
        public ImageGroup createFromParcel(Parcel source) {
            return new ImageGroup(source);
        }

        @Override
        public ImageGroup[] newArray(int size) {
            return new ImageGroup[size];
        }
    };

    public void setImageAdapter(ImageAdapter imageAdapter) {
        this.imageAdapter = imageAdapter;
    }

    public @Nullable
    ImageAdapter getImageAdapter() {
        return imageAdapter;
    }
}
