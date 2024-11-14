package com.example.facialtest.UiScreen

import android.content.Context
import androidx.camera.core.ImageCapture
import com.example.facialtest.data.CapturedData

sealed class CameraScreenEvents {
  object OnSwitchCameraClick : CameraScreenEvents()
  data class OnTakePhotoClick(val imageCapture: ImageCapture, val context: Context) : CameraScreenEvents()
  data class EditImage(val capturedData: CapturedData) : CameraScreenEvents()
}
