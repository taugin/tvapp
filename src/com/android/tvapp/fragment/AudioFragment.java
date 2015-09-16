package com.android.tvapp.fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.tvapp.R;
import com.android.tvapp.util.AudioPlayHelper;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.android.tvapp.view.CustomViewFlipper;

public class AudioFragment extends BaseFragment implements OnCompleteListener, OnClickListener {

    private CustomViewFlipper mViewFlipper;
    private AudioPlayHelper mAudoPlayHelper;
    private Handler mHandler;
    private List<Bitmap> mBitmaps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tvapp_showaudio, null);
        mViewFlipper = (CustomViewFlipper) view.findViewById(R.id.viewflipper);
        mViewFlipper.setOnClickListener(this);
        mHandler = new Handler();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBitmaps = new ArrayList<Bitmap>();
        start();
    }

    @Override
    public void onDestroy() {
        Log.d(Log.TAG, "");
        super.onDestroy();
        if (mViewFlipper != null) {
            mViewFlipper.stopFlipping();
        }
        if (mBitmaps != null) {
            for (Bitmap bitmap : mBitmaps) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
        if (mAudoPlayHelper != null) {
            mAudoPlayHelper.stop();
        }
    }

    private void addBgImage() {
        mViewFlipper.removeAllViews();
        if (mBitmaps != null) {
            mBitmaps.clear();
        }
        ImageView imageView = null;
        for (String url : mTaskInfo.imgurl) {
            Log.d(Log.TAG, "imgurl : " + url);
            imageView = new ImageView(getActivity());
            imageView.setScaleType(ScaleType.FIT_CENTER);
            /*
            VolleyImageLoader loader = VolleyImageLoader.getVolleyImageLoader(getActivity());
            ImageListener imageListener = VolleyImageLoader.getImageListener(imageView, 0);
            imageView.setTag(VolleyImageLoader.encodeUrl(url));
            loader.get(url, imageListener);
            */
            ImageLoader loader = new ImageLoader(url, imageView);
            loader.execute();
            mViewFlipper.addView(imageView);
        }
    }

    private void start() {
        Log.d(Log.TAG, "");
        if (mViewFlipper != null) {
            int interval = 0;
            try {
                interval = Integer.parseInt(mTaskInfo.interval);
                interval *= 1000;
            } catch(NumberFormatException e) {
                interval = 10 * 1000;
            }
            Log.d(Log.TAG, "interval : " + interval);
            mViewFlipper.setFlipInterval(interval);
            addBgImage();
            mViewFlipper.startFlipping();
            Log.d(Log.TAG, "View Count : " + mViewFlipper.getChildCount());
        }

        String musicUrl = mTaskInfo.audiourl;
        if (!TextUtils.isEmpty(musicUrl)) {
            Log.d(Log.TAG, "audiourl : " + musicUrl);
            mAudoPlayHelper = new AudioPlayHelper();
            mAudoPlayHelper.setOnCompleteListener(this);
            mAudoPlayHelper.playUrl(musicUrl);
            Log.d(Log.TAG, "after playurl");
        } else {
            if (mHandler != null) {
                long time = 0;
                try {
                    time = Long.parseLong(mTaskInfo.time);
                    time = time * 1000;
                } catch(NumberFormatException e) {
                    time = 10 * 1000;
                }
                Log.d(Log.TAG, "time : " + time);
                mHandler.postDelayed(mRunnable, time);
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(Log.TAG, "send text task complete");
            Intent intent = new Intent(Utils.TASK_COMPLETE);
            if (getActivity() != null) {
                getActivity().sendBroadcast(intent);
            }
        }
    };

    @Override
    public void onComplete() {
        Log.d(Log.TAG, "send audio task complete");
        Intent intent = new Intent(Utils.TASK_COMPLETE);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onClick(View view) {
        Log.d(Log.TAG, "send audio task complete");
        Intent intent = new Intent(Utils.TASK_COMPLETE);
        getActivity().sendBroadcast(intent);
    }

    class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

        private String mImgUrl;
        private ImageView mImageView;
        public ImageLoader(String imgurl, ImageView imageView) {
            mImgUrl = imgurl;
            mImageView = imageView;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Log.d(Log.TAG, "imgurl : " + mImgUrl + " , w : " + bitmap.getWidth() + " , h : " + bitmap.getHeight());
                mBitmaps.add(bitmap);
                mImageView.setImageBitmap(bitmap);
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            HttpGet httpRequest = new HttpGet(mImgUrl);
            HttpClient httpclient = new DefaultHttpClient();
            try {
                HttpResponse httpResponse = httpclient.execute(httpRequest);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    InputStream is = httpEntity.getContent();
                    byte [] buffer = new byte[4096];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int read = 0;
                    while((read = is.read(buffer)) > 0) {
                        baos.write(buffer, 0, read);
                    }
                    is.close();
                    byte[] bitmapByte = baos.toByteArray();
                    baos.close();
                    Bitmap bitmap = null;
                    int simpleSize = 1;
                    while(true) {
                        bitmap = handlePic(bitmapByte, simpleSize);
                        // Log.d(Log.TAG, "bitmap : " + bitmap);
                        if (bitmap == null) {
                            simpleSize <<= 1;
                        } else {
                            break;
                        }
                    }
                    return bitmap;
                }
            } catch (ClientProtocolException e) {
                Log.d(Log.TAG, "error : " + e);
            } catch (IOException e) {
                Log.d(Log.TAG, "error : " + e);
            } catch(Exception e) {
                Log.d(Log.TAG, "error : " + e);
            } catch(OutOfMemoryError error) {
                Log.d(Log.TAG, "error : " + error);
            }
            return null;
        }
    }

    private Bitmap handlePic(byte []data, int simpleSize) {
        try {
            Options opts = new Options();
            opts.inSampleSize = simpleSize;
            opts.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            if (bitmap.getWidth() > 4096 || bitmap.getHeight() > 4096) {
                bitmap.recycle();
                return null;
            }
            return bitmap;
        } catch(OutOfMemoryError error) {
            Log.d(Log.TAG, "error : " + error);
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }
}
