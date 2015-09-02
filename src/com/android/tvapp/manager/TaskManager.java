package com.android.tvapp.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.android.tvapp.info.TaskInfo;
import com.android.tvapp.util.Log;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TaskManager {

    private static final String URL = "file:///android_asset/demo.json";
    // private static final String URL = "http://www.baidu.com";
    private Context mContext;
    private List<TaskInfo> mTaskList;
    public TaskManager(Context context) {
        mContext = context;
    }

    public void startTask() {
        requestTaskList();
    }

    private void requestTaskList() {
        TaskListRequest request = new TaskListRequest();
        request.request();
    }

    private void parseTaskList(String content) {
        Gson gson = new Gson();
        try {
            List<TaskInfo> list = gson.fromJson(content, new TypeToken<List<TaskInfo>>(){}.getType());
            mTaskList = list;
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        if (mTaskList != null) {
            for (TaskInfo info : mTaskList) {
                // Log.d(Log.TAG, "info : " + info);
            }
        }
    }

    class TaskListRequest implements Listener<String>, ErrorListener {

        public void request() {
            StringRequest request = new StringRequest(URL, this, this);
            Volley.newRequestQueue(mContext).add(request);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(Log.TAG, "error : " + error);
            try {
                InputStream is = mContext.getAssets().open("demo.json");
                byte[] buffer = new byte[1024];
                int read = 0;
                StringBuilder builder = new StringBuilder();
                while((read = is.read(buffer)) > 0) {
                    builder.append(new String(buffer, 0, read));
                }
                is.close();
                parseTaskList(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }

        @Override
        public void onResponse(String response) {
            Log.d(Log.TAG, "response : " + response);
            if (!TextUtils.isEmpty(response)) {
                parseTaskList(response);
            }
        }
    }
}
