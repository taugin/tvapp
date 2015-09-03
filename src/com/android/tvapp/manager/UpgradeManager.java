package com.android.tvapp.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tvapp.R;
import com.android.tvapp.info.UpgradeInfo;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;
import com.google.gson.Gson;

public class UpgradeManager implements Runnable, OnClickListener {

    private static final int ACTION_FETCH_CONFIG = 0;
    private static final int ACTION_DOWNLOAD = 1;

    private static final int MSG_SHOW_PROGRESS_DIALOG = 0;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 1;
    private static final int MSG_SHOW_TOAST = 2;
    private static final int MSG_SHOW_NEWVERSION_DIALOG = 3;
    private static final int MSG_UPDATE_PROGRESS_BAR = 4;
    private static final int MSG_SET_PROGRESS_BAR_MAX = 5;
    private static final int MSG_DISMISS_ALERTDIALOG = 6;

    private Context mContext;
    private int mAction = -1;
    private ProgressDialog mProgressDialog = null;
    private Handler mHandler = null;
    private boolean mCancelDownload = false;

    private ProgressBar mProgressBar;
    private Button mDownloadSize;
    private Button mDownload;
    private Button mCancel;
    private View mProgressLayout;
    private UpgradeInfo mUpgradeInfo;
    private AlertDialog mAlertDialog;

    public UpgradeManager(Context context) {
        mContext = context;
        init();
    }

    private String getTestConfig() {
        try {
            InputStream is = mContext.getAssets().open("upgrade.json");
            byte[] buffer = new byte[1024];
            int read = 0;
            StringBuilder builder = new StringBuilder();
            while((read = is.read(buffer)) > 0) {
                builder.append(new String(buffer, 0, read));
            }
            is.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUpgradeConfig() {
        try {
            URL url = new URL(Utils.UPGRADE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(100000);
            conn.connect();
            InputStream inStream = conn.getInputStream();
            byte buf[] = new byte[1024];
            int read = 0;
            StringBuilder builder = new StringBuilder();
            while ((read = inStream.read(buf)) > 0) {
                builder.append(new String(buf, 0, read));
            }
            inStream.close();
            // Log.d(Log.TAG, "config = \n" + builder.toString());
            return builder.toString();
        } catch (MalformedURLException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }

    private String download(String apkUrl, String fileName) {
        Message msg = null;
        try {
            URL url = new URL(apkUrl);
            URLConnection conn = (URLConnection) url.openConnection();
            //conn.setDoInput(true);
            //conn.setDoOutput(true);
            Log.d(Log.TAG, "setTimeout");
            conn.setConnectTimeout(10000);
            conn.connect();
            long fileLen = 0;
            String length = conn.getHeaderField("Content-Length");
            if (TextUtils.isDigitsOnly(length)) {
                try {
                    fileLen = Long.parseLong(length);
                } catch(NumberFormatException e) {
                    Log.d(Log.TAG, "error : " + e);
                }
            }
            if (fileLen == 0) {
                return null;
            }
            msg = mHandler.obtainMessage(MSG_SET_PROGRESS_BAR_MAX);
            msg.obj = fileLen;
            mHandler.sendMessage(msg);
            Log.d(Log.TAG, "fileLen = " + fileLen);
            InputStream inStream = conn.getInputStream();
            byte buf[] = new byte[1024];
            int read = 0;
            String apkPath = generateDetFile(fileName);
            if (TextUtils.isEmpty(apkPath)) {
                return null;
            }
            Log.d(Log.TAG, "apkPath = " + apkPath);
            FileOutputStream fos = new FileOutputStream(apkPath);
            long totalRead = 0;
            while ((read = inStream.read(buf)) > 0 && !mCancelDownload) {
                totalRead += read;
                msg = mHandler.obtainMessage(MSG_UPDATE_PROGRESS_BAR);
                msg.obj = totalRead;
                mHandler.sendMessage(msg);
                fos.write(buf, 0, read);
            }
            Log.d(Log.TAG, "totalRead = " + totalRead);
            fos.close();
            inStream.close();
            if (fileLen == totalRead) {
                return apkPath;
            } else {
                File f = new File(apkPath);
                if (f.exists()) {
                    f.delete();
                }
            }
        } catch (MalformedURLException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }

    private String generateDetFile(String apkName) {
        File externalFile = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (externalFile != null) {
            String apkPath = externalFile.getAbsolutePath() + File.separator
                    + apkName;
            return apkPath;
        }
        File packagePath = mContext.getCacheDir();
        if (packagePath != null) {
            String apkPath = packagePath.getAbsolutePath() + File.separator
                    + apkName;
            return apkPath;
        }
        return null;
    }

    private void upgradeCheck() {
        String config = null;
        if (Utils.USE_TEST_MODE) {
            config = getTestConfig();
        } else {
            config = getUpgradeConfig();
        }
        mHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS_DIALOG);
        if (TextUtils.isEmpty(config)) {
            Intent intent = new Intent(Utils.NONEW_VERSION);
            mContext.sendBroadcast(intent);
            return;
        }
        Gson gson = new Gson();
        UpgradeInfo info = gson.fromJson(config, UpgradeInfo.class);
        mUpgradeInfo = info;
        Log.d(Log.TAG, info.toString());
        int versionCode = getAppVer();
        int lastestVersion = 0;
        try {
            lastestVersion = Integer.parseInt(info.versioncode);
        } catch(NumberFormatException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        Log.d(Log.TAG, "lastestVersion : " + lastestVersion + " , versionCode : " + versionCode);
        if (versionCode >= lastestVersion) {
            // mHandler.sendEmptyMessage(MSG_SHOW_TOAST);
            Intent intent = new Intent(Utils.NONEW_VERSION);
            mContext.sendBroadcast(intent);
            return;
        }
        mHandler.sendEmptyMessage(MSG_SHOW_NEWVERSION_DIALOG);
    }

    private void openFile(File file) {
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    private void newVersionDialog() {
        if (mAlertDialog == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.upgrade_layout, null);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            mProgressLayout = view.findViewById(R.id.progressbar_layout);
            mDownloadSize = mDownload = (Button) view.findViewById(R.id.upgrade_button);
            mDownload.setOnClickListener(this);
            mCancel = (Button) view.findViewById(R.id.cancel);
            mCancel.setOnClickListener(this);
            TextView versionView = (TextView) view.findViewById(R.id.version_name);
            String versionName = mContext.getResources().getString(R.string.version_name, mUpgradeInfo.versionname);
            versionView.setText(versionName);
            TextView releaseView = (TextView) view.findViewById(R.id.release_time);
            String releaseTime = mContext.getResources().getString(R.string.release_time, mUpgradeInfo.releasetime);
            releaseView.setText(releaseTime);
            if (TextUtils.isEmpty(mUpgradeInfo.instructions)) {
                mUpgradeInfo.instructions = mContext.getResources().getString(R.string.empty_instructions);
            }
            TextView instructionsView = (TextView) view.findViewById(R.id.version_instruction);
            instructionsView.setText(mUpgradeInfo.instructions);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String title = mContext.getResources().getString(R.string.new_version_tiptitle);
            builder.setTitle(title);
            builder.setView(view);

            mAlertDialog = builder.create();
        }
        mAlertDialog.setCancelable(false);
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
    }

    public void checkUpgrade() {
        mAction = ACTION_FETCH_CONFIG;
        mCancelDownload = false;
        mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
        new Thread(this).start();
    }

    private void startDownload() {
        mAction = ACTION_DOWNLOAD;
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (ACTION_FETCH_CONFIG == mAction) {
            upgradeCheck();
        } else {
            String apkPath = download(mUpgradeInfo.downloadurl,
                    mUpgradeInfo.apkname);
            mHandler.sendEmptyMessage(MSG_DISMISS_ALERTDIALOG);
            if (!TextUtils.isEmpty(apkPath)) {
                openFile(new File(apkPath));
                Intent intent = new Intent(Utils.FINISH_ACTIVITY);
                mContext.sendBroadcast(intent);
            }
        }
    }

    private int getAppVer() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return -1;
    }

    @SuppressLint("HandlerLeak")
    private void init() {
        mHandler = new Handler(mContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_SHOW_PROGRESS_DIALOG:
                    if (mProgressDialog == null) {
                        String content = mContext.getResources().getString(
                                R.string.loading);
                        mProgressDialog = ProgressDialog.show(mContext, null,
                                content, true, false);
                        mProgressDialog.show();
                        Log.d(Log.TAG, "show progress dialog");
                    }
                    break;
                case MSG_DISMISS_PROGRESS_DIALOG:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    break;
                case MSG_SHOW_NEWVERSION_DIALOG:
                    newVersionDialog();
                    break;
                case MSG_SHOW_TOAST:
                    Toast.makeText(mContext, R.string.no_newversion_tip,
                            Toast.LENGTH_LONG).show();
                    break;
                case MSG_SET_PROGRESS_BAR_MAX: {
                    Long integer = (Long) msg.obj;
                    mProgressBar.setMax(integer.intValue());
                    mProgressLayout.setVisibility(View.VISIBLE);
                    mDownloadSize.setTextColor(Color.WHITE);
                }
                    break;
                case MSG_UPDATE_PROGRESS_BAR: {
                    Long integer = (Long) msg.obj;
                    mProgressBar.setProgress(integer.intValue());
                    int max = mProgressBar.getMax();
                    int cur = mProgressBar.getProgress();
                    mDownloadSize.setText(cur + "/" + max);
                    break;
                }
                case MSG_DISMISS_ALERTDIALOG:
                    if (mAlertDialog != null && mAlertDialog.isShowing()) {
                        mAlertDialog.dismiss();
                        mAlertDialog = null;
                    }
                    break;
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.upgrade_button:
            startDownload();
            v.setEnabled(false);
            v.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            ((TextView)v).setText(R.string.connecting);
            break;
        case R.id.cancel:
            mCancelDownload = true;
            if (mAlertDialog != null && mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
                mAlertDialog = null;
                Intent intent = new Intent(Utils.FINISH_ACTIVITY);
                mContext.sendBroadcast(intent);
            }
            break;
        }
    }
}
