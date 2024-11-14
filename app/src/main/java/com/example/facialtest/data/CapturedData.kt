package com.example.facialtest.data

import android.graphics.Bitmap
import android.net.Uri
import com.example.facialtest.viewModel.FacePosition

data class CameraDetection(
    val lastUpdate : Long = System.currentTimeMillis(),
    val faceDetection : Boolean = false,
    val capturedFace : CapturedData? = null
)



data class CapturedData(
    val facePosition: FacePosition? = null,
    var image: Bitmap? = null,
    var uri : Uri? = null,
    var descrpitor : String? = null
)