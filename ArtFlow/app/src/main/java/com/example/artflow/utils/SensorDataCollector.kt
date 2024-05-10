package com.example.artflow.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.artflow.view.CanvasView
import kotlin.math.sqrt

class SensorDataCollector(
    private val sensorManager: SensorManager,
    private var canvasView: CanvasView,
    private var lastX: Float,
    private var lastY: Float
) : SensorEventListener {

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var accData = FloatArray(3)
    private var gyroData = FloatArray(3)
    private var filter : ComplementaryFilter
    private var scaleFactor = 10.0f
    private var movementThreshold = 0.10f
    private var maxStrokeWidth = 40.0f
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
        if (canvasView.getDraw()) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accData = event.values.clone()
                    val filteredData = filter.applyFilter(accData, false)

                    val xAcc = -filteredData[0] // Invertido para corresponder à orientação do dispositivo
                    val yAcc = filteredData[1]
                    val magnitude = sqrt((xAcc * xAcc + yAcc * yAcc).toDouble()).toFloat()

                    if (magnitude > movementThreshold) {
                        val newX = lastX + xAcc * scaleFactor
                        val newY = lastY + yAcc * scaleFactor
                        canvasView.drawLineTo(lastX, lastY, newX, newY)
                        lastX = newX
                        lastY = newY
                    }
                }
                Sensor.TYPE_GYROSCOPE -> {
                    gyroData = event.values.clone()
                    val filteredGyroData = filter.applyFilter(gyroData, true)

                    val xGyro = filteredGyroData[0]
                    val yGyro = filteredGyroData[1]
                    val zGyro = filteredGyroData[2]
                    val gyroMagnitude = sqrt((xGyro * xGyro + yGyro * yGyro + zGyro * zGyro).toDouble()).toFloat()
                    val strokeWidth = (maxStrokeWidth - (gyroMagnitude * scaleFactor))
                    canvasView.setStrokeWidth(
                        if (strokeWidth < minStrokeWidth) minStrokeWidth else strokeWidth
                    )
                }
            }
        }
        if (canvasView.getIsAddPoint()) {
            canvasView.addNewPoint(lastX, lastY)
            canvasView.stopAddPoint()
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }

    fun updateLastPoint(x: Float, y: Float) {
        this.lastX = x
        this.lastY = y
    }
}
