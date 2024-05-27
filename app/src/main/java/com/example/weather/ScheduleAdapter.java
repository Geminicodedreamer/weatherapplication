package com.example.weather;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;



import java.util.List;

public class ScheduleAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

    private List<SchDataBase> mScheduleList ;
    private Context mContext;

    public ScheduleAdapter(List<SchDataBase> mScheduleList, Context mContext) {
        this.mScheduleList = mScheduleList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mScheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        return mScheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_schedule,null);
            holder.tv_schedule = convertView.findViewById(R.id.item_schedule_tv);
            convertView.setTag(holder); // 将ViewHolder对象保存到convertView的Tag中
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        SchDataBase info = mScheduleList.get(position);
        holder.tv_schedule.setText(info.toString());

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final SchDataBase SchDataBase = mScheduleList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.edit_text_title);
        final EditText editTextDescription = dialogView.findViewById(R.id.edit_text_description);
        final EditText editTextTime = dialogView.findViewById(R.id.edit_text_time);
        final EditText editTextPlace = dialogView.findViewById(R.id.edit_text_place);

        editTextTitle.setText(SchDataBase.getTitle());
        editTextDescription.setText(SchDataBase.getDescription());
        editTextTime.setText(SchDataBase.getTime());
        editTextPlace.setText(SchDataBase.getPlace());
        builder.setTitle("编辑日程");
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                String time = editTextTime.getText().toString().trim();
                String place = editTextPlace.getText().toString().trim();
                String date = SchDataBase.getDate();

                // 更新数据库中的信息
                SchDataBase.setTitle(title);
                SchDataBase.setDescription(description);
                SchDataBase.setTime(time);
                SchDataBase.setPlace(place);

                SchManager.updateSchInfo(SchDataBase);
                mScheduleList = SchManager.querySchInfoByDate(date);
                // 刷新列表
                notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("确认删除");
        builder.setMessage("您确定要删除该项吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 添加删除数据库中数据的操作
                String date = mScheduleList.get(position).getDate();
                List<SchDataBase> infoList = SchManager.querySchInfoByDate(date);
                SchManager.deleteSchInfoBydate(date);
                infoList.remove(position);
                for(int i = 0;i < infoList.size();i ++){
                    SchManager.addSchInfo(infoList.get(i));
                }
                // 用户点击确定，执行删除操作
                mScheduleList.remove(position);
                mScheduleList = SchManager.querySchInfoByDate(date);
                notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击取消，关闭对话框
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true; // 返回true表示消费了长按事件
    }


    public void setScheduleList(List<SchDataBase> newList) {
        mScheduleList.clear();
        mScheduleList.addAll(newList);
    }


    public final class ViewHolder{
        public TextView tv_schedule;


    }
}
