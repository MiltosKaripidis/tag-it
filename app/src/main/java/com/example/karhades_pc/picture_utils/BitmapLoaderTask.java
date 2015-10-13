package com.example.karhades_pc.picture_utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Karhades on 04-10-15.
 */
public class BitmapLoaderTask extends AsyncTask<Object, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private String filePath;

    public BitmapLoaderTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected.
        imageViewReference = new WeakReference<>(imageView);
    }

    public String getFilePath() {
        return filePath;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Object... params) {
        filePath = (String) params[0];
        int width = (int) params[1];
        int height = (int) params[2];

        // Decode the bitmap.
        Bitmap bitmap = PictureUtils.decodeSampledBitmapFromResource(filePath, width, height);
        Log.d("BitmapLoaderTask", "Bytes of bitmap: " + bitmap.getByteCount() / 1024 + " KB");

        if (params.length > 3) {
            LruMemoryCache lruMemoryCache = (LruMemoryCache) params[3];
            // Add to cache.
            lruMemoryCache.addBitmapToMemoryCache(filePath, bitmap);
            Log.d("BitmapLoaderTask", "Cache Size: " + lruMemoryCache.size());
        }

        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapLoaderTask bitmapLoaderTask = PictureUtils.getBitmapLoaderTask(imageView);

            if (this == bitmapLoaderTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
