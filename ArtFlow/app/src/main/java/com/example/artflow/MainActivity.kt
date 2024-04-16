package com.example.artflow

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sensorDataCollector: SensorDataCollector
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorDataCollector = SensorDataCollector(sensorManager)
    }

    override fun onStart() {
        super.onStart()
        sensorDataCollector.start()
    }

    override fun onStop() {
        super.onStop()
        sensorDataCollector.stop()
    }

}