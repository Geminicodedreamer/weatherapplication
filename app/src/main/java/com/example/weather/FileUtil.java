package com.example.weather;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    private final static String TAG = "FileUtil";


    public static boolean checkFileUri(Context ctx, String path) {
        boolean result = true;
        File file = new File(path);
        if (!file.exists() || !file.isFile() || file.length() <= 0) {
            result = false;
        }

        try {
            Uri uri = Uri.parse(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(ctx,
                        ctx.getPackageName()+".fileProvider", new File(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }



}
