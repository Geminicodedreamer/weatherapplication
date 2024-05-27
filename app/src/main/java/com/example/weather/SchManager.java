package com.example.weather;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SchManager {
    private static SQLiteDatabase database;

    public static void initDB(Context context){
        //初始化数据库信息
        SchHelper helper = new SchHelper(context);
        database = helper.getWritableDatabase();
    }

    public static List<SchDataBase> getAllSchInfo() {
        List<SchDataBase> resultList = new ArrayList<>();

        // 执行查询
        Cursor cursor = database.query("schInfo", null, null, null, null, null, null);

        // 遍历查询结果
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Integer id = cursor.getInt(cursor.getColumnIndex("_id"));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range") String place = cursor.getString(cursor.getColumnIndex("place"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("description"));

                SchDataBase info = new SchDataBase(id, title, place, date, time, description);
                resultList.add(info);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return resultList;
    }


    public static long addSchInfo(SchDataBase info){
        ContentValues values = new ContentValues();
        values.put("title",info.getTitle());
        values.put("place",info.getPlace());
        values.put("date",info.getDate());
        values.put("time",info.getTime());
        values.put("description",info.getDescription());
        return database.insert("schInfo",null,values);
    }

    public static List<SchDataBase> querySchInfoByDate(String date) {
        List<SchDataBase> resultList = new ArrayList<>();

        // 查询条件
        String selection = "date = ?";
        String[] selectionArgs = { date };

        // 执行查询
        Cursor cursor = database.query("schInfo", null, selection, selectionArgs, null, null, null);

        // 遍历查询结果
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range") String place = cursor.getString(cursor.getColumnIndex("place"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("description"));

                SchDataBase info = new SchDataBase(_id,title, place, date, time, description);
                resultList.add(info);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return resultList;
    }

    public static  int deleteSchInfoBydate(String date){
        return database.delete("schInfo","date=?",new String[]{date});
    }

    public static void updateSchInfo(SchDataBase info) {
        ContentValues values = new ContentValues();
        values.put("title", info.getTitle());
        values.put("place", info.getPlace());
        values.put("date", info.getDate());
        values.put("time", info.getTime());
        values.put("description", info.getDescription());

        String whereClause = "_id = ?";
        int id = info.get_id(); // 获取 _id
        String[] whereArgs = {String.valueOf(id)}; // 转换为字符串数组

        int rowsUpdated = database.update("schInfo", values, whereClause, whereArgs);
        Log.d("SchManager", "Updated " + rowsUpdated + " rows.");
    }



}
