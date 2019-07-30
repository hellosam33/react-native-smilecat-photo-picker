package com.esafirm.imagepicker.bridge;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
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

    //    private final ReactApplicationContext reactContext;
    private final WeakReference<ReactApplicationContext> reactContextRef;
    private Promise photoPickerPromise;

    MediaStoreImageAsyncTask(ReactApplicationContext reactContext, Promise photoPickerPromise) {
        reactContextRef = new WeakReference<>(reactContext);
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
//            final ImageContainer imageContainer = MediaStoreImageAsyncTask.fetchImage(reactContextRef.get().getContentResolver(), imageUri);
            final ImageContainer imageContainer = MediaStoreImageAsyncTask.getImageContainer(reactContextRef.get().getContentResolver(), image);

            if (imageContainer == null) {
                Log.d("test", "fail to make thumbnail data.....");
                continue;
            }

            final ImageVo original = imageContainer.getImage();
            final ImageVo thumbnail = imageContainer.getThumbnail();
            final WritableMap map = Arguments.createMap();

            map.putString("imageUri", original.getPath());
            map.putInt("width", original.getWidth());
            map.putInt("height", original.getHeight());
            map.putString("thumbnailUri", thumbnail.getPath());
            map.putInt("thumbnailWidth", thumbnail.getWidth());
            map.putInt("thumbnailHeight", thumbnail.getHeight());
            map.putInt("orientation", -1); // ios only
            array.pushMap(map);

            Log.d("test", "YAY!! original fetch success!");
        }

        return array;
    }

    private static ImageContainer getImageContainer(ContentResolver contentResolver, final Image image) {
        final ImageVo imageVo = getImageVo(image.getPath());
        final ImageVo thumbnailImageVo;
        try {
            thumbnailImageVo = getThumbnailVo(contentResolver, image);
        } catch (MediaStoreImageException e) {
            Log.d("test", "cannot fetch thumbnail. uri : " + image.getPath() + " error " + e.getMessage());
            return null;
        }
        return new ImageContainer(imageVo, thumbnailImageVo);
    }

    private static ImageVo getImageVo(final String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        final int width = options.outWidth;
        final int height = options.outHeight;
        return new ImageVo(imagePath, width, height, 0);

    }

    private static ImageVo getThumbnailVo(final ContentResolver contentResolver, final Image image) throws MediaStoreImageException {

        final File file = new File(image.getPath());

        if (!file.exists()) {
            throw new MediaStoreImageException("file not exists. image id : " + image.getPath());
        }

        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(contentResolver, image.getId(), MediaStore.Images.Thumbnails.MINI_KIND, null);

        if (cursor == null) {
            throw new MediaStoreImageException("cannot query thumbnail. image id : " + image.getPath());
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            Log.d("test", "thumbnail for image : " + image.getPath() + " not exists. create new one.");
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, image.getId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
            cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(contentResolver, image.getId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
        }


        if (cursor.moveToFirst()) {
            final int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
            final String thumbnailPath = cursor.getString(index);
            cursor.close();
            return getImageVo(thumbnailPath);
        }

        cursor.close();
        throw new MediaStoreImageException("cannot find thumbnail. image id : " + image.getPath());
    }
}
