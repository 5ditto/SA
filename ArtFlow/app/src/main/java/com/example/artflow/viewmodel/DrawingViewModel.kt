package com.example.artflow.viewmodel

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.ViewModel
import com.example.artflow.repository.DrawingRepository

class DrawingViewModel : ViewModel() {

    private val drawingRepository = DrawingRepository()

    fun sendDrawingToDatabase(draw: ArrayList<Pair<Path, Paint>>, bitmap: Bitmap) {
        drawingRepository.sendDrawingToDatabase(draw,bitmap)
    }
}

