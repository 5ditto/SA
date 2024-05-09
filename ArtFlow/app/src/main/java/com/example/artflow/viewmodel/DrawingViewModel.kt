package com.example.artflow.viewmodel

import android.graphics.Paint
import androidx.lifecycle.ViewModel
import com.example.artflow.repository.DrawingRepository

class DrawingViewModel : ViewModel() {

    private val drawingRepository = DrawingRepository()

    fun sendDrawingToDatabase(draw: ArrayList<Pair<android.graphics.Path, Paint>>) {
        drawingRepository.sendDrawingToDatabase(draw)
    }
}

