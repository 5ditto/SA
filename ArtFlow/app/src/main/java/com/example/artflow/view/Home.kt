package com.example.artflow.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.artflow.R
import com.example.artflow.model.SensorDataCollector
import com.example.artflow.viewmodel.DrawingViewModel
import yuku.ambilwarna.AmbilWarnaDialog


class Home : AppCompatActivity(){
    private lateinit var sensorDataCollector: SensorDataCollector
    private lateinit var canvasView: CanvasView
    private lateinit var drawingViewModel: DrawingViewModel
    private var initialColor = Color.BLACK
    private var initialX = 500f
    private var initialY = 1000f

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        canvasView = findViewById(R.id.canvasView)
        canvasView.stopDrawing()

        sensorDataCollector = SensorDataCollector(
            getSystemService(Context.SENSOR_SERVICE) as SensorManager,
            canvasView,
            initialX,
            initialY
        )

        drawingViewModel = ViewModelProvider(this)[DrawingViewModel::class.java]

        val shareButton = findViewById<ImageView>(R.id.btn_share)
        shareButton.setOnClickListener {
            onPause()
            canvasView.shareDrawing(drawingViewModel)
            onResume()
        }


        val deleteButton = findViewById<ImageView>(R.id.btn_delete)
        deleteButton.setOnClickListener {
            onPause()
            canvasView.clear()
            sensorDataCollector.setLastX(initialX)
            sensorDataCollector.setLastY(initialY)
            onResume()
            canvasView.stopDrawing()
        }

        val undoButton = findViewById<ImageView>(R.id.btn_undo)
        undoButton.setOnClickListener {
            onPause()
            canvasView.undo()
            sensorDataCollector.updateLastPoint(canvasView.getLastX(), canvasView.getLastY())
            onResume()
        }


        val colorButton = findViewById<ImageView>(R.id.btn_color)
        colorButton.setOnClickListener {
            onPause()
            openColorPicker(canvasView, colorButton)
            onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorDataCollector.stop()
    }

    override fun onResume() {
        super.onResume()
        sensorDataCollector.start()
    }


    // Mudar a cor do traço
    private fun openColorPicker(canvasView: CanvasView, btncolor: ImageView) {
        val colorPicker =
            AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    // Nada a fazer se o usuário cancelar a seleção de cor
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    canvasView.setBrushColor(color)
                    val roundedDrawable = GradientDrawable()
                    roundedDrawable.shape = GradientDrawable.OVAL
                    roundedDrawable.setColor(color)
                    btncolor.background = roundedDrawable
                }
            })
        colorPicker.show()
    }

}
