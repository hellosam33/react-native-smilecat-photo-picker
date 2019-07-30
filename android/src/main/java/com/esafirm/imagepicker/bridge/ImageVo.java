package com.esafirm.imagepicker.bridge;

import android.net.Uri;

class ImageVo {
    private String path;
    private int width;
    private int height;
    private int orientation;

    public ImageVo(String path, int width, int height, int orientation) {
        this.path = path;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
    }

    public String getPath() {
        return path;
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
