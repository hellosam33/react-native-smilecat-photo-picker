package com.esafirm.imagepicker.features.common;

public enum Constants {
    IMAGE_PICKER_MAIN_GUIDE("image_picker_main_guide");

    private final String key;

    Constants(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
