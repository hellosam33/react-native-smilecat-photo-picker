package com.esafirm.imagepicker.features.imageloader;

import android.net.Uri;
import android.widget.ImageView;

import java.io.Serializable;

public interface ImageLoader extends Serializable {
    void loadImage(Uri imageUri, ImageView imageView, ImageType imageType);
}
