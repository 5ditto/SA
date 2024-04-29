package com.example.artflow.utils.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.artflow.ui.CanvasView
import com.example.artflow.ui.Home
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SensorDataCollector(private val sensorManager: SensorManager, private val home: Home, private val canvasView: CanvasView,) : SensorEventListener {
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var databaseReference: DatabaseReference
    private var madgwickFilter: MadgwickFilter = MadgwickFilter()


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

        //val NS2S = 1.0f / 1000000000.0f // Nano para segundo

        // Inicialização do filtro de Madgwick (movido para fora do método para evitar recriação a cada evento)
        //val madgwickFilter = MadgwickFilter()


        val sensorData = hashMapOf(
            "sensor_type" to sensorType,
            "sensor_name" to sensorName,
            "sensor_values" to sensorValues
        )

        // databaseReference.push().setValue(sensorData)
        //    .addOnSuccessListener {
        //        Log.d("Message","Sensor data added successfully")
        //    }
        //    .addOnFailureListener { e ->
        //        Log.e("Message", "Error adding sensor data", e)
        //    }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xAcc = event.values[0].toDouble()
            val yAcc = event.values[1].toDouble()
            val zAcc = event.values[2].toDouble()

            /*
            madgwickFilter.updateIMU(0.0, 0.0, 0.0, gx, gy, gz, 0.0)

            // Obter a orientação do dispositivo do filtro de Madgwick
            val quaternion = madgwickFilter.getQuaternion()
            val roll = Math.atan2(2.0 * (quaternion[0] * quaternion[1] + quaternion[2] * quaternion[3]),
                1.0 - 2.0 * (quaternion[1] * quaternion[1] + quaternion[2] * quaternion[2]))
            val pitch = Math.asin(2.0 * (quaternion[0] * quaternion[2] - quaternion[3] * quaternion[1]))
            val yaw = Math.atan2(2.0 * (quaternion[0] * quaternion[3] + quaternion[1] * quaternion[2]),
                1.0 - 2.0 * (quaternion[2] * quaternion[2] + quaternion[3] * quaternion[3]))

            //home.draw(pitch, roll, yaw)
            // Use os valores de roll, pitch e yaw para detectar o movimento do dispositivo
            */
            // Calcular a magnitude do vetor de aceleração
            val magnitude = Math.sqrt((xAcc * xAcc + yAcc * yAcc + zAcc * zAcc).toDouble())

            // Calcular a inclinação do dispositivo em relação à horizontal e vertical
            val inclinationX = Math.atan2(xAcc.toDouble(), Math.sqrt((yAcc * yAcc + zAcc * zAcc).toDouble()))
            val inclinationY = Math.atan2(yAcc.toDouble(), Math.sqrt((xAcc * xAcc + zAcc * zAcc).toDouble()))

            val limiarMagnitude = 4 // Limiar menor para detectar movimentos
            val limiarInclination = Math.toRadians(15.0) // Limiar menor para a inclinação em radianos

            // Detectar movimento para cima
            if (magnitude > limiarMagnitude && inclinationY > limiarInclination) {
                home.updateLayoutColor("up") // Atualiza a cor do layout para cima
            }
            // Detectar movimento para baixo
            else if (magnitude > limiarMagnitude && inclinationY < -limiarInclination) {
                home.updateLayoutColor("down") // Atualiza a cor do layout para baixo
            }
            // Detectar movimento para a esquerda
            else if (magnitude > limiarMagnitude && inclinationX < -limiarInclination) {
                home.updateLayoutColor("left") // Atualiza a cor do layout para a esquerda
            }
            // Detectar movimento para a direita
            else if (magnitude > limiarMagnitude && inclinationX > limiarInclination) {
                home.updateLayoutColor("right") // Atualiza a cor do layout para a direita
            }
            // Detectar movimento na diagonal superior direita
            else if (magnitude > limiarMagnitude && inclinationY > limiarInclination && inclinationX > limiarInclination) {
                home.updateLayoutColor("upright") // Atualiza a cor do layout para a diagonal superior direita
            }
            // Detectar movimento na diagonal superior esquerda
            else if (magnitude > limiarMagnitude && inclinationY > limiarInclination && inclinationX < -limiarInclination) {
                home.updateLayoutColor("upleft") // Atualiza a cor do layout para a diagonal superior esquerda
            }
            // Detectar movimento na diagonal inferior direita
            else if (magnitude > limiarMagnitude && inclinationY < -limiarInclination && inclinationX > limiarInclination) {
                home.updateLayoutColor("downright") // Atualiza a cor do layout para a diagonal inferior direita
            }
            // Detectar movimento na diagonal inferior esquerda
            else if (magnitude > limiarMagnitude && inclinationY < -limiarInclination && inclinationX < -limiarInclination) {
                home.updateLayoutColor("downleft") // Atualiza a cor do layout para a diagonal inferior esquerda
            }

        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }

    private fun detectDeviceMovement(roll: Double, pitch: Double, yaw: Double) {
        val limiar = 15.0
        if (roll > limiar) {
            home.updateLayoutColor("right")
        } else if (roll < -limiar) {
            home.updateLayoutColor("left")
        } else if (pitch > limiar) {
            home.updateLayoutColor("up")
        } else if (pitch < -limiar) {
            home.updateLayoutColor("down")
        } else {
            // Não há movimento detectado
        }
    }

    companion object {
        private const val NS2S = 1.0f / 1000000000.0f
    }

    private var lastTimestamp: Long = 0

}