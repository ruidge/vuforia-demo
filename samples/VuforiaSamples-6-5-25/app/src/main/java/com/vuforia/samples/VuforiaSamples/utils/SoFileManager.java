package com.vuforia.samples.VuforiaSamples.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rui.zhang on 2017/11/17.
 */

public class SoFileManager {
    private static final String TAG = "SoFileManager";
    private static String vuforiaName = "libVuforia.so";
    private static String dataPre = "/data/data/";

    private static File getInternSoDir(Context context) {
        File soLib = context.getDir("solib", Context.MODE_PRIVATE);
//        File soLib = context.getFilesDir();
//        File soLib = new File(dataPre + context.getPackageName() + "/files/");
        if (!soLib.exists()) {
            soLib.mkdirs();
        }
        if (soLib.exists()) {
            return soLib;
        } else {
            return null;
        }
    }

    private static File getVuforiaInternFile(Context context) {
        File soDir = getInternSoDir(context);
        if (soDir != null) {
            File file = new File(soDir, vuforiaName);
            Log.w(TAG, "getVuforiaInternFile: " + file.toString());
            return file;
        } else {
            return null;
        }
    }

    private static File getSDDir() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir;
    }

    private static File getVuforiaSDFile(Context context) {
        File soDir = getSDDir();
        if (soDir != null) {
            File file = new File(soDir, vuforiaName);
            Log.w(TAG, "getVuforiaSDFile: " + file.toString());
            return file;
        } else {
            return null;
        }
    }

    private static boolean copyVuforia(Context context) {
        File soFrom = getVuforiaSDFile(context);
        File soTo = getVuforiaInternFile(context);
        if (soFrom != null && soTo != null) {
            return copySdcardFile(soFrom.getAbsolutePath(), soTo.getAbsolutePath());
        }
        return false;

    }

    private static boolean isFileValid(File file) {
        if (file != null && file.length() > 0) {
            Log.w(TAG, "file length is : " + file.length() / 1024 + "kb");
            Log.w(TAG, "file length is : " + file.length() / 1024 / 1024 + "mb");
            return true;
        }
        return false;

    }

    public static boolean loadVuforia(final Context context, final Runnable callback) {
        final File file = getVuforiaInternFile(context);
        if (!isFileValid(file)) {
            copyVuforia(context);
        }
        Log.w(TAG, "loadVuforia path : " + file.getAbsolutePath());
        if (isFileValid(file)) {
            System.load(file.getAbsolutePath());
            new Handler(Looper.getMainLooper()).post(callback);
            return true;
        } else {
            return false;
        }
    }

    public static boolean copySdcardFile(String fromFile, String toFile) {
        InputStream fosfrom = null;
        OutputStream fosto = null;
        try {
            fosfrom = new FileInputStream(fromFile);
            fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            Log.w(TAG, "copySdcardFile success ");
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            if (fosfrom != null) {
                try {
                    fosfrom.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fosto != null) {
                try {
                    fosto.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
