package com.example.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayActivity extends AppCompatActivity implements Recycler.OnItemClickListener, View.OnClickListener {
    private final static String TAG = "AudioPlayActivity";
    private int mCurrentPosition = 0;
    private RecyclerView rv_audio;
    private List<AudioInfo> mAudioList = new ArrayList<AudioInfo>();
    private Uri mAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private ImageView iv_pause,iv_prev,iv_next;
    private String[] mAudioColumn = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA};
    private AudioAdapter mAdapter;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private AlertDialog permissionDialog;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 102;
    private Timer mTimer = new Timer();
    private int mLastPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        checkAndRequestAudioPermission();
        rv_audio = findViewById(R.id.rv_audio);
        iv_pause = findViewById(R.id.iv_pause);
        iv_prev = findViewById(R.id.iv_prev);
        iv_next = findViewById(R.id.iv_next);

        loadAudioList();
        showAudioList();
        iv_pause.setOnClickListener(this);
        iv_prev.setOnClickListener(this);
        iv_next.setOnClickListener(this);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                playNextAudio();
            }
        });
    }

    // 检查并请求音频权限
    private void checkAndRequestAudioPermission() {
        // 检查是否已经授予 READ_MEDIA_AUDIO 权限
        int audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO);

        // 如果未授予该权限，则请求权限
        if (audioPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, AUDIO_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            // 检查用户是否授予了请求的权限
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予权限，可以开始使用相关功能
                Toast.makeText(this, "已授予音频文件权限", Toast.LENGTH_SHORT).show();
                // 在这里可以执行读取音频文件的操作
            } else {
                // 用户拒绝了权限请求，可能需要提供一些解释或处理
                Toast.makeText(this, "已拒绝授予音频文件权限", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void playPreviousAudio() {

        if (mLastPosition != -1) {
            AudioInfo currentAudio = mAudioList.get(mLastPosition);
            currentAudio.setProgress(-1);
            mAudioList.set(mLastPosition, currentAudio);
            mAdapter.notifyItemChanged(mLastPosition);
        }

        if (mCurrentPosition > 0) {
            mCurrentPosition--;
            mLastPosition --;

            AudioInfo previousAudio = mAudioList.get(mCurrentPosition);
            try {

                mTimer.cancel();
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(previousAudio.getPath());
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                startTimerForProgressUpdate(mCurrentPosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            mMediaPlayer.stop();

        }
    }


    private void playNextAudio() {

        if (mLastPosition != -1) {
            AudioInfo lastAudio = mAudioList.get(mLastPosition);
            lastAudio.setProgress(-1);
            mAudioList.set(mLastPosition, lastAudio);

            mAdapter.notifyItemChanged(mLastPosition);
        }


        if (mCurrentPosition < mAudioList.size() - 1) {
            mCurrentPosition++;
            mLastPosition ++;

            AudioInfo nextAudio = mAudioList.get(mCurrentPosition);
            try {

                mTimer.cancel();
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(nextAudio.getPath());
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                startTimerForProgressUpdate(mCurrentPosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            mMediaPlayer.stop();

        }
    }



    private void loadAudioList() {
        mAudioList.clear(); // 清空音频列表

        Cursor cursor = getContentResolver().query(mAudioUri, mAudioColumn,
                null, null, "date_modified desc");
        if (cursor != null) {
            Log.d(TAG, "cursor is not null");

            for (int i=0; i<30 && cursor.moveToNext(); i++) {
                Log.d(TAG, "cursor i="+i);
                AudioInfo audio = new AudioInfo();
                audio.setId(cursor.getLong(0));
                audio.setTitle(cursor.getString(1));
                audio.setDuration(cursor.getInt(2));
                audio.setSize(cursor.getLong(3));
                audio.setPath(cursor.getString(4));
                Log.d(TAG, audio.getTitle() + " " + audio.getDuration() + " " + audio.getSize() + " " + audio.getPath());
                if (!FileUtil.checkFileUri(this, audio.getPath())) {
                    i--;
                    Log.i(TAG,"hello");
                    continue;

                }
                mAudioList.add(audio);
            }
            cursor.close();
        } else {
            Log.d(TAG, "cursor is null");
        }

    }


    private void showAudioList() {

        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        rv_audio.setLayoutManager(manager);
        mAdapter = new AudioAdapter(this, mAudioList,mMediaPlayer);
        rv_audio.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
    }

    @Override
    public void onItemClick(View view, final int position) {
        if (mLastPosition!=-1 && mLastPosition!=position) {
            AudioInfo last_audio = mAudioList.get(mLastPosition);
            last_audio.setProgress(-1);
            mAudioList.set(mLastPosition, last_audio);
            mAdapter.notifyItemChanged(mLastPosition);
        }
        mLastPosition = position;
        mCurrentPosition = position;



        final AudioInfo audio = mAudioList.get(position);
        Log.d(TAG, "onItemClick position="+position+",audio.getPath()="+audio.getPath());
        mTimer.cancel();
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(audio.getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            startTimerForProgressUpdate(position);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void startTimerForProgressUpdate(final int position) {
        mTimer = new Timer(); // 创建一个计时器
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                AudioInfo audio = mAudioList.get(position);
                audio.setProgress(mMediaPlayer.getCurrentPosition());
                mAudioList.set(position, audio);
                mHandler.sendEmptyMessage(position);
                Log.d(TAG, "CurrentPosition="+mMediaPlayer.getCurrentPosition()+",position="+position);
            }
        }, 0, 500);
    }



    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mAdapter.notifyItemChanged(msg.what);
        }
    };
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_pause) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                ((ImageView) v).setImageResource(R.mipmap.running);
            } else {
                mMediaPlayer.start();
                ((ImageView) v).setImageResource(R.mipmap.running);
            }
        } else if (v.getId() == R.id.iv_prev) {
            playPreviousAudio();
        } else if (v.getId() == R.id.iv_next) {
            playNextAudio();
        }
    }


}