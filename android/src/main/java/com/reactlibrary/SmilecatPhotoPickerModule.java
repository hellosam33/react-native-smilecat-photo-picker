package com.reactlibrary;

import com.esafirm.imagepicker.features.ImagePicker;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.esafirm.imagepicker.bridge.PhotoPickerEventListener;


public class SmilecatPhotoPickerModule extends ReactContextBaseJavaModule {

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
                .showCamera(false)
                .limit(300)
                .start();

//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");
//
//        Intent pickIntent = new Intent(Intent.ACTION_PICK);
//        pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
//
//        reactContext.startActivityForResult(chooserIntent, PHOTO_PICKER_REQUEST, null);

//        final Intent intent = new Intent(reactContext, PhotoPickerActivity.class);
//        if (intent.resolveActivity(reactContext.getPackageManager()) != null) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            reactContext.startActivity(intent);
//        }
//        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }
}


