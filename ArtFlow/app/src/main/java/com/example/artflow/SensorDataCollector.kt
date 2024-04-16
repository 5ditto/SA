package com.example.artflow


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SensorDataCollector(private val sensorManager: SensorManager) : SensorEventListener {
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var databaseReference: DatabaseReference

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorType = event.sensor.type
        val sensorValues = event.values.joinToString(separator = ", ")

        val sensorData = hashMapOf(
            "sensor_type" to sensorType,
            "sensor_values" to sensorValues
        )

        databaseReference.child("sensor_data").push().setValue(sensorData)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }
}