package com.example.weather

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class WeatherApplication: Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val TOKEN = "vrZU1uW5u6BVMxg9"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}