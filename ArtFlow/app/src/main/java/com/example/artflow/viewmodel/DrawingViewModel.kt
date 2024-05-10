package com.example.artflow.viewmodel

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class DrawingViewModel : ViewModel() {

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

    fun getColorNameFromHex(color: Int): String {
        val colorName = when (color) {
            Color.BLACK -> "Preto"
            Color.DKGRAY -> "Cinza Escuro"
            Color.GRAY -> "Cinza"
            Color.LTGRAY -> "Cinza Claro"
            Color.WHITE -> "Branco"
            Color.RED -> "Vermelho"
            Color.GREEN -> "Verde"
            Color.BLUE -> "Azul"
            Color.YELLOW -> "Amarelo"
            Color.CYAN -> "Ciano"
            Color.MAGENTA -> "Magenta"
            else -> "#${Integer.toHexString(color).toUpperCase()}"
        }
        return colorName
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    // Enviar para a Base de Dados
    fun sendDrawingToDatabase(draw: ArrayList<Pair<Path, Paint>>, bitmap: Bitmap, rating: Int) {
        val timestamp = System.currentTimeMillis()
        val drawingData = HashMap<String, Any>()

        for ((index, pair) in draw.withIndex()) {
            val path = pair.first
            val paint = pair.second

            if (!path.isEmpty) { // Verifica se o Path não está vazio
                val pathData = pathToString(path)
                val paintData = mapOf(
                    "strokeWidth" to paint.strokeWidth,
                    "color" to getColorNameFromHex(paint.color)
                )

                val pathPaintData = HashMap<String, Any>()
                pathPaintData["path"] = pathData
                pathPaintData["paint"] = paintData

                drawingData["path $index"] = pathPaintData
            }
        }

        val base64Bitmap = bitmapToBase64(bitmap)

        drawingData["Draw File"] = base64Bitmap
        drawingData["rating"] = rating

        val drawingReference = databaseReference.child("id_$timestamp")
        drawingReference.setValue(drawingData).addOnSuccessListener {
            Log.d(ContentValues.TAG, "Desenho enviado com sucesso para o Firebase.")
        }.addOnFailureListener { e ->
            Log.e(ContentValues.TAG, "Erro ao enviar desenho para o Firebase.", e)
        }
    }
}

