package com.example.artflow


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

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
        val sensorName = event.sensor.name
        val sensorValues = event.values.joinToString(separator = ", ")

        val sensorData = hashMapOf(
            "sensor_type" to sensorType,
            "sensor_name" to sensorName,
            "sensor_values" to sensorValues
        )

        databaseReference.push().setValue(sensorData)
            .addOnSuccessListener {
                Log.d("Message","Sensor data added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Message", "Error adding sensor data", e)
            }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }

}