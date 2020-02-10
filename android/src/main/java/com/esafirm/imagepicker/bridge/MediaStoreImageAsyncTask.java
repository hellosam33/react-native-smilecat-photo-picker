package com.esafirm.imagepicker.bridge;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.esafirm.imagepicker.model.Image;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class MediaStoreImageAsyncTask extends AsyncTask<List<Image>, Void, WritableMap> {

    private Promise photoPickerPromise;

    MediaStoreImageAsyncTask(Promise photoPickerPromise) {
        this.photoPickerPromise = photoPickerPromise;
    }


    @SafeVarargs
    @Override
    protected final WritableMap doInBackground(List<Image>... lists) {
        // for generic varargs type safe
        final List<Image> images = new ArrayList<>();

        for (List<Image> imageList : lists) {
            images.addAll(imageList);
        }

        if (images.isEmpty()) {
            return Arguments.createMap();
        }

        return mapPhotoPickerIntentResult(images);
    }

    @Override
    protected void onPostExecute(WritableMap writableMap) {
        this.photoPickerPromise.resolve(writableMap);
    }

    private WritableMap mapPhotoPickerIntentResult(final List<Image> images) {
        final WritableMap result = Arguments.createMap();

        if (images.isEmpty()) {
            Log.d("test", "photo picker returned empty");
            result.putInt("result_code", 101);
            return result;
        }

        result.putInt("result_code", 200);
        result.putArray("photo_list", mapPhotoDataArray(images));

        return result;
    }

    private WritableArray mapPhotoDataArray(final List<Image> images) {
        final WritableArray array = Arguments.createArray();
        for (final Image image : images) {

            final WritableMap map = Arguments.createMap();
            map.putString("imageUri", image.getPath());
            map.putInt("imageId", (int) image.getId());
            map.putInt("width", image.getWidth());
            map.putInt("height", image.getHeight());
            array.pushMap(map);
        }

        return array;
    }
}
