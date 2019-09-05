package com.esafirm.imagepicker.helper.imagegroup;

import android.util.Log;

import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.model.ImageGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ImageGroupSeparator {

    private final List<Image> images;

    public ImageGroupSeparator(List<Image> images) {
        this.images = images;
    }

    public List<ImageGroup> separate(ImageGroupLevel imageGroupLevel) {
        return groupImagesByAddedTime(imageGroupLevel);
    }

    private List<ImageGroup> groupImagesByAddedTime(ImageGroupLevel imageGroupLevel) {
        final Map<Integer, ImageGroup> imageGroupMap = new TreeMap<>(Integer::compareTo);

        Collections.sort(images, (o1, o2) -> Long.compare(o2.getDateAdded(), o1.getDateAdded()));

        int position = 0;
        String prevGroupName = "";
        for (final Image image : images) {
            final String groupName = imageGroupLevel.getDateFormat().format(image.getDateAdded());

            if (!prevGroupName.equals(groupName)) {
                position++;
            }

            prevGroupName = groupName;

            final ImageGroup imageGroup = imageGroupMap.get(position);
            if (imageGroup == null) {
                final ImageGroup newImageGroup = new ImageGroup(position, groupName, imageGroupLevel);
                newImageGroup.addImage(image);
                imageGroupMap.put(position, newImageGroup);
            } else {
                imageGroup.addImage(image);
            }
        }

        // no stream api.. QQ
        final List<ImageGroup> imageGroups = new ArrayList<>();
        for (Map.Entry<Integer, ImageGroup> elem : imageGroupMap.entrySet()) {
            imageGroups.add(elem.getValue());
        }

        return imageGroups;
    }
}
