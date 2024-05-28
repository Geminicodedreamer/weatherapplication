package com.example.weather;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

@SuppressLint("DefaultLocale")
public class MediaUtil {
    private final static String TAG = "MediaUtil";


    public static String formatDuration(int milliseconds) {
        int seconds = milliseconds / 1000;
        int hour = seconds / 3600;
        int minute = seconds / 60;
        int second = seconds % 60;
        String str;
        if (hour > 0) {
            str = String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            str = String.format("%02d:%02d", minute, second);
        }
        return str;
    }



}
