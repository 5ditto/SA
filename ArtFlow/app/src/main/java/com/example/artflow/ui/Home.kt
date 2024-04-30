package com.example.artflow.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.artflow.R
import com.example.artflow.utils.sensor.SensorDataCollector
import yuku.ambilwarna.AmbilWarnaDialog

private val INITIAL_SIZE = 10f // Tamanho inicial da bolinha
private val MAX_SIZE = 20f // Tamanho máximo do pincel
private val MIN_SIZE = 1f // Tamanho mínimo do pincel

class Home : AppCompatActivity() {
    private var initialColor = Color.BLACK
    private lateinit var canvasView: CanvasView
    private lateinit var sensorDataCollector: SensorDataCollector
    private var isSeekBarVisible = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        canvasView = findViewById<CanvasView>(R.id.canvasView)

        val shareButton = findViewById<ImageView>(R.id.btn_share)
        shareButton.setOnClickListener {
            canvasView.shareDrawing()
        }

        val deleteButton = findViewById<ImageView>(R.id.btn_delete)
        deleteButton.setOnClickListener {
            canvasView.clear()
        }

        val undoButton = findViewById<ImageView>(R.id.btn_undo)
        undoButton.setOnClickListener {
            canvasView.undo()
        }

        val redoButton = findViewById<ImageView>(R.id.btn_redo)
        redoButton.setOnClickListener {
            canvasView.redo()
        }

        val colorButton = findViewById<ImageView>(R.id.btn_color)
        colorButton.setOnClickListener {
            openColorPicker(canvasView, colorButton)
        }

        val widthButton = findViewById<ImageView>(R.id.btn_width)
        widthButton.setOnClickListener {
            changeWidth(widthButton, canvasView)
        }

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorDataCollector = SensorDataCollector(sensorManager, this)
    }

    override fun onStart() {
        super.onStart()
        sensorDataCollector.start()
    }

    override fun onStop() {
        super.onStop()
        sensorDataCollector.stop()
    }

    fun openColorPicker(canvasView: CanvasView, btn_color: ImageView) {
        val colorPicker =
            AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    canvasView.setColor(color)
                    val roundedDrawable = GradientDrawable()
                    roundedDrawable.shape = GradientDrawable.OVAL
                    roundedDrawable.setColor(color)
                    btn_color.background = roundedDrawable
                }
            })
        colorPicker.show()
    }

    @SuppressLint("WrongViewCast")
    fun changeWidth(button: ImageView, canvasView: CanvasView) {
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val brushSizeIndicator = findViewById<ImageView>(R.id.brushSizeIndicator)

        button.setOnClickListener {
            if (isSeekBarVisible) {
                seekBar.visibility = View.GONE
                brushSizeIndicator.visibility = View.GONE
                isSeekBarVisible = false
            } else {
                seekBar.visibility = View.VISIBLE
                isSeekBarVisible = true
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newSize =
                    (MIN_SIZE + progress * (MAX_SIZE - MIN_SIZE) / seekBar?.max!!).toFloat()

                // Atualizar a escala da bolinha
                brushSizeIndicator.scaleX = newSize / INITIAL_SIZE
                brushSizeIndicator.scaleY = newSize / INITIAL_SIZE

                canvasView.setStrokeWidth(progress.toFloat())
                brushSizeIndicator.visibility = View.VISIBLE


                val layoutParams = brushSizeIndicator.layoutParams as ConstraintLayout.LayoutParams
                if (seekBar != null) {
                    layoutParams.leftMargin =
                        seekBar.left + seekBar.thumb.bounds.left - brushSizeIndicator.width / 2
                }
                brushSizeIndicator.layoutParams = layoutParams
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                brushSizeIndicator.visibility = View.GONE
            }
        })
    }
}
