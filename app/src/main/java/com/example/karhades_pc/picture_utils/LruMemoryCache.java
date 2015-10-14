package com.example.karhades_pc.picture_utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Created by Karhades on 10-10-15.
 */
public class LruMemoryCache {

    private LruCache<String, Bitmap> lruCache;

    public LruMemoryCache() {

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        Log.d("BitmapLoaderTask", "Cache size: " + cacheSize / 1024 + " MB");

        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return value.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap value) {
        if (getBitmapFromMemoryCache(key) == null) {
            lruCache.put(key, value);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return lruCache.get(key);
    }

    public int size() {
        return lruCache.size();
    }

    public void remove(String key) {
        lruCache.remove(key);
    }
}
