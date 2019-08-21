package com.reactlibrary;

import com.esafirm.imagepicker.features.ImagePicker;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.esafirm.imagepicker.bridge.PhotoPickerEventListener;


public class SmilecatPhotoPickerModule extends ReactContextBaseJavaModule {

    // 한번에 선택할 수 있는 사진 개수
    private static final int SELECT_LIMITATION = 300;

    private final ReactApplicationContext reactContext;

    SmilecatPhotoPickerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "SmilecatPhotoPicker";
    }

    @ReactMethod
    public void open(final String command, final Promise promise) {
        // TODO: Implement some real useful functionality

        final PhotoPickerEventListener photoPickerEventListener = new PhotoPickerEventListener(reactContext, promise);
        this.reactContext.addActivityEventListener(photoPickerEventListener);

        ImagePicker.create(reactContext.getCurrentActivity())
                .includeVideo(false)
                .folderMode(true)
                .showCamera(false)
                .limit(SELECT_LIMITATION)
                .start();
    }
}


