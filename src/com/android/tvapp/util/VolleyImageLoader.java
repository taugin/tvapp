package com.android.tvapp.util;

import java.io.File;
import java.net.URLEncoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.tvapp.TVApp;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

public class VolleyImageLoader extends ImageLoader {
    private static VolleyImageLoader sInstance;
    private Context mContext;
    private static BitmapLruCache sCache;
    public static VolleyImageLoader initInstance(Context context) {
        if (sInstance == null) {
            sCache = new BitmapLruCache();
            if (context instanceof TVApp) {
                sInstance = new VolleyImageLoader(context, GlobalRequest.get(
                        context).getRequestQueue(),
                        sCache);
            } else {
                sInstance = new VolleyImageLoader(context,
                        GlobalRequest.get(context).getRequestQueue(), 
                        sCache);
            }
        }
        
        return sInstance;
    }
    
    public static VolleyImageLoader getVolleyImageLoader(Context context) {
        return initInstance(context);
    }
    
    private VolleyImageLoader(Context context, RequestQueue queue, ImageCache imageCache) {
        super(queue, imageCache);
        mContext = context;
    }

    @Override
    public ImageContainer get(String requestUrl, ImageListener listener) {
        if (TextUtils.isEmpty(requestUrl)) {
            return null;
        }
        return super.get(encodeUrl(requestUrl), listener);
    }

    public static ImageListener getImageListener(final ImageView view,
            final int defaultImageResId) {
        return new ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Log.TAG, "error : " + error);
                view.setImageResource(defaultImageResId);
            }

            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    if (view.getTag() != null && view.getTag().equals(response.getRequestUrl())) {
                        view.setImageBitmap(scaledBitmap(response.getBitmap()));
                    }
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }
        };
    }

    private static Bitmap scaledBitmap(Bitmap srcBmp) {
        if (srcBmp != null) {
            int w = srcBmp.getWidth();
            int h = srcBmp.getHeight();
            Log.d(Log.TAG, "w : " + w + " , h : " + h);
            int newW = w;
            int newH = h;
            if (w > 4096) {
                newW = 4096;
            }
            if (h > 4096) {
                newH = 4096;
            }
            Bitmap dstBmp = Bitmap.createScaledBitmap(srcBmp, newW, newH, false);
            if (srcBmp != dstBmp) {
                srcBmp.recycle();
            }
            Log.d(Log.TAG, "srcBmp : " + srcBmp + " , dstBmp : " + dstBmp);
            return dstBmp;
        }
        return null;
    }

    public static String encodeUrl(String url) {
        if (url != null && url.length() > 0) {
            String fileNama = url
                    .substring(url.lastIndexOf(File.separator) + 1);
            url = url.replace(fileNama, "");
            url += URLEncoder.encode(fileNama).replace("+", "%20").trim();
        }
        return url;
    }
}