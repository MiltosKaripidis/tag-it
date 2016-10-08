/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.utils;

import android.content.Context;
import android.widget.ImageView;

import com.karhades.tag_it.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Helper class for loading images.
 */
public class PictureLoader {

    public static void invalidateBitmap(Context context, String filePath) {
        if (filePath != null) {
            Picasso.with(context).invalidate(new File(filePath));
        }
    }

    public static void loadBitmap(Context context, String filePath, ImageView imageView) {
        if (filePath != null) {
            Picasso.with(context).load(new File(filePath)).fit().centerInside().into(imageView);
        } else {
            Picasso.with(context).load(R.drawable.image_no_image).fit().centerCrop().into(imageView);
        }
    }

    public static void loadBitmap(Context context, String filePath, ImageView imageView, Callback callback) {
        if (filePath != null) {
            Picasso.with(context).load(new File(filePath)).fit().centerInside().into(imageView, callback);
        } else {
            Picasso.with(context).load(R.drawable.image_no_image).fit().centerCrop().into(imageView);
        }
    }

    public static void loadBitmapWithNoCache(Context context, String filePath, ImageView imageView) {
        Picasso.with(context).load(new File(filePath))
                .fit()
                .centerInside()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView);
    }
}
