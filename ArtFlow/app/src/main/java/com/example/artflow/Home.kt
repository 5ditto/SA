package com.example.artflow

import android.content.Context
import android.graphics.Color
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class Home : AppCompatActivity() {
    private lateinit var sensorDataCollector: SensorDataCollector
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorDataCollector = SensorDataCollector(sensorManager,this)
    }

    override fun onStart() {
        super.onStart()
        sensorDataCollector.start()
    }

    override fun onStop() {
        super.onStop()
        sensorDataCollector.stop()
    }

    // Função para atualizar a cor do layout com base na inclinação detectada pelo SensorDataCollector
    fun updateLayoutColor(direction: String) {
        val layout = findViewById<View>(R.id.homeid)
        val color = when (direction) {
            "up" -> Color.BLUE
            "down" -> Color.YELLOW
            "left" -> Color.RED
            "right" -> Color.GREEN
            else -> Color.WHITE // Cor padrão
        }
        layout.setBackgroundColor(color)
    }
}