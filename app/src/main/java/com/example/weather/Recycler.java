package com.example.weather;

import android.view.View;

public class Recycler {

    // 定义一个循环视图列表项的点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
