package com.example.artflow.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.artflow.R
import com.example.artflow.utils.SensorDataCollector
import com.example.artflow.viewmodel.DrawingViewModel
import yuku.ambilwarna.AmbilWarnaDialog


class Home : AppCompatActivity(){
    private lateinit var sensorDataCollector: SensorDataCollector
    private lateinit var canvasView: CanvasView
    private lateinit var drawingViewModel: DrawingViewModel
    private var initialColor = Color.BLACK
    private var initial_x = 500f
    private var initial_y = 500f

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        canvasView = findViewById(R.id.canvasView)

        sensorDataCollector = SensorDataCollector(
            getSystemService(Context.SENSOR_SERVICE) as SensorManager,
            canvasView,
            initial_x,
            initial_y
        )

        drawingViewModel = ViewModelProvider(this).get(DrawingViewModel::class.java)

        val shareButton = findViewById<ImageView>(R.id.btn_share)
        shareButton.setOnClickListener {
            onPause()
            canvasView.shareDrawing()
            drawingViewModel.sendDrawingToDatabase(canvasView.getArrayList())
            onResume()
        }


        val deleteButton = findViewById<ImageView>(R.id.btn_delete)
        deleteButton.setOnClickListener {
            onPause()
            canvasView.clear()
            sensorDataCollector.setLastX(initial_x)
            sensorDataCollector.setLastY(initial_y)
            onResume()
        }

        val undoButton = findViewById<ImageView>(R.id.btn_undo)
        undoButton.setOnClickListener {
            onPause()
            canvasView.undo()
            onResume()
        }

        val colorButton = findViewById<ImageView>(R.id.btn_color)
        colorButton.setOnClickListener {
            onPause()
            openColorPicker(canvasView, colorButton)
            onResume()
        }


        val drawButton = findViewById<Button>(R.id.button_draw)
        drawButton.setOnClickListener{
            fun onStartDrawing() {
                canvasView.startDrawing()
            }

            fun onStopDrawing() {
                canvasView.stopDrawing()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        sensorDataCollector.stop()
        canvasView.stopDrawing()
    }

    override fun onResume() {
        super.onResume()
        sensorDataCollector.start()
        canvasView.startDrawing()
    }

    fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignorar
    }

    // Mudar a cor do traço
    private fun openColorPicker(canvasView: CanvasView, btn_color: ImageView) {
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
                    btn_color.background = roundedDrawable
                }
            })
        colorPicker.show()
    }

}
