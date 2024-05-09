package com.example.artflow.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import com.example.artflow.R
import com.example.artflow.repository.DrawingRepository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()  // É para esconder a barra de cima
        window.statusBarColor = Color.parseColor("#EA4800") // É para tudo fica com a cor de fundo

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        },3000) // 3000 milisegundos = 3 segundos que corresponde ao tempo que a splash screen aparece
    }
}
