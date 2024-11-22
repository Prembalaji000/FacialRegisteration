package com.example.facialtest.viewModel

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.facialtest.UiScreen.CameraScreenEvents
import com.example.facialtest.UiScreen.CameraScreenState
import com.example.facialtest.data.CameraDetection
import com.example.facialtest.data.CapturedData
import com.pdm.ml_face_detection.new.CameraFileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

  var state by mutableStateOf(CameraScreenState())
  var isFace by mutableStateOf(false)
  val listItem : MutableList<CapturedData> = mutableListOf()

  init {
    listItem.add(CapturedData(facePosition = FacePosition.STRAIGHT))
    listItem.add(CapturedData(facePosition = FacePosition.LEFT))
    listItem.add(CapturedData(facePosition = FacePosition.RIGHT))
    listItem.add(CapturedData(facePosition = FacePosition.TOP))
    listItem.add(CapturedData(facePosition = FacePosition.BOTTOM))
  }

  private val cameraViewModeState = MutableStateFlow(CameraDetection(faceDetection = false, capturedFace = listItem.find { it.image == null}))
  val cameraViewUiState = cameraViewModeState.asStateFlow()

  fun onEvent(event: CameraScreenEvents) {
    when (event) {
      CameraScreenEvents.OnSwitchCameraClick -> {
        state =
          state.copy(selectedCamera = if (state.selectedCamera == CameraSelector.DEFAULT_FRONT_CAMERA) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA)
      }

      is CameraScreenEvents.OnTakePhotoClick -> {
        CameraFileUtils.takePicture(event.imageCapture, event.context, this)
      }

      is CameraScreenEvents.EditImage -> {
        Log.e("editNew","${event.capturedData.facePosition}")
        cameraViewModeState.update {
          it.copy(
            lastUpdate = System.currentTimeMillis(),
            capturedFace =  event.capturedData)
        }
      }
    }
  }

  fun addCapturedFace(image: Bitmap, faceId : String, cropImage: Bitmap) {
    val tempData = cameraViewModeState.value.capturedFace
    listItem.find { it.facePosition == tempData?.facePosition }?.apply {
        this.image = image
        this.faceId = faceId
        this.cropImage = cropImage
      Log.e("faceIsVM","in $cropImage, $image")
      when(facePosition){
        FacePosition.RIGHT -> this.right = image
        FacePosition.LEFT -> this.left = image
        FacePosition.STRAIGHT -> this.straight = image
        FacePosition.TOP -> this.top = image
        FacePosition.BOTTOM -> this.bottom = image
        else -> this.image = image
      }
      Log.e("PositionBitmap","straight = ${this.straight}, left = ${this.left}, right = ${this.right}, top = ${this.top}, bottom = ${this.bottom}")
    }
    cameraViewModeState.update { it.copy (
      lastUpdate = System.currentTimeMillis(),
      capturedFace = listItem.find { it.image == null },
      isCaptureFace = false,
    ) }
  }
}



enum class FacePosition {
  STRAIGHT,
  RIGHT,
  LEFT,
  TOP,
  BOTTOM
}



