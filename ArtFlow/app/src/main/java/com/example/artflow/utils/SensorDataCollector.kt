package com.example.artflow.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.artflow.view.CanvasView

class SensorDataCollector(private val sensorManager: SensorManager, private var canvasView: CanvasView, private var  lastX:Float,private var lastY : Float) :
    SensorEventListener {

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    fun setLastX(x: Float){
        this.lastX = x
    }
    fun setLastY(y: Float){
        this.lastY = y
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xAcc = event.values[0]
            val yAcc = event.values[1]
            val zAcc = event.values[2]

            // Calcular a magnitude do vetor de aceleração
            val magnitude = Math.sqrt((xAcc * xAcc + yAcc * yAcc + zAcc * zAcc).toDouble())

            // Definir um limiar para detectar movimentos significativos
            val threshold = 4

            if (magnitude > threshold) {
                // Atualizar a posição do desenho com base nos dados do acelerômetro
                val newX = lastX + xAcc
                val newY = lastY + yAcc
                canvasView.drawLineTo(lastX, lastY, newX, newY)
                lastX = newX
                lastY = newY
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }

    fun setListener(canvasView: CanvasView) {
        this.canvasView = canvasView
    }
}
