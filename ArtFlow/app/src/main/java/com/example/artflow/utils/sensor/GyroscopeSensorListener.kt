package com.example.artflow.utils.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.artflow.data.GyroscopeData

class GyroscopeSensorListener: SensorEventListener {
    companion object {
        private const val TAG: String = "GyroscopeSensorListener"
    }

    private lateinit var sensorManager: SensorManager

    fun setSensorManager(sensorMan: SensorManager) {
        sensorManager = sensorMan
    }

    override fun onSensorChanged(event: SensorEvent) {
        GyroscopeData.valueX = event.values[0]
        GyroscopeData.valueY = event.values[1]
        GyroscopeData.valueZ = event.values[2]
        GyroscopeData.accuracy = event.accuracy
        Log.d(
            TAG,
            "[SENSOR] - X=${GyroscopeData.valueX}, Y=${GyroscopeData.valueY}, Z=${GyroscopeData.valueZ}")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}