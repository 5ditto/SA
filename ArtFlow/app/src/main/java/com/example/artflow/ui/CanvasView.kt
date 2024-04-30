package com.example.artflow.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.Paint
import android.os.Environment
import android.util.AttributeSet
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color
import android.view.MotionEvent
import java.io.IOException

class CanvasView(context: Context, attrs: AttributeSet?): View(context,attrs) {
    private val paths  = ArrayList<Pair<Path, Paint>>()
    private lateinit var currentPath: Path
    private lateinit var currentPaint: Paint
    private var isBitmapReady = false

    init{
        setupPaint(Color.BLACK, 10f)
    }

    private fun setupPaint(color: Int, width : Float){
        currentPaint = Paint()
        currentPaint.isAntiAlias = true
        currentPaint.color = color
        currentPaint.style = Paint.Style.STROKE
        currentPaint.strokeJoin = Paint.Join.ROUND
        currentPaint.strokeWidth = width
    }

    fun setColor(color: Int) {
        currentPaint.color = color
    }

    fun setStrokeWidth(width: Float) {
        currentPaint.strokeWidth = width
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }
        isBitmapReady = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath.moveTo(touchX, touchY)
                paths.add(Pair(currentPath, Paint(currentPaint)))
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                // No caso de ACTION_UP, não precisamos fazer nada
            }
        }
        invalidate()
        return true
    }

    // Clear
    fun clear() {
        paths.clear()
        invalidate()
    }

    // Undo
    fun undo() {
        if (paths.size > 0) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

    // Share
    fun shareDrawing() {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)

        val file = saveBitmapToFile(bitmap)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            val uri = FileProvider.getUriForFile(context,context.packageName+ ".provider",file)
            putExtra(Intent.EXTRA_STREAM,uri)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar via"))
    }
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "artflow_temp.jpg")
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
}
