package com.example.artflow.ui

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
import java.io.IOException
import android.graphics.Color
import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import java.nio.file.Files.lines

class CanvasView : View {
    private val paint = Paint()
    private val path = Path()
    private val lines = mutableListOf<Path>()
    private var brushColor: Int = Color.BLACK

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun setStrokeWidth(i : Float){
        this.paint.strokeWidth = i
    }

    private fun init(attrs: AttributeSet?) {
        paint.isAntiAlias = true
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        paint.color = brushColor // Define a cor do pincel
    }


    fun setBrushColor(color: Int){
        brushColor = color
        paint.color = color
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                // nothing to do here
            }
        }

        // Schedules a repaint.
        invalidate()
        return true
    }

    // Clear
    fun clearCanvas(){
        path.reset()
        lines.clear()
        invalidate()
    }

    // Undo
    @RequiresApi(Build.VERSION_CODES.O)
    fun undo(){
        if (lines.size >0)
        {
            lines.removeAt(lines.size - 1);
            invalidate();
        }
        //toast the user
    }

    // Share
    fun shareCanvasDrawing() {
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
