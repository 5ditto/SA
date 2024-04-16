package com.example.artflow

import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log

class AccelerometerSensorListener: SensorEventListener {
    companion object{
        private const val TAG: String = "AccelerometerSensorListener"
    }

    private lateinit var sensorManager: SensorManager

    fun setSensorManager(sensorMan: SensorManager){
        sensorManager = sensorMan
    }

    override fun onSensorChanged(event: SensorEvent){
        AccelerometerData.valueX = event.values[0]
        AccelerometerData.valueY = event.values[1]
        AccelerometerData.valueZ = event.values[2]
        AccelerometerData.accuracy = event.accuracy
        //sensorManager.unregisterListener(this)
        Log.d(TAG,
            "[SENSOR] - X=${AccelerometerData.valueX}, Y=${AccelerometerData.valueY}, Z=${AccelerometerData.valueZ}")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int){}
}
