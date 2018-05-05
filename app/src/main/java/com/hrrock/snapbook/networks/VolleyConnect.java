package com.hrrock.snapbook.networks;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.hrrock.snapbook.ApplicationContextReference;

/**
 * Created by hp-u on 9/10/2017.
 */

public class VolleyConnect {
    private static VolleyConnect volleyConnect;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private VolleyConnect() {
        requestQueue = Volley.newRequestQueue(ApplicationContextReference.applicationContextReference);
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private LruCache<String, Bitmap> cache = new LruCache<>((int) ((Runtime.getRuntime().maxMemory() / 1024) / 8));

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static VolleyConnect getInstance() {
        if (volleyConnect == null) {
            volleyConnect = new VolleyConnect();
        }
        return volleyConnect;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
