package com.esafirm.imagepicker.bridge;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.esafirm.imagepicker.model.Image;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
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

            final File file = new File(image.getPath());
            Log.d("test", "image uri : " + file.getAbsolutePath());

            final ImageVo imageVo = getImageVo(image.getPath());
            final WritableMap map = Arguments.createMap();

            map.putString("imageUri", image.getPath());
            map.putInt("imageId", (int) image.getId());
            map.putInt("width", imageVo.getWidth());
            map.putInt("height", imageVo.getHeight());
            array.pushMap(map);

            Log.d("test", "YAY!! imageVo fetch success!");
        }

        return array;
    }

    private static ImageVo getImageVo(final String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        final int width = options.outWidth;
        final int height = options.outHeight;
        return new ImageVo(imagePath, width, height, 0);
    }
}
