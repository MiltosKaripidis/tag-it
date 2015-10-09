package com.example.karhades_pc.picture_utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Karhades on 04-10-15.
 */
public class BitmapLoaderTask extends AsyncTask<Object, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private String filePath;
    private long timeStart;

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
        timeStart = System.currentTimeMillis();

        filePath = (String) params[0];
        int width = (int) params[1];
        int height = (int) params[2];

        return PictureUtils.decodeSampledBitmapFromResource(filePath, width, height);
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

                long timeEnd = System.currentTimeMillis();
                long timeDelta = timeEnd - timeStart;
                double seconds = timeDelta / 1000.0;
                //Log.d("BitmapLoaderTask", "AsyncTask completed in: " + seconds);
            }
        }
    }
}
