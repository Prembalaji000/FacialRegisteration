package com.example.facialtest.UiScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.facialtest.UiScreen.components.BasicButton
import com.example.facialtest.UiScreen.components.MainScreens
import com.example.facialtest.ml.FaceDetectionAnalyzer
import com.example.facialtest.viewModel.CameraViewModel
import com.google.mlkit.vision.face.Face

@Composable
fun CameraX(cameraPermission: Boolean, viewModel: CameraViewModel = hiltViewModel(), storagePermission: Boolean) {
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
        // Request The permission
        Log.d("Camera is Granted", "$isCameraGranted")
      }
    }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {


    AnimatedVisibility(visible = isCameraGranted && isStorageGranted) {
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
  viewModel: CameraViewModel
) {

  val lifecycleOwner = LocalLifecycleOwner.current
  val faces = remember { mutableStateListOf<Face>() }
  val screenWidth = remember { mutableIntStateOf(context.resources.displayMetrics.widthPixels) }
  val screenHeight = remember { mutableIntStateOf(context.resources.displayMetrics.heightPixels) }
  val imageWidth = remember { mutableIntStateOf(0) }
  val imageHeight = remember { mutableIntStateOf(0) }
  var isStorageGranted by remember { mutableStateOf(storagePermission) }
  val launcher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        isStorageGranted = true
      } else {
        // Request The permission
        Log.d("Camera is Granted", "$isStorageGranted")
      }
    }

  val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }


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
        setAnalyzer(executor, FaceDetectionAnalyzer { listFaces, width, height ->
          faces.clear()
          faces.addAll(listFaces)
          imageWidth.value = width
          imageHeight.value = height
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
    MainScreens(onTakePhotoClick = onTakePhotoClick, storagePermission)
  }
}
