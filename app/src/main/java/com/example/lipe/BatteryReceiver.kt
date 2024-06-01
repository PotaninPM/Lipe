package com.example.lipe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BatteryReceiver : BroadcastReceiver() {

    private val batteryLevelLiveData = MutableLiveData<String>()

    fun getBatteryLevel(context: Context): LiveData<String> {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(this, filter)
        return batteryLevelLiveData
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val battery = level * 100 / scale.toFloat()
            batteryLevelLiveData.postValue(battery.toString())
        }
    }
}