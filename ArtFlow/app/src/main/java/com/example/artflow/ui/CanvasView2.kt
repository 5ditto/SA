package com.example.artflow.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.ArrayList

class CanvasView2(context: Context, attrs : AttributeSet) : View(context,attrs){
    private val paths  =ArrayList<Pair<Path,Paint>>()
    private var currentPaint = Paint()
    private var currentPath = Path()

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

    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas)
        for((path,paint) in paths){
            canvas.drawPath(path,paint)
        }
        canvas.drawPath(currentPath,currentPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                currentPath.reset()
                currentPath.moveTo(touchX,touchY)
                return true
            }
            MotionEvent.ACTION_MOVE -> currentPath.lineTo(touchX, touchY)
            MotionEvent.ACTION_UP -> {
                paths.add(Pair(Path(currentPath), Paint(currentPaint)))
                currentPath.reset()
            }
            else -> return false
        }

        invalidate()
        return true
    }

    fun undo() {
        if (paths.size > 0) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

    fun setColor(color: Int) {
        currentPaint.color = color
    }

    fun setStrokeWidth(width: Float) {
        currentPaint.strokeWidth = width
    }

}