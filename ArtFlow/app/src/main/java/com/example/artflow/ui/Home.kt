package com.example.artflow.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.SensorManager
import android.graphics.drawable.GradientDrawable
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
    private lateinit var sensorDataCollector: SensorDataCollector
    private var isSeekBarVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val canvasView = findViewById<CanvasView>(R.id.canvasView)
        val shareButton = findViewById<ImageView>(R.id.btn_share)
        val deleteButton = findViewById<ImageView>(R.id.btn_delete)
        val undoButton = findViewById<ImageView>(R.id.btn_undo)
        val colorButton = findViewById<ImageView>(R.id.btn_color)
        val widthButton = findViewById<ImageView>(R.id.btn_width)
        shareButton.setOnClickListener {
            canvasView.shareCanvasDrawing()
        }
        deleteButton.setOnClickListener {
            canvasView.clearCanvas()
        }
        undoButton.setOnClickListener {
            canvasView.undo()
        }
        colorButton.setOnClickListener {
            openColorPicker(canvasView,colorButton)
        }
        widthButton.setOnClickListener {
            changeWidth(widthButton,canvasView)
        }


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
            "upright" -> Color.BLACK
            "upleft" -> Color.CYAN // Escolha a cor desejada para a diagonal superior esquerda
            "downright" -> Color.MAGENTA // Escolha a cor desejada para a diagonal inferior direita
            "downleft" -> Color.GRAY // 
            else -> Color.WHITE // Cor padrão
        }
        layout.setBackgroundColor(color)
    }

    fun openColorPicker(canvasView : CanvasView, btn_color : ImageView){
        val colorPicker = AmbilWarnaDialog(this, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
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

    @SuppressLint("WrongViewCast")
    fun changeWidth(button : ImageView, canvasView: CanvasView) {
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val brushSizeIndicator = findViewById<ImageView>(R.id.brushSizeIndicator)

        button.setOnClickListener {
            if (isSeekBarVisible){
                seekBar.visibility = View.GONE
                isSeekBarVisible = false
            } else {
                seekBar.visibility = View.VISIBLE
                isSeekBarVisible = true
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newSize = (MIN_SIZE + progress * (MAX_SIZE - MIN_SIZE) / seekBar?.max!!).toFloat()

                // Atualizar a escala da bolinha
                brushSizeIndicator.scaleX = newSize / INITIAL_SIZE
                brushSizeIndicator.scaleY = newSize / INITIAL_SIZE

                canvasView.setStrokeWidth(progress.toFloat())
                brushSizeIndicator.visibility = View.VISIBLE


                val layoutParams = brushSizeIndicator.layoutParams as ConstraintLayout.LayoutParams
                if (seekBar != null) {
                    layoutParams.leftMargin = seekBar.left + seekBar.thumb.bounds.left - brushSizeIndicator.width / 2
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