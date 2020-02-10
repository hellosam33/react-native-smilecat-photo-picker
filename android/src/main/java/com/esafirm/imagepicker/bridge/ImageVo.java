package com.esafirm.imagepicker.bridge;

import android.net.Uri;

class ImageVo {
    private Uri imageUri;
    private int width;
    private int height;
    private int orientation;

    public ImageVo(Uri imageUri, int width, int height, int orientation) {
        this.imageUri = imageUri;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getOrientation() {
        return orientation;
    }
}
