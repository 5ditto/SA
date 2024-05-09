package com.example.artflow.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.artflow.viewmodel.DrawingViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CanvasView : View {
    private val paths = ArrayList<Pair<Path, Paint>>()
    private var brushColor: Int = Color.BLACK
    private var strokeWidth = 10f
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var isDrawingEnabled = false


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

    fun getDraw(): Boolean {
        return  this.isDrawingEnabled
    }

    private fun init(attrs: AttributeSet?) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        paint.color = brushColor
        paths.add(Pair(Path(), paint))
    }

    fun setLastX(x: Float){
        this.lastX = x
    }

    fun setLastY(y: Float) {
        this.lastY = y
    }

    fun getArrayList(): ArrayList<Pair<Path, Paint>> {
        return this.paths
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
        isDrawingEnabled = false
        invalidate()
    }

    // Desfazer último traço - Erro de coordenadas RESOLVER
    @RequiresApi(Build.VERSION_CODES.O)
    fun undo() {
        if (paths.size > 1) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
        else{
            clear()
        }
    }

    // Partilhar o desenho
    fun shareDrawing() {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)

        val file = saveBitmapToFile(bitmap)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar via"))
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

    fun onStartDrawing(){
        isDrawingEnabled = true
    }


    // Ao tocar no ecrã desenha
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (x >= 0 && x <= width && y >= 0 && y <= height) {
                    paths.add(Pair(Path(), Paint(paths.last().second)))
                    paths.last().first.moveTo(x, y)
                    lastX = x
                    lastY = y
                    isDrawingEnabled = true
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                paths.add(Pair(Path(), Paint(paths.last().second)))
                isDrawingEnabled = false
            }
        }

        invalidate()
        return true
    }

}
