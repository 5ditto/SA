package com.example.artflow.utils.sensor

import kotlin.math.sqrt

class MadgwickFilter(beta: Double = 0.1) {
    private var beta: Double = beta
    private var q0: Double = 1.0
    private var q1: Double = 0.0
    private var q2: Double = 0.0
    private var q3: Double = 0.0

    fun updateIMU(gx: Double, gy: Double, gz: Double, ax: Double, ay: Double, az: Double, dt: Double) {
        val qDot1 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz)
        val qDot2 = 0.5 * (q0 * gx + q2 * gz - q3 * gy)
        val qDot3 = 0.5 * (q0 * gy - q1 * gz + q3 * gx)
        val qDot4 = 0.5 * (q0 * gz + q1 * gy - q2 * gx)

        val norm = sqrt(ax * ax + ay * ay + az * az)
        if (norm > 0.0) {
            val invNorm = 1.0 / norm
            val axn = ax * invNorm
            val ayn = ay * invNorm
            val azn = az * invNorm

            val _2q0 = 2 * q0
            val _2q1 = 2 * q1
            val _2q2 = 2 * q2
            val _2q3 = 2 * q3
            val _4q0 = 4 * q0
            val _4q1 = 4 * q1
            val _4q2 = 4 * q2
            val _8q1 = 8 * q1
            val _8q2 = 8 * q2
            val q0q0 = q0 * q0
            val q1q1 = q1 * q1
            val q2q2 = q2 * q2
            val q3q3 = q3 * q3

            val s0 = _4q0 * q2q2 + _2q2 * axn + _4q0 * q1q1 - _2q1 * ayn
            val s1 = _4q1 * q3q3 - _2q3 * axn + 4 * q0q0 * q1 - _2q0 * ayn - _4q1 + _8q1 * q1q1 + _8q1 * q2q2 + _4q1 * azn
            val s2 = 4 * q0q0 * q2 + _2q0 * axn + _4q2 * q3q3 - _2q3 * ayn - _4q2 + _8q2 * q1q1 + _8q2 * q2q2 + _4q2 * azn
            val s3 = 4 * q1q1 * q3 - _2q1 * axn + 4 * q2q2 * q3 - _2q2 * ayn

            val invs = 1.0 / sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3)
            val halfdt = 0.5 * dt

            // Compute rate of change of quaternion
            val qDot1Imu = (s1 * q3 - s0 * q2) * invs
            val qDot2Imu = (s2 * q0 + s3 * q1) * invs
            val qDot3Imu = (s2 * q1 - s3 * q0) * invs
            val qDot4Imu = (s0 * q0 + s1 * q1) * invs

            // Integrate to yield quaternion
            q0 -= halfdt * (qDot1Imu - beta * q0q0 * qDot1Imu)
            q1 -= halfdt * (qDot2Imu - beta * q1q1 * qDot2Imu)
            q2 -= halfdt * (qDot3Imu - beta * q2q2 * qDot3Imu)
            q3 -= halfdt * (qDot4Imu - beta * q3q3 * qDot4Imu)

            // Normalize quaternion
            val normq = sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3)
            val invNormq = 1.0 / normq
            q0 *= invNormq
            q1 *= invNormq
            q2 *= invNormq
            q3 *= invNormq
        }
    }

    fun getQuaternion(): Array<Double> {
        return arrayOf(q0, q1, q2, q3)
    }
}