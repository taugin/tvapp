package com.android.tvapp.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import android.content.Context;

import com.android.tvapp.info.TaskInfo;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TaskRequest implements Listener<String>, ErrorListener {

    private Context mContext;

    private OnTaskRequestCompletedListener mOnTaskRequestCompletedListener;
    public TaskRequest(Context context) {
        mContext = context;
    }

    public void setOnTaskRequestCompletedListener(OnTaskRequestCompletedListener l) {
        mOnTaskRequestCompletedListener = l;
    }

    public void requestTaskInfo() {
        if (Utils.USE_TEST_MODE) {
            readFromAssets();
        } else {
            requestTaskList();
        }
    }

    private void requestTaskList() {
        String url = Utils.genParamUrl(Utils.URL_TASKLIST);
        Log.d(Log.TAG, "url : " + url);
        StringRequest request = new StringRequest(url, this, this){
            @Override
            protected Response<String> parseNetworkResponse(
                    NetworkResponse response) {
                String parsed;
                try {
                    String charset = HttpHeaderParser.parseCharset(response.headers);
                    if ("ISO-8859-1".equalsIgnoreCase(charset)) {
                        charset = "utf-8";
                    }
                    Log.d(Log.TAG, "charset : " + charset);
                    parsed = new String(response.data, charset);
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        Volley.newRequestQueue(mContext).add(request);
    }

    private void parseTaskList(String content) {
        Log.d(Log.TAG, "");
        Gson gson = new Gson();
        List<TaskInfo> list = null;
        try {
            list = gson.fromJson(content, new TypeToken<List<TaskInfo>>() {
            }.getType());
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        if (mOnTaskRequestCompletedListener != null) {
            mOnTaskRequestCompletedListener.onTaskRequestCompleted(list);
        }
    }

    private void readFromAssets() {
        Log.d(Log.TAG, "");
        StringBuilder builder = new StringBuilder();
        try {
            InputStream is = mContext.getAssets().open("task.json");
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                builder.append(new String(buffer, 0, read));
            }
            is.close();
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        parseTaskList(builder.toString());
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(Log.TAG, "error : " + error);
        if (mOnTaskRequestCompletedListener != null) {
            mOnTaskRequestCompletedListener.onTaskRequestCompleted(null);
        }
    }

    @Override
    public void onResponse(String response) {
        // Log.d(Log.TAG, "response : " + response);
        parseTaskList(response);
    }
    
    public interface OnTaskRequestCompletedListener{
        public void onTaskRequestCompleted(List<TaskInfo> list);
    }
}
