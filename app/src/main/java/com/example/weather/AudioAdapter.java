package com.example.weather;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext; // 声明一个上下文对象
    private List<AudioInfo> mAudioList; // 声明一个音频信息列表
    private MediaPlayer mMediaPlayer; // 声明一个媒体播放器对象

    // 构造函数，接受上下文对象和媒体播放器对象作为参数
    public AudioAdapter(Context context, List<AudioInfo> audio_list, MediaPlayer mediaPlayer) {
        mContext = context;
        mAudioList = audio_list;
        mMediaPlayer = mediaPlayer;
    }

    // 获取列表项的个数
    public int getItemCount() {
        return mAudioList.size();
    }

    // 创建列表项的视图持有者
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup vg, int viewType) {
        // 根据布局文件item_audio.xml生成视图对象
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_audio, vg, false);
        return new ItemHolder(v);
    }

    // 绑定列表项的视图持有者
    public void onBindViewHolder(RecyclerView.ViewHolder vh, final int position) {
        ItemHolder holder = (ItemHolder) vh;
        AudioInfo audio = mAudioList.get(position);
        holder.tv_name.setText(audio.getTitle()); // 显示音频名称
        holder.tv_duration.setText(MediaUtil.formatDuration(audio.getDuration())); // 显示音频时长
        if (audio.getProgress() >= 0) { // 正在播放
            holder.ll_progress.setVisibility(View.VISIBLE);
            holder.pb_audio.setMax(audio.getDuration()); // 设置进度条的最大值，也就是媒体的播放时长
            holder.pb_audio.setMin(0);
            holder.pb_audio.setProgress(audio.getProgress()); // 设置进度条的播放进度，也就是已播放的进度
            holder.tv_progress.setText(MediaUtil.formatDuration(audio.getProgress())); // 显示已播放时长
        } else { // 没在播放
            holder.ll_progress.setVisibility(View.GONE);
        }
        // 列表项的点击事件需要自己实现
        holder.ll_audio.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, position);
            }
        });

        // 设置 ProgressBar 的点击事件
        holder.pb_audio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    // 获取用户点击的位置
                    float touchX = event.getX();
                    float width = ((ProgressBar) v).getWidth();
                    int duration = mAudioList.get(vh.getAdapterPosition()).getDuration();
                    // 计算用户点击的位置对应的播放时间
                    int seekPosition = (int) ((touchX / width) * duration);
                    // 调整 MediaPlayer 的播放进度
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.seekTo(seekPosition);
                    }
                }
                return true;
            }
        });



    }

    // 获取列表项的类型
    public int getItemViewType(int position) {
        return 0;
    }

    // 获取列表项的编号
    public long getItemId(int position) {
        return position;
    }

    // 定义列表项的视图持有者
    public class ItemHolder extends RecyclerView.ViewHolder {
        public LinearLayout ll_audio; // 声明音频列表的线性布局对象
        public TextView tv_name; // 声明音频名称的文本视图对象
        public TextView tv_duration; // 声明总时长的文本视图对象
        public LinearLayout ll_progress; // 声明进度区域的线性布局对象
        public ProgressBar pb_audio; // 声明音频播放的进度条对象
        public TextView tv_progress; // 声明已播放时长的文本视图对象
        public ImageView iv_pause; //暂停播放按钮

        public ItemHolder(View v) {
            super(v);
            ll_audio = v.findViewById(R.id.text_layout);
            tv_name = v.findViewById(R.id.tv_name);
            tv_duration = v.findViewById(R.id.tv_duration);
            ll_progress = v.findViewById(R.id.ll_progress);
            pb_audio = v.findViewById(R.id.pb_audio);
            tv_progress = v.findViewById(R.id.tv_progress);
            iv_pause = v.findViewById(R.id.iv_pause);
        }

    }

    // 声明列表项的点击监听器对象
    private Recycler.OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(Recycler.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


}
