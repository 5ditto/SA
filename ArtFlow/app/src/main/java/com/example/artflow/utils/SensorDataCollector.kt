package com.example.artflow.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt
import com.example.artflow.view.CanvasView

class SensorDataCollector(
    private val sensorManager: SensorManager,
    private var motionView: CanvasView,
    private var lastX: Float,
    private var lastY: Float
) : SensorEventListener {

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var filter: ComplementaryFilter
    private var scaleFactor = 20.0f
    private var movementThreshold = 4.0f
    private var maxStrokeWidth = 20.0f
    private var minStrokeWidth = 2.0f

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        filter = ComplementaryFilter()
    }

    fun setLastX(x: Float) {
        this.lastX = x
    }

    fun setLastY(y: Float) {
        this.lastY = y
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
        if (motionView.getDraw()) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val isGyroscopeData = false
                    val filteredData = filter.applyFilter(event.values, isGyroscopeData)
                    val xAcc = filteredData[0]
                    val yAcc = filteredData[1]
                    val magnitude = sqrt((xAcc * xAcc + yAcc * yAcc).toDouble())
                    if (magnitude > movementThreshold) {
                        val newX = lastX - xAcc * scaleFactor
                        val newY = lastY - yAcc * scaleFactor
                        motionView.drawLineTo(lastX, lastY, newX, newY)
                        lastX = newX
                        lastY = newY
                    }
                }
                Sensor.TYPE_GYROSCOPE -> {
                    val xGyro = event.values[0]
                    val yGyro = event.values[1]
                    val zGyro = event.values[2]
                    val gyroMagnitude = sqrt((xGyro * xGyro + yGyro * yGyro + zGyro * zGyro).toDouble())
                    val strokeWidth = (maxStrokeWidth - (gyroMagnitude * scaleFactor)).toFloat()
                    motionView.setStrokeWidth(
                        if (strokeWidth < minStrokeWidth) minStrokeWidth else strokeWidth
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }
}
