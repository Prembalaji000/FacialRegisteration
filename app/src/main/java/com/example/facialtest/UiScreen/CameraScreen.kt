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
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facialtest.UiScreen.components.BasicButton
import com.example.facialtest.UiScreen.components.BottomBar
import com.example.facialtest.UiScreen.components.CameraScreen
import com.example.facialtest.data.CapturedData
import com.example.facialtest.ml.FaceDetectionAnalyzer
import com.example.facialtest.viewModel.CameraViewModel
import com.example.facialtest.viewModel.FacePosition
import com.google.mlkit.vision.face.FaceLandmark
import com.pdm.ml_face_detection.new.CameraFileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraX(
    cameraPermission: Boolean,
    viewModel: CameraViewModel = hiltViewModel(),
    storagePermission: Boolean,
    modifier: Modifier,
) {
    val state = viewModel.state
    val localContext = LocalContext.current
    val displayManager = localContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    val rotation =
        displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
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
        modifier = modifier.fillMaxSize(),
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
            if (!isCameraGranted ) {
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
    var isFaceCapture by remember { mutableStateOf(uiState.isCaptureFace) }
    val faceIds by remember { mutableStateOf(listItem[0].faceId) }
    var lastCapturedPosition by remember { mutableStateOf<FacePosition?>(null) }
    Log.e("faceIsVM","isFaceDetected, $faceIds")
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val positionText = uiState.capturedFace?.facePosition?.name
    val position = uiState.capturedFace?.facePosition
    val previewView = remember { PreviewView(context) }
    val executor = ContextCompat.getMainExecutor(context)
    val scope = rememberCoroutineScope()
    LaunchedEffect(selectedCamera) {
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(executor, FaceDetectionAnalyzer { listFaces, width, height, imaged ->
                    if (listFaces.size > 1) {
                        Toast.makeText(context,"Multiple faces detected",Toast.LENGTH_SHORT).show()
                        Log.d("FaceDetection", "Multiple faces detected. Skipping capture.")
                        return@FaceDetectionAnalyzer
                    }
                    if (listFaces.isNotEmpty() && !isFaceCapture) {
                        val face = listFaces[0]
                        val faceId = face.trackingId.toString()

                        val leftEyeOpen = face.leftEyeOpenProbability ?: 0.0f
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 0.0f
                        val areEyesOpen = leftEyeOpen > 0.8f && rightEyeOpen > 0.8f

                        val markAllLandmarks = face.getLandmark(FaceLandmark.LEFT_EAR) != null &&
                                face.getLandmark(FaceLandmark.RIGHT_EAR) != null &&
                                face.getLandmark(FaceLandmark.LEFT_EYE) != null &&
                                face.getLandmark(FaceLandmark.RIGHT_EYE) != null &&
                                face.getLandmark(FaceLandmark.LEFT_CHEEK) != null &&
                                face.getLandmark(FaceLandmark.RIGHT_CHEEK) != null &&
                                face.getLandmark(FaceLandmark.MOUTH_BOTTOM) != null &&
                                face.getLandmark(FaceLandmark.MOUTH_LEFT) != null &&
                                face.getLandmark(FaceLandmark.MOUTH_RIGHT) != null &&
                                face.getLandmark(FaceLandmark.NOSE_BASE) != null

                        val positionStatus = when (uiState.capturedFace?.facePosition) {
                            FacePosition.RIGHT -> face.headEulerAngleX in -5.0..5.0 && face.headEulerAngleY in -25.0..-10.0
                            FacePosition.LEFT -> face.headEulerAngleX in -5.0..5.0 && face.headEulerAngleY in 10.0..25.0
                            FacePosition.BOTTOM -> face.headEulerAngleX < -10.0 && face.headEulerAngleY in -5.0..5.0
                            FacePosition.TOP -> face.headEulerAngleX > 10.0 && face.headEulerAngleY in -5.0..5.0
                            FacePosition.STRAIGHT -> face.headEulerAngleX in -5.0..5.0 && face.headEulerAngleY in -5.0..5.0
                            else -> false
                        }

                        if (markAllLandmarks && positionStatus && lastCapturedPosition != uiState.capturedFace?.facePosition) {
                            isFaceCapture = true
                            lastCapturedPosition = uiState.capturedFace?.facePosition
                            val trackingIds = listItem[0].faceId
                            Log.e("faced123","${trackingIds == faceId}")
                            scope.launch {
                                if (trackingIds == null || trackingIds != faceId){
                                    CameraFileUtils.takePicture(imageCapture, context, viewModel)
                                    delay(2000)
                                    isFaceCapture = false
                                } else {
                                    Toast.makeText(context,"Face Not Matched",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        isFace.value = positionStatus && markAllLandmarks
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


    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = {
            Box(
                modifier = Modifier
                .fillMaxSize()
                .padding(it)
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
                CameraScreen(
                    onTakePhotoClick = onTakePhotoClick,
                    isFaceDetected = isFace.value,
                    positionText = positionText,
                    position = position
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp, start = 4.dp, end = 4.dp)
            ) {
                BottomBar(
                    storagePermission =  storagePermission,
                    capturedFaces =  listItem,
                )
            }
        }
    )
}
