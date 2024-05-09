package com.example.artflow.repository

import android.content.ContentValues.TAG
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class DrawingRepository {

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.reference.child("drawings")

    private fun pathToString(path: Path): String {
        val stringBuilder = StringBuilder()
        val points = FloatArray(2)
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length
        val step = 1f

        for (i in 0 until pathLength.toInt() step step.toInt()) {
            pathMeasure.getPosTan(i.toFloat(), points, null)
            stringBuilder.append("${points[0]},${points[1]};")
        }

        return stringBuilder.toString()
    }




    // Enviar para a Base de Dados
    fun sendDrawingToDatabase(draw: ArrayList<Pair<Path, Paint>>) {
        val timestamp = System.currentTimeMillis()
        val drawingData = HashMap<String, Any>()

        for ((index, pair) in draw.withIndex()) {
            val path = pair.first
            val paint = pair.second

            if (!path.isEmpty) { // Verifica se o Path não está vazio
                val pathData = pathToString(path)
                val paintData = mapOf(
                    "strokeWidth" to paint.strokeWidth,
                    "color" to paint.color
                )

                val pathPaintData = HashMap<String, Any>()
                pathPaintData["path"] = pathData
                pathPaintData["paint"] = paintData

                drawingData["path $index"] = pathPaintData
            }
        }

        val drawingReference = databaseReference.child("id: $timestamp")
        drawingReference.child("paths").setValue(drawingData).addOnSuccessListener {
            Log.d(TAG, "Desenho enviado com sucesso para o Firebase.")
        }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao enviar desenho para o Firebase.", e)
            }
    }

}
