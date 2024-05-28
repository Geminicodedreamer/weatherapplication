package com.example.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
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
    private List<AudioInfo> mAudioList = new ArrayList<>();
    private Uri mAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private ImageView iv_pause, iv_prev, iv_next;
    private String[] mAudioColumn = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
    };
    private AudioAdapter mAdapter;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
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

        iv_pause.setOnClickListener(this);
        iv_prev.setOnClickListener(this);
        iv_next.setOnClickListener(this);

        mMediaPlayer.setOnCompletionListener(mp -> playNextAudio());

        loadAudioList();
        showAudioList();
    }

    private void checkAndRequestAudioPermission() {
        int audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO);
        }

        if (audioPermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, AUDIO_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, AUDIO_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Permission already granted
            loadAudioList();
            showAudioList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已授予音频文件权限", Toast.LENGTH_SHORT).show();
                loadAudioList();
                showAudioList();
            } else {
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
            mLastPosition--;

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
            mLastPosition++;

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

        // 定义查询条件，这里查询的是Music目录中的音频文件
        String selection = MediaStore.Audio.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%Music%"};

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mAudioColumn,
                selection,
                selectionArgs,
                "date_modified desc");

        if (cursor != null) {
            Log.d(TAG, "cursor is not null");

            while (cursor.moveToNext()) {
                AudioInfo audio = new AudioInfo();
                audio.setId(cursor.getLong(0));
                audio.setTitle(cursor.getString(1));
                audio.setDuration(cursor.getInt(2));
                audio.setSize(cursor.getLong(3));
                audio.setPath(cursor.getString(4));
                Log.d(TAG, audio.getTitle() + " " + audio.getDuration() + " " + audio.getSize() + " " + audio.getPath());
                if (!FileUtil.checkFileUri(this, audio.getPath())) {
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
        mAdapter = new AudioAdapter(this, mAudioList, mMediaPlayer);
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
    public void onItemClick(View view, int position) {
        if (mLastPosition != -1 && mLastPosition != position) {
            AudioInfo last_audio = mAudioList.get(mLastPosition);
            last_audio.setProgress(-1);
            mAudioList.set(mLastPosition, last_audio);
            mAdapter.notifyItemChanged(mLastPosition);
        }
        mLastPosition = position;
        mCurrentPosition = position;

        AudioInfo audio = mAudioList.get(position);
        Log.d(TAG, "onItemClick position=" + position + ",audio.getPath()=" + audio.getPath());
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
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                AudioInfo audio = mAudioList.get(position);
                audio.setProgress(mMediaPlayer.getCurrentPosition());
                mAudioList.set(position, audio);
                mHandler.sendEmptyMessage(position);
                Log.d(TAG, "CurrentPosition=" + mMediaPlayer.getCurrentPosition() + ",position=" + position);
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
