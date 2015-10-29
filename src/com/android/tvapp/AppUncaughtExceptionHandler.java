package com.android.tvapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;

public class AppUncaughtExceptionHandler implements UncaughtExceptionHandler {
    public static final String TAG = "AppUncaughtExceptionHandler";

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static AppUncaughtExceptionHandler mInstance = null;
    private Context mContext;
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private AppUncaughtExceptionHandler(Context context) {
        mContext = context;
    }

    public static AppUncaughtExceptionHandler getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppUncaughtExceptionHandler(context);
        }

        return mInstance;
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uncaughtException error[" + e + "]");
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        saveCrashInfo2File(ex);
        return true;
    }
    
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        
        try {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            Log.e(TAG, result);
            sb.append(result);
            try {
                long timestamp = System.currentTimeMillis();
                String time = formatter.format(new Date());
                String fileName = "crash-" + time + "-" + timestamp + ".txt";
                String path = Utils.getExceptionLogDir(mContext);
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                Log.d(Log.TAG, "path = " + (path + fileName));
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
                return fileName;
            } catch (Exception e) {
                Log.e(TAG, "saveCrashInfo2File e[" + e + "]");
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }
}