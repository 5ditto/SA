package com.example.artflow.ui

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
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
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import java.io.IOException
import java.io.OutputStream

class CanvasView(context: Context, attrs: AttributeSet?): View(context,attrs) {
    private val paths = mutableListOf<Pair<Path, Paint>>()
    private val undoPaths = mutableListOf<Pair<Path, Paint>>()
    private var mPaintColor = Color.BLACK
    private var mPaintStroke = 10f
    private lateinit var mPaint: Paint
    private lateinit var mPath: Path
    private var isDrawing = false

    private val TAG = "CanvasView" // TAG para os logs

    init{
        setupPaint()
    }

    private fun setupPaint(){
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.color = mPaintColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mPaintStroke

        mPath = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }
    }

    fun setColor(color: Int) {
        Log.d(TAG, "Changed width to $color")
        mPaintColor = color
        mPaint.color = mPaintColor
    }

    fun setStrokeWidth(width: Float) {
        Log.d(TAG, "Changed width to $width")
        mPaintStroke = width
        mPaint.strokeWidth = mPaintStroke
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        val xPos = event.x
        val yPos = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDrawing = true
                mPath = Path().apply {
                    moveTo(xPos, yPos)
                }
                paths.add(Pair(mPath,Paint(mPaint)))
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDrawing) {
                    mPath.lineTo(xPos, yPos)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                isDrawing = false
                invalidate()
            }
            else -> return false
        }

        return true
    }

    // Clear
    fun clear() {
        paths.clear()
        undoPaths.clear()
        invalidate()
    }

    // Undo
    fun undo() {
        if (paths.isNotEmpty()) {
            undoPaths.add(paths.last())
            paths.remove(paths.last())
            invalidate()
        }
    }

    fun redo() {
        if (undoPaths.isNotEmpty()) {
            undoPaths.removeAt(undoPaths.size - 1)
            invalidate()
        }
    }

    fun shareDrawing() {
        val bitmap = createBitmapFromView(this)
        saveBitmap(bitmap)
        shareBitmap(bitmap)
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val width = if (view.width > 0) view.width else 1
        val height = if (view.height > 0) view.height else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    private fun saveBitmap(bitmap: Bitmap) {
        val fileName = "drawing_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            // Add bitmap to gallery
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, fileName, null)
            Toast.makeText(context, "Drawing saved to Gallery", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save drawing", Toast.LENGTH_SHORT).show()
        } finally {
            outputStream?.close()
        }
    }

    private fun shareBitmap(bitmap: Bitmap) {
        val uri = bitmapToUri(bitmap)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }
        val chooser = Intent.createChooser(shareIntent, "Share Drawing")
        context.startActivity(chooser)
    }

    private fun bitmapToUri(bitmap: Bitmap): String {
        val fileName = "drawing_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file.absolutePath
    }
}
