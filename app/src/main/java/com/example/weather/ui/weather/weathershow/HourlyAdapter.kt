package com.example.weather.ui.weather.weathershow

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.logic.model.HourlyForecast
import com.example.weather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class HourlyAdapter(private val hourlyForecastList: List<HourlyForecast>): RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val hourlyTemperatureInfo: TextView = view.findViewById(R.id.hourlyTemperatureInfo)
        val hourlySkyIcon: ImageView = view.findViewById(R.id.hourlySkyIcon)
        val hourlyDateInfo: TextView = view.findViewById(R.id.hourlyDateInfo)
        init {
            // 添加点击事件监听器
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val hourlyForecast = hourlyForecastList[position]
                    val datetime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(hourlyForecast.datetime)
                    val hourlyTemperatureInfoText = hourlyForecast.temVal.toInt()
                    val sky = getSky(hourlyForecast.skyVal)
                    val detailInfo = "时间: $datetime\n温度: $hourlyTemperatureInfoText °C\n天气状况: ${sky.info}"
                    // 创建并显示对话框，展示逐时天气的详细信息，并传递天气图标的资源 ID
                    showDetailDialog(itemView.context, detailInfo, sky.icon)
                }
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forecast_hourly_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return hourlyForecastList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hourlyForecast = hourlyForecastList[position]
        val hourlyTemperatureInfoText = hourlyForecast.temVal.toInt()
        holder.hourlyTemperatureInfo.text = "${hourlyTemperatureInfoText}°"
        val sky = getSky(hourlyForecast.skyVal)
        holder.hourlySkyIcon.setImageResource(sky.icon)
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.hourlyDateInfo.text = simpleDateFormat.format(hourlyForecast.datetime)
    }

    private fun showDetailDialog(context: Context, detailInfo: String, skyIconResId: Int) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_weather_detail, null)

        val detailTextView: TextView = dialogView.findViewById(R.id.detailTextView)
        val iconImageView: ImageView = dialogView.findViewById(R.id.iconImageView)

        detailTextView.text = detailInfo
        iconImageView.setImageResource(skyIconResId)

        builder.setTitle("天气详情")
        builder.setView(dialogView)
        builder.setPositiveButton("确定", null)
        builder.show()
    }


}