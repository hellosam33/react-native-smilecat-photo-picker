package com.esafirm.imagepicker.bridge;

class ImageContainer {
    private ImageVo image;
    private ImageVo thumbnail;

    public ImageContainer(ImageVo image, ImageVo thumbnail) {
        this.image = image;
        this.thumbnail = thumbnail;
    }

    public ImageVo getImage() {
        return image;
    }

    public ImageVo getThumbnail() {
        return thumbnail;
    }
}
