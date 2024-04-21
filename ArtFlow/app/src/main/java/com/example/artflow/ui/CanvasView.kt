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

class CanvasView : View {
    private val paint = Paint()
    private val path = Path()
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

    // Desenhar
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawPath(path,paint)
    }


    // Clear
    fun clearCanvas(){
        path.reset()
        invalidate()
    }

    // Undo
    fun undo(){
        if(!path.isEmpty){
            path.reset()
            invalidate()
        }
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
