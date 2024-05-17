package com.example.artflow.model

class ComplementaryFilter() {
    private val alpha = 0.8f
    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)

        fun applyFilter(sensorData: FloatArray, isGyroscopeData: Boolean): FloatArray {
            if (!isGyroscopeData) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorData[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorData[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorData[2]
                linearAcceleration[0] = sensorData[0] - gravity[0]
                linearAcceleration[1] = sensorData[1] - gravity[1]
                linearAcceleration[2] = sensorData[2] - gravity[2]
            }
            return linearAcceleration
        }
}
