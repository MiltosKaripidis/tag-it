package com.example.karhades_pc.picture_utils;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Karhades on 15-10-15.
 */
public class PictureLoader {

    public static void invalidateWithPicasso(Context context, String filePath) {
        Picasso.with(context).invalidate(new File(filePath));
    }

    public static void loadBitmapWithPicasso(final Context context, final String filePath, final ImageView imageView) {

        // Run on a new Thread to let the ImageView calculate it's size on the screen.
        imageView.post(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(new File(filePath))
                        .resize(imageView.getWidth(), imageView.getHeight())
                        .centerInside()
                        .into(imageView);
            }
        });
    }
}
