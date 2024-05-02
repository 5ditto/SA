package com.example.artflow.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class DrawingViewModel : ViewModel() {
    private lateinit var databaseReference: DatabaseReference

    init {
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    fun saveDrawingToDatabase(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val ref = databaseReference.child("drawings").push()
        ref.setValue(data).addOnSuccessListener {
            Log.d("DrawingViewModel", "Drawing saved successfully")
        }.addOnFailureListener { e ->
            Log.e("DrawingViewModel", "Error saving drawing", e)
        }
    }
}

