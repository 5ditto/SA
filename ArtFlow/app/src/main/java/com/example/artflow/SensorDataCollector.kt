package com.example.artflow

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class SensorDataCollector(private val sensorManager: SensorManager,private val home: Home) : SensorEventListener {
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

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xAcc = event.values[0]
            val yAcc = event.values[1]
            val zAcc = event.values[2]

            // Calcular a magnitude do vetor de aceleração
            val magnitude = Math.sqrt((xAcc * xAcc + yAcc * yAcc + zAcc * zAcc).toDouble())

            // Calcular a inclinação do dispositivo em relação à horizontal
            val inclination = Math.atan2(yAcc.toDouble(), Math.sqrt((xAcc * xAcc + zAcc * zAcc).toDouble()))

            val limiarMagnitude = 4 // Limiar menor para detectar movimentos
            val limiarInclination = Math.toRadians(15.0) // Limiar menor para a inclinação em radianos

            // Detectar movimento para cima
            if (magnitude > limiarMagnitude && inclination > limiarInclination) {
                home.updateLayoutColor("up") // Atualiza a cor do layout para cima
            }
            // Detectar movimento para baixo
            else if (magnitude > limiarMagnitude && inclination < -limiarInclination) {
                home.updateLayoutColor("down") // Atualiza a cor do layout para baixo
            }
            // Detectar movimento para a esquerda
            else if (magnitude > limiarMagnitude && xAcc < -limiarMagnitude) {
                home.updateLayoutColor("left") // Atualiza a cor do layout para a esquerda
            }
            // Detectar movimento para a direita
            else if (magnitude > limiarMagnitude && xAcc > limiarMagnitude) {
                home.updateLayoutColor("right") // Atualiza a cor do layout para a direita
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }

}