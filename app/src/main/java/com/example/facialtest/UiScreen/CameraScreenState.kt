package com.example.facialtest.UiScreen

import androidx.camera.core.CameraSelector

data class CameraScreenState(
  val selectedCamera : CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

)
