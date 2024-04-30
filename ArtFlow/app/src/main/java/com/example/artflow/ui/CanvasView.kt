package com.example.artflow.ui

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CanvasView : View {
    private val paths = ArrayList<Pair<Path, Paint>>()
    private var brushColor: Int = Color.BLACK
    private var strokeWidth = 10f

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

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        paths.last().second.strokeWidth = width
        invalidate()
    }

    private fun init(attrs: AttributeSet?) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        paint.color = brushColor
        paths.add(Pair(Path(), paint))
    }

    fun setBrushColor(color: Int) {
        brushColor = color
        paths.last().second.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                paths.last().first.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                paths.last().first.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                paths.add(Pair(Path(), Paint(paths.last().second)))
            }
        }

        invalidate()
        return true
    }

    fun clearCanvas() {
        paths.clear()
        init(null)
        invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun undo() {
        if (paths.size > 1) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

    fun shareCanvasDrawing() {
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
}