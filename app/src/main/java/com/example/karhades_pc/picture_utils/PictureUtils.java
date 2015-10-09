package com.example.karhades_pc.picture_utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Karhades on 03-10-15.
 */
public class PictureUtils {

    /**
     * Get a Bitmap from the file path and scale it
     * down to fit the required width and height of
     * the view.
     *
     * @param filename  The path of the file.
     * @param reqWidth  The width of the destination View.
     * @param reqHeight The height of the destination View.
     * @return The scaled down Bitmap.
     */
    public static Bitmap decodeSampledBitmapFromResource(String filename, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        // Get the dimensions without allocating the bitmap to memory.
        options.inJustDecodeBounds = true;
        // Decode the Bitmap from the specified file path.
        // Returns null, due to inJustDecodeBounds. It is
        // used to get the dimensions of the image.
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set to false, to allocate it to memory.
        options.inJustDecodeBounds = false;
        // Decode the Bitmap from the specified file path and return it.
        Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
        return bitmap;
    }

    /**
     * Calculate the factor to scale down the image.
     *
     * @param options    The options needed for the source width and height.
     * @param destWidth  The width of the destination View.
     * @param destHeight The height of the destination View.
     * @return The calculated inSampleSize.
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int destWidth, int destHeight) {
        // Height and Width of the source image.
        final int srcHeight = options.outHeight;
        final int srcWidth = options.outWidth;

        // Initialize the inSampleSize.
        int inSampleSize = 1;

        if (srcHeight > destHeight || srcWidth > destWidth) {

            final int heightRatio = Math.round((float) srcHeight / (float) destHeight);
            final int widthRatio = Math.round((float) srcWidth / (float) destWidth);

            if (heightRatio >= widthRatio)
                return heightRatio;
            else
                return widthRatio;
        }

        return inSampleSize;
    }

    /**
     * Load a Bitmap to this ImageView from the given file path.
     *
     * @param filePath  The file path where the picture resides.
     * @param imageView The ImageView to load the Bitmap.
     */
    public static void loadBitmap(final String filePath, final ImageView imageView) {
        // Run on a new Thread to let the ImageView calculate it's size on the screen.
        imageView.post(new Runnable() {
            @Override
            public void run() {
                // If there is a previous BitmapLoaderTask associated with the given ImageView.
                if (PictureUtils.cancelPotentialLoad(filePath, imageView)) {
                    final BitmapLoaderTask bitmapLoaderTask = new BitmapLoaderTask(imageView);

                    // Drawable that connects the ImageView with the it's BitmapLoaderTask.
                    // Also a placeholder while the bitmap is loading.
                    final PictureUtils.LoaderDrawable placeHolder = new PictureUtils.LoaderDrawable(bitmapLoaderTask);
                    imageView.setImageDrawable(placeHolder);

                    bitmapLoaderTask.execute(filePath, imageView.getWidth(), imageView.getHeight());
                }
            }
        });
    }

    /**
     * Cancel the previous bitmap loading if bitmapFilePath is not yet set or
     * it differs from the new filePath. On contrast, continue the loading.
     *
     * @param filePath  The path to compare if it is the previous BitmapLoaderTask.
     * @param imageView The ImageView to get the associated BitmapLoaderTask.
     * @return Return true if no task was associated with the ImageView,
     * or an existing task was cancelled. Return false if bitmapFilePath is not yet
     * set or it differs from the new filePath.
     */
    public static boolean cancelPotentialLoad(String filePath, ImageView imageView) {
        final BitmapLoaderTask bitmapLoaderTask = getBitmapLoaderTask(imageView);

        // If the is no associated BitmapLoaderTask for this ImageView.
        if (bitmapLoaderTask != null) {
            final String bitmapFilePath = bitmapLoaderTask.getFilePath();

            // If bitmapFilePath is not yet set or it differs from the new filePath.
            if (bitmapFilePath == null || !bitmapFilePath.equals(filePath)) {
                // Cancel previous task.
                bitmapLoaderTask.cancel(true);
            } else {
                // The same loading is already in progress.
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled.
        return true;
    }

    /**
     * Get the BitmapLoaderTask associated with the given ImageView.
     *
     * @param imageView The ImageView to search for it's BitmapLoaderTask.
     * @return The BitmapLoaderTask that is bound to this ImageView.
     */
    public static BitmapLoaderTask getBitmapLoaderTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof LoaderDrawable) {
                final LoaderDrawable loaderDrawable = (LoaderDrawable) drawable;
                return loaderDrawable.getBitmapLoaderTask();
            }
        }
        return null;
    }

    /**
     * Hold a BitmapLoaderTask reference and bind it to the ImageView.
     * Also works as a placeholder, while the BitmapLoaderTask is loading
     * the Bitmap.
     */
    public static class LoaderDrawable extends ColorDrawable {

        private final WeakReference<BitmapLoaderTask> bitmapLoaderTaskWeakReference;

        public LoaderDrawable(BitmapLoaderTask bitmapLoaderTask) {
            super(Color.GRAY);
            bitmapLoaderTaskWeakReference = new WeakReference<>(bitmapLoaderTask);
        }

        public BitmapLoaderTask getBitmapLoaderTask() {
            return bitmapLoaderTaskWeakReference.get();
        }
    }
}
