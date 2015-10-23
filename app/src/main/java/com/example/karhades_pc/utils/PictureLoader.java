package com.example.karhades_pc.utils;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Karhades on 15-10-15.
 */
public class PictureLoader {

    public static void invalidateWithPicasso(Context context, String filePath) {
        Picasso.with(context).invalidate(new File(filePath));
    }

    public static void loadBitmapWithPicasso(Context context, final String filePath, final ImageView imageView) {
        Picasso.with(context).load(new File(filePath)).fit().centerInside().into(imageView);
    }

    public static void loadBitmapWithPicassoNoCache(Context context, final String filePath, final ImageView imageView) {
        Picasso.with(context).load(new File(filePath))
                .fit()
                .centerInside()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView);
    }
}
