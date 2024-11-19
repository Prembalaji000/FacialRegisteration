package com.example.facialtest.data

import android.graphics.Bitmap
import android.net.Uri
import com.example.facialtest.viewModel.FacePosition

data class CameraDetection(
    val lastUpdate : Long = System.currentTimeMillis(),
    val faceDetection : Boolean = false,
    val isCaptureFace : Boolean = false,
    val capturedFace : CapturedData? = null,
)



data class CapturedData(
    val facePosition : FacePosition? = null,
    var image : Bitmap? = null,
    var cropImage : Bitmap? = null,
    var uri : Uri? = null,
    var descriptor : String? = null,
    var faceId : String? = null
)