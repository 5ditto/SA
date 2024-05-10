package com.example.artflow.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RatingBar
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.artflow.R
import com.example.artflow.utils.SensorDataCollector
import com.example.artflow.viewmodel.DrawingViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CanvasView : View {
    private val paths = ArrayList<Pair<Path, Paint>>()
    private var brushColor: Int = Color.parseColor("#EA4800")
    private var strokeWidth = 10f
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private lateinit var lastPoints : ArrayList<Pair<Float,Float>>
    private var isDrawingEnabled = false
    private var isAddPoint = false

    constructor(context: Context) : super(context) {
        init(null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    fun startDrawing(){
        this.isDrawingEnabled = true
    }

    fun stopDrawing(){
        this.isDrawingEnabled = false
    }

    fun getLastX(): Float {
        return this.lastX
    }

    fun getLastY(): Float{
        return this.lastY
    }
    fun getDraw(): Boolean {
        return  this.isDrawingEnabled
    }

    private fun init(attrs: AttributeSet?) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.parseColor("#EA4800")
        paths.add(Pair(Path(), paint))
        isDrawingEnabled = false
        lastPoints = ArrayList()
        lastPoints.add(Pair(500f,500f))
    }

    // Desenhar (util para o caso de se carregar desenhos antigos - a implementar)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }
    }

    // Mudar cor do pincel
    fun setBrushColor(color: Int) {
        brushColor = color
        paths.last().second.color = color
        invalidate()
    }

    // Mudar espessura do pincel
    fun setStrokeWidth(width: Number) {
        strokeWidth = width as Float
        paths.last().second.strokeWidth = width
        invalidate()
    }

    // Desenhar uma linha
    fun drawLineTo(startX: Float, startY: Float, endX: Float, endY: Float) {
        val lastPath = paths.last().first
        lastPath.moveTo(startX, startY)
        lastPath.lineTo(endX, endY)
    }

    // Limpar ecra
    fun clear() {
        paths.clear()
        init(null)
        invalidate()
    }

    // Desfazer último traço - Erro de coordenadas RESOLVER
    @RequiresApi(Build.VERSION_CODES.O)
    fun undo() {
        if (paths.size > 1) {
            paths.removeLast()
            if (lastPoints.size > 1) {
                val lastPoint = lastPoints.last()
                lastX = lastPoint.first
                lastY = lastPoint.second
                lastPoints.removeAt(lastPoints.size - 1)
            }
            invalidate()
        } else {
            clear()
        }
    }





    // Partilhar o desenho
    fun shareDrawing(drawingViewModel: DrawingViewModel) {
        val ratingDialog = AlertDialog.Builder(context)
            .setView(R.layout.rating_dialog)
            .setTitle("Rate Your Drawing Experience")
            .setPositiveButton("Share") { dialog, _ ->
                val ratingBar = (dialog as AlertDialog).findViewById<RatingBar>(R.id.ratingBar)
                val rating = ratingBar?.rating?.toInt() ?: 0
                saveAndShareDrawing(drawingViewModel, rating)
            }
            .setNegativeButton("Cancel", null)
            .create()

        ratingDialog.show()
    }

    private fun saveAndShareDrawing(drawingViewModel: DrawingViewModel, rating: Int) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)

        val file = saveBitmapToFile(bitmap)

        drawingViewModel.sendDrawingToDatabase(paths, bitmap, rating)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
    // Guardar Bitmap
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file =
            File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "artflow_temp.jpg")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    fun addNewPoint(x : Float, y :Float){
        lastPoints.add(Pair(x,y))
    }


    // Ao tocar no ecrã desenha
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startDrawing()
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isDrawingEnabled) {
                    paths.add(Pair(Path(), Paint(paths.last().second)))
                    stopDrawing()
                    isAddPoint = true
                }
            }
        }

        invalidate()
        return true
    }

    fun getIsAddPoint(): Boolean {
        return this.isAddPoint
    }
    fun stopAddPoint(){
        this.isAddPoint = false
    }

    private fun isNightMode(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
}
