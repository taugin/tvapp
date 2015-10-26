package com.android.tvapp.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ViewFlipper;

import com.android.tvapp.R;
import com.android.tvapp.util.AudioPlayHelper;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;

public class AudioFragment2 extends BaseFragment implements OnCompleteListener,
        OnClickListener {

    private MyViewFlipper mViewFlipper;
    private AudioPlayHelper mAudoPlayHelper;
    private ImageView mImageView1;
    private ImageView mImageView2;
    private int mIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mViewFlipper  = new MyViewFlipper(getActivity());
        mViewFlipper.setOnClickListener(this);
        return mViewFlipper;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        start();
    }

    @Override
    public void onDestroy() {
        Log.d(Log.TAG, "");
        super.onDestroy();
        if (mViewFlipper != null) {
            mViewFlipper.stopFlipping();
        }
        if (mAudoPlayHelper != null) {
            mAudoPlayHelper.stop();
        }
        deleteCache();
    }

    private void deleteCache() {
        File cacheDir = getPicCache();
        if (cacheDir != null) {
            File fileList[] = cacheDir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    Log.d(Log.TAG, "file : " + file);
                    file.delete();
                }
            }
        }
    }

    public void pauseTask() {
        Log.d(Log.TAG, "");
        mViewFlipper.stopFlipping();
        if (mTaskInfo != null && !TextUtils.isEmpty(mTaskInfo.audiourl)) {
            if (mAudoPlayHelper != null && mAudoPlayHelper.isPlaying()) {
                mAudoPlayHelper.pause();
            }
        } else {
            super.pauseTask();
        }
    }

    public void resumeTask() {
        Log.d(Log.TAG, "");
        mViewFlipper.startFlipping();
        if (mTaskInfo != null && !TextUtils.isEmpty(mTaskInfo.audiourl)) {
            if (mAudoPlayHelper != null) {
                mAudoPlayHelper.play();
            }
        } else {
            super.resumeTask();
        }
    }

    private void addBgImage() {
        mViewFlipper.removeAllViews();
        mImageView1 = new ImageView(getActivity());
        mImageView1.setScaleType(ScaleType.FIT_CENTER);
        mViewFlipper.addView(mImageView1);
        mImageView2 = new ImageView(getActivity());
        mImageView2.setScaleType(ScaleType.FIT_CENTER);
        mViewFlipper.addView(mImageView2);
    }

    private void loadNetImages() {
        if (mTaskInfo != null && mTaskInfo.imgurl != null) {
            ImageLoader loader = new ImageLoader();
            loader.execute(mTaskInfo.imgurl);
        }
    }

    protected void start() {
        loadNetImages();
        Log.d(Log.TAG, "");
        if (mViewFlipper != null) {
            int interval = 0;
            try {
                interval = Integer.parseInt(mTaskInfo.interval);
                interval *= 1000;
            } catch (NumberFormatException e) {
                interval = 10 * 1000;
            }
            Log.d(Log.TAG, "interval : " + interval);
            mViewFlipper.setFlipInterval(interval);
            addBgImage();
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
            super.start();
        }
    }

    @Override
    public void onComplete() {
        Log.d(Log.TAG, "send audio task complete");
        sendCompleteBroadcast();
    }

    @Override
    public void onClick(View view) {
        Log.d(Log.TAG, "send audio task complete");
        sendCompleteBroadcast();
    }

    private void switchImageView() {
        if (mViewFlipper == null || mTaskInfo == null || mTaskInfo.imgurl == null) {
            return;
        }
        mIndex ++;
        if (mIndex >= mTaskInfo.imgurl.length) {
            mIndex = 0;
        }
        String url = null;
        if (mTaskInfo.imgurl.length > mIndex) {
            url = mTaskInfo.imgurl[mIndex];
        }
        if (mViewFlipper.getCurrentView() == mImageView1) {
            if (!TextUtils.isEmpty(url)) {
                mImageView2.setImageBitmap(getBitmapByUrl(url));
            }
        } else {
            if (!TextUtils.isEmpty(url)) {
                mImageView1.setImageBitmap(getBitmapByUrl(url));
            }
        }
    }

    private void startShowAndFlip() {
        if (getActivity() == null) {
            return;
        }
        Log.d(Log.TAG, "");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mViewFlipper == null || mTaskInfo == null || mTaskInfo.imgurl == null) {
                    return;
                }
                mIndex = 0;
                String url = null;
                if (mTaskInfo.imgurl.length > mIndex) {
                    url = mTaskInfo.imgurl[mIndex];
                }
                if (!TextUtils.isEmpty(url)) {
                    mImageView1.setImageBitmap(getBitmapByUrl(url));
                }
                mViewFlipper.startFlipping();
            }
        });
    }

    private Bitmap getBitmapByUrl(String url) {
        String filePath = getFilePath(url);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        return bitmap;
    }

    class ImageLoader extends AsyncTask<String, Void, Void> {

        private boolean mNotified = false;
        @Override
        protected Void doInBackground(String... params) {
            if (params != null) {
                boolean ret = false;
                for (String url : params) {
                    ret = savePic(url);
                    if (ret && !mNotified) {
                        mNotified = true;
                        startShowAndFlip();
                    }
                }
            }
            return null;
        }
    }

    private Bitmap handlePic(byte[] data, int simpleSize) {
        try {
            Options opts = new Options();
            opts.inSampleSize = simpleSize;
            opts.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    opts);
            if (bitmap.getWidth() > 4096 || bitmap.getHeight() > 4096) {
                bitmap.recycle();
                return null;
            }
            return bitmap;
        } catch (OutOfMemoryError error) {
            Log.d(Log.TAG, "error : " + error);
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }

    private boolean savePic(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        HttpGet httpRequest = new HttpGet(url);
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = httpclient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream is = httpEntity.getContent();
                byte[] buffer = new byte[4096];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int read = 0;
                while ((read = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, read);
                }
                is.close();
                byte[] bitmapByte = baos.toByteArray();
                baos.close();
                Bitmap bitmap = null;
                int simpleSize = 1;
                while (true) {
                    bitmap = handlePic(bitmapByte, simpleSize);
                    // Log.d(Log.TAG, "bitmap : " + bitmap);
                    if (bitmap == null) {
                        simpleSize <<= 1;
                    } else {
                        break;
                    }
                }
                if (bitmap != null) {
                    String filePath = getFilePath(url);
                    Log.d(Log.TAG, "filePath : " + filePath);
                    FileOutputStream fos = new FileOutputStream(filePath);
                    bitmap.compress(CompressFormat.PNG, 80, fos);
                    fos.close();
                    return true;
                }
            }
        } catch (ClientProtocolException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (OutOfMemoryError error) {
            Log.d(Log.TAG, "error : " + error);
        }
        return false;
    }

    private String getFilePath(String url) {
        String fileName = Utils.string2MD5(url);
        fileName += ".png";
        File picDir = getPicCache();
        if (picDir != null) {
            File filePath = new File(picDir, fileName);
            return filePath.getAbsolutePath();
        }
        return null;
    }

    private File getPicCache() {
        File cacheDir = Environment.getExternalStoragePublicDirectory("tvapp");
        if (cacheDir != null) {
            File tmpDir = new File(cacheDir, "pic_dir");
            if (tmpDir != null) {
                tmpDir.mkdirs();
                return tmpDir.getAbsoluteFile();
            }
        }
        return null;
    }

    public class MyViewFlipper extends ViewFlipper {

        private ArrayList<Animation> mInAnimations;
        private ArrayList<Animation> mOutAnimations;

        public MyViewFlipper(Context context) {
            super(context);
            init(context);
        }

        public MyViewFlipper(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        private void init(Context context) {
            mInAnimations = new ArrayList<Animation>();
            mOutAnimations = new ArrayList<Animation>();
            Animation animation = null;
            animation = AnimationUtils
                    .loadAnimation(context, R.anim.push_up_in);
            mInAnimations.add(animation);
            animation = AnimationUtils.loadAnimation(context,
                    R.anim.push_up_out);
            mOutAnimations.add(animation);
            animation = AnimationUtils.loadAnimation(context,
                    R.anim.push_left_in);
            mInAnimations.add(animation);
            animation = AnimationUtils.loadAnimation(context,
                    R.anim.push_left_out);
            mOutAnimations.add(animation);

            animation = AnimationUtils.loadAnimation(context, R.anim.alpha_in);
            mInAnimations.add(animation);
            animation = AnimationUtils.loadAnimation(context, R.anim.alpha_out);
            mOutAnimations.add(animation);

            animation = AnimationUtils.loadAnimation(context, R.anim.rotate_in);
            mInAnimations.add(animation);
            animation = AnimationUtils
                    .loadAnimation(context, R.anim.rotate_out);
            mOutAnimations.add(animation);
        }

        @Override
        public void showNext() {
            if (Utils.USE_TEST_MODE) {
                long time = System.currentTimeMillis();
                int index1 = new Random(time).nextInt(mInAnimations.size());
                int index2 = new Random(time).nextInt(mOutAnimations.size());
                setInAnimation(mInAnimations.get(index1));
                setOutAnimation(mOutAnimations.get(index2));
            }
            switchImageView();
            super.showNext();
        }
    }

}
