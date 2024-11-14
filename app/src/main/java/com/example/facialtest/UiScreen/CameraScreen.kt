package com.example.facialtest.UiScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facialtest.UiScreen.components.BasicButton
import com.example.facialtest.UiScreen.components.MainScreens
import com.example.facialtest.data.CameraDetection
import com.example.facialtest.ml.FaceDetectionAnalyzer
import com.example.facialtest.viewModel.CameraViewModel
import com.example.facialtest.viewModel.FacePosition

@Composable
fun CameraX(
  cameraPermission: Boolean,
  viewModel: CameraViewModel = hiltViewModel(),
  storagePermission: Boolean,
  modifier: Modifier
) {
  val state = viewModel.state
  val localContext = LocalContext.current
  val displayManager = localContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
  val rotation = displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
  val imageCapture = remember {
    ImageCapture.Builder()
      .setTargetRotation(rotation)
      .build()
  }
  var isCameraGranted by remember { mutableStateOf(cameraPermission) }
  var isStorageGranted by remember { mutableStateOf(storagePermission) }
  val launcher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        Log.d("Camera is Granted", "$isCameraGranted")
        isCameraGranted = true
        isStorageGranted = true
      } else {
        Log.d("Camera is Granted", "$isCameraGranted")
      }
    }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    AnimatedVisibility(visible = isCameraGranted) {
      CameraContent(
        modifier = Modifier,
        state.selectedCamera,
        onSwitchClick = { viewModel.onEvent(CameraScreenEvents.OnSwitchCameraClick) },
        onTakePhotoClick = {
          viewModel.onEvent(
            CameraScreenEvents.OnTakePhotoClick(
              imageCapture,
              localContext
            )
          )
        },
        imageCapture = imageCapture,
        context = localContext,
        storagePermission = storagePermission,
        viewModel = viewModel
      )
    }

    BasicButton(modifier = Modifier
      .heightIn(), value = "Open Camera", onClick = {
      if (!isCameraGranted) {
        launcher.launch(Manifest.permission.CAMERA)
      }
    })
  }
}

@OptIn(ExperimentalGetImage::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CameraContent(
  modifier: Modifier = Modifier,
  selectedCamera: CameraSelector,
  imageCapture: ImageCapture,
  onSwitchClick: () -> Unit,
  onTakePhotoClick: () -> Unit,
  context: Context,
  storagePermission: Boolean,
  viewModel: CameraViewModel = hiltViewModel(),
) {
  val uiState by viewModel.cameraViewUiState.collectAsStateWithLifecycle()
  val listItem = viewModel.listItem

  val lifecycleOwner = LocalLifecycleOwner.current

  val isFace = remember {
    mutableStateOf(false)
  }
  val image = CameraDetection(faceDetection = Boolean.equals(true))
  Log.e("isFaceDe","true or false in comp in main 2 = $image, neww = $uiState, and = ${listItem.size}")
  val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

  val positionText = uiState.capturedFace?.facePosition?.name
  val position = uiState.capturedFace?.facePosition
  val previewView = remember { PreviewView(context) }
  val executor = ContextCompat.getMainExecutor(context)
  LaunchedEffect(selectedCamera) {
    Log.d("Camera Selection ", selectedCamera.toString())
    val cameraProvider = cameraProviderFuture.get()

    val preview = Preview.Builder().build().apply {
      setSurfaceProvider(previewView.surfaceProvider)
    }
    val imageAnalysis = ImageAnalysis.Builder()
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .build().apply {
        setAnalyzer(executor, FaceDetectionAnalyzer { listFaces, width, height, imaged ->
          if (listFaces.isNotEmpty()){
            Log.e("edit","${uiState.capturedFace?.facePosition}")
            val face = listFaces[0]
            isFace.value = when(uiState.capturedFace?.facePosition) {
              FacePosition.LEFT -> face.headEulerAngleX in -5.0..5.0 && face.headEulerAngleY in -25.0..-10.0
              FacePosition.RIGHT -> face.headEulerAngleX in -5.0..5.0 && face.headEulerAngleY in 10.0..25.0
              FacePosition.TOP -> face.headEulerAngleX < -10.0 && face.headEulerAngleY in -5.0..5.0
              FacePosition.BOTTOM -> face.headEulerAngleX > 10.0 && face.headEulerAngleY in -5.0..5.0
              FacePosition.STRAIGHT -> face.headEulerAngleX in -5.0..5.0 && face.headEulerAngleY in -5.0..5.0
              else -> false
            }
          } else {
            isFace.value = false
          }
        })
      }
    try {
      cameraProvider.unbindAll()
      cameraProvider.bindToLifecycle(
        lifecycleOwner,
        selectedCamera,
        imageCapture,
        preview,
        imageAnalysis
      )
    } catch (exc: Exception) {
      Log.e("CameraX", "Use case binding failed", exc)
    }
  }
  Box(modifier = Modifier.fillMaxSize()) {

    Scaffold(
      modifier = modifier.fillMaxSize(),

      ) {
      AndroidView(factory = { context ->
        FrameLayout(context).apply {
          layoutParams = ViewGroup.LayoutParams(
            MATCH_PARENT,
            MATCH_PARENT
          )
          addView(previewView)
        }
      })
    }
    MainScreens(
      onTakePhotoClick = onTakePhotoClick,
      storagePermission =  storagePermission,
      isFaceDetected = isFace.value,
      capturedFaces =  listItem,
      positionText = positionText,
      position = position
      )
  }
}