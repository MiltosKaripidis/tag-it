/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.utils;

import android.content.Context;
import android.graphics.Typeface;
import java.util.Hashtable;

/**
 * Helper class for caching fonts.
 */
public class FontCache
{
    private static Hashtable<String, Typeface> fontCache = new Hashtable<>();

    public static Typeface get(String name, Context context)
    {
        Typeface typeface = fontCache.get(name);
        if(typeface == null)
        {
            try
            {
                typeface = Typeface.createFromAsset(context.getAssets(), name);
            }
            catch (Exception e)
            {
                return null;
            }
            fontCache.put(name, typeface);
        }
        return typeface;
    }
}
