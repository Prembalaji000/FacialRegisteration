package com.example.facialtest.viewModel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.facialtest.UiScreen.CameraScreenEvents
import com.example.facialtest.UiScreen.CameraScreenState
import com.example.facialtest.UiScreen.components.Capturedata
import com.pdm.ml_face_detection.new.CameraFileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

  var state by mutableStateOf(CameraScreenState())

  private val _capturedFaces = MutableStateFlow<List<Bitmap>>(emptyList())
  val capturedFaces: StateFlow<List<Bitmap>> = _capturedFaces

  private val _capturedData = MutableStateFlow(Capturedata(null, emptyList()))
  val capturedData: StateFlow<Capturedata> get() = _capturedData

  private val _imageSavedUri = MutableStateFlow<Uri?>(null)
  val imageSavedUri: StateFlow<Uri?> get() = _imageSavedUri

  fun onEvent(event: CameraScreenEvents) {
    when (event) {
      CameraScreenEvents.OnSwitchCameraClick -> {
        state =
          state.copy(selectedCamera = if (state.selectedCamera == CameraSelector.DEFAULT_FRONT_CAMERA) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA)
      }

      is CameraScreenEvents.OnTakePhotoClick -> {
        CameraFileUtils.takePicture(event.imageCapture, event.context, CameraViewModel())
      }
    }
  }

  fun setImageSavedUri(uri: Uri) {
    val currentData = _capturedData.value
    Log.d("CameraViewModel", "Setting image URI: $uri")
    _capturedData.value = currentData.copy(imageUri = uri)
    Log.d("CameraViewModel", "Image URI set: ${_capturedData.value.imageUri}")
  }

  fun addCapturedFace(faceBitmap: Bitmap) {
    Log.e("CameraViewModel", "Adding captured face: $faceBitmap")
    val updatedFaces = _capturedFaces.value + faceBitmap
    _capturedFaces.value = updatedFaces
    Log.d("ViewModel", "Total captured faces: ${_capturedFaces.value.size}")
    Log.d("ViewModel", "Total captured faces in : ${capturedFaces.value.size}")
  }
}
