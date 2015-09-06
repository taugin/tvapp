package com.android.tvapp.manager;

import java.io.UnsupportedEncodingException;

import android.content.Context;

import com.android.tvapp.info.PollInfo;
import com.android.tvapp.util.GlobalRequest;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

public class PollRequest implements Listener<String>, ErrorListener {

    private Context mContext;
    private Gson mGson;

    private OnPollRequestCompletedListener mOnPollRequestCompletedListener;
    public PollRequest(Context context) {
        mContext = context;
        mGson = new Gson();
    }

    public void setOnPollRequestCompletedListener(OnPollRequestCompletedListener l) {
        mOnPollRequestCompletedListener = l;
    }

    public void requestPollInfo() {
        requestPoll();
    }

    private void requestPoll() {
        String url = Utils.genParamUrl(Utils.URL_POLL);
        // Log.d(Log.TAG, "url : " + url);
        StringRequest request = new StringRequest(Utils.URL_POLL, this, this){
            @Override
            protected Response<String> parseNetworkResponse(
                    NetworkResponse response) {
                String parsed;
                try {
                    String charset = HttpHeaderParser.parseCharset(response.headers);
                    if ("ISO-8859-1".equalsIgnoreCase(charset)) {
                        charset = "utf-8";
                    }
                    parsed = new String(response.data, charset);
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        GlobalRequest.get(mContext).getRequestQueue().add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(Log.TAG, "error : " + error);
        if (mOnPollRequestCompletedListener != null) {
            mOnPollRequestCompletedListener.onPollRequestCompleted(null);
        }
    }

    @Override
    public void onResponse(String response) {
        // Log.d(Log.TAG, "response : " + response);
        PollInfo pollInfo = null;
        try {
            if (mGson == null) {
                mGson = new Gson();
            }
            pollInfo = mGson.fromJson(response, PollInfo.class);
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        Log.d(Log.TAG, "pollInfo : " + pollInfo);
        if (mOnPollRequestCompletedListener != null) {
            mOnPollRequestCompletedListener.onPollRequestCompleted(pollInfo);
        }
    }
    
    public interface OnPollRequestCompletedListener{
        public void onPollRequestCompleted(PollInfo pollInfo);
    }
}
