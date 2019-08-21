package com.esafirm.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ImageGroup implements Parcelable {

    private final long id;
    private final String name;
    private boolean selected = false;
    private final List<Image> images = new ArrayList<>();

    public ImageGroup(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addImage(final Image image) {
        this.images.add(image);
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return name;
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
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeList(this.images);

    }

    protected ImageGroup(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
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

    public static class Util {

        private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("M월 dd일 E요일", Locale.KOREA);
        private static final long EXCLUSION = 24 * 60 * 60 * 1000L;

        public static List<ImageGroup> organizeImages(final List<Image> images) {
            // descending order by key
            final Map<Long, ImageGroup> imageGroupMap = new TreeMap<>((o1, o2) -> -(o1.compareTo(o2)));

            for (final Image image : images) {
                final long key = excludeDayInMilliseconds(image.getDateAdded());
                final ImageGroup imageGroup = imageGroupMap.get(key);
                if (imageGroup == null) {
                    final String groupName = SIMPLE_DATE_FORMAT.format(key);
                    final ImageGroup newImageGroup = new ImageGroup(key, groupName);
                    newImageGroup.addImage(image);
                    imageGroupMap.put(key, newImageGroup);
                } else {
                    imageGroup.addImage(image);
                }
            }

            // no stream api.. QQ
            final List<ImageGroup> imageGroups = new ArrayList<>();
            for (Map.Entry<Long, ImageGroup> elem : imageGroupMap.entrySet()) {
                imageGroups.add(elem.getValue());
            }

            return imageGroups;
        }

        private static long excludeDayInMilliseconds(long time) {
            return EXCLUSION * (time / EXCLUSION);
        }

    }
}
