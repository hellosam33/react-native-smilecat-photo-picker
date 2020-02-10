package com.esafirm.imagepicker.bridge;

import android.app.Activity;
import android.content.Intent;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;

import java.util.List;

import javax.annotation.Nullable;

public class PhotoPickerEventListener extends BaseActivityEventListener {

    public static final int PHOTO_PICKER_REQUEST = 1;
    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
    private static final String E_PICKER_CANCELLED = "E_PICKER_CANCELLED";
    private static final String E_FAILED_TO_SHOW_PICKER = "E_FAILED_TO_SHOW_PICKER";
    private static final String E_NO_IMAGE_DATA_FOUND = "E_NO_IMAGE_DATA_FOUND";

    private final ReactApplicationContext reactContext;
    // listener 에서 사진선택기의 activity 결과를 promise 형태로 RN 에 응답한다.
    private final @Nullable Promise photoPickerPromise;

    public PhotoPickerEventListener(ReactApplicationContext reactContext, Promise promise) {
        this.reactContext = reactContext;
        this.photoPickerPromise = promise;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

        if (photoPickerPromise != null) {
            if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
                final List<Image> images = ImagePicker.getImages(data);

                final MediaStoreImageAsyncTask mediaStoreImageAsyncTask = new MediaStoreImageAsyncTask(photoPickerPromise);
                mediaStoreImageAsyncTask.execute(images);
                return;
            }

            this.photoPickerPromise.reject(E_PICKER_CANCELLED, "photo picker canceled.");
        }

        super.onActivityResult(activity, requestCode, resultCode, data);

    }
}
