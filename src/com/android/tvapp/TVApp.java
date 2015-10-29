package com.android.tvapp;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import android.app.Application;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.tvapp.util.GlobalRequest;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.tvapp.util.VolleyImageLoader;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class TVApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VolleyImageLoader.initInstance(this);
        GlobalRequest.get(this).start();
        AppUncaughtExceptionHandler.getInstance(this).init();
    }

    class UploadExecutor implements  Listener<String>, ErrorListener, Runnable{

        public void execute(String filePath) {
            Log.d(Log.TAG, "");
            final HashMap<String, String> params = new HashMap<String, String>();
            params.put("filename", new File(filePath).getName());
            params.put("errorlog", readErrorLogs(filePath));
            params.put("appver", Utils.getAppVersion(getBaseContext()));
            params.put("sdkint", String.valueOf(Build.VERSION.SDK_INT));
            params.put("sdk", Build.VERSION.RELEASE);
            params.put("manufacturer", Build.MANUFACTURER);
            params.put("host", Build.HOST);
            params.put("board", Build.BOARD);
            params.put("type", Build.TYPE);
            params.put("brand", Build.BRAND);
            params.put("product", Build.PRODUCT);
            params.put("model", Build.MODEL);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            params.put("solution", String.format("%dx%d", dm.widthPixels, dm.heightPixels));
            params.put("density", String.valueOf(dm.density));
            params.put("densitydpi", String.valueOf(dm.densityDpi));

            String url = "http://www.baidu.com/";
            StringRequest request = new StringRequest(Method.POST, url, this, this) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    byte body[] = null;
                    try {
                        JSONObject jsonObject = new JSONObject();
                        Set<String> keySet = params.keySet();
                        Iterator<String> iterator = keySet.iterator();
                        String key = null;
                        String value = null;
                        while(iterator.hasNext()) {
                            key = iterator.next();
                            value = params.get(key);
                            jsonObject.put(key, value);
                        }
                        Log.d(Log.TAG, "body : " + jsonObject.toString());
                        body = jsonObject.toString().getBytes();
                    } catch(Exception e) {
                        Log.d(Log.TAG, "error : " + e);
                    }
                    return body;
                }
            };
            GlobalRequest.get(getBaseContext()).getRequestQueue().add(request);
        }

        private String readErrorLogs(String filePath) {
            StringBuilder builder = new StringBuilder();
            try {
                FileInputStream fis = new FileInputStream(filePath);
                byte buffer [] = new byte[4096];
                int read = 0;
                while((read = fis.read(buffer)) > 0) {
                    builder.append(new String(buffer, 0, read));
                }
                fis.close();
            } catch(Exception e) {
                Log.d(Log.TAG, "error : " + e);
            }
            return builder.toString();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(Log.TAG, "error : " + error);
        }

        @Override
        public void onResponse(String response) {
        }

        @Override
        public void run() {
            String exceptionDir = Utils.getExceptionLogDir(getBaseContext());
            if (TextUtils.isEmpty(exceptionDir)) {
                return;
            }
            File dir = new File(exceptionDir);
            File files[] = dir.listFiles();
            if (files == null) {
                return ;
            }
            for (File f : files) {
                Log.d(Log.TAG, "f : " + f);
                execute(f.getAbsolutePath());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
