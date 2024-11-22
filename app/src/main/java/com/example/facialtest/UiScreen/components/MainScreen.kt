package com.example.facialtest.UiScreen.components

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.facialtest.R
import com.example.facialtest.UiScreen.CameraScreenEvents
import com.example.facialtest.data.CapturedData
import com.example.facialtest.viewModel.CameraViewModel
import com.example.facialtest.viewModel.FacePosition

@Preview
@Composable
fun CameraPreviews(){
    CameraScreen(
        onTakePhotoClick = {},
        isFaceDetected =false,
        positionText = "",
        position = null
    )
}

@Composable
fun CameraScreen(
    onTakePhotoClick: () -> Unit,
    isFaceDetected : Boolean,
    positionText : String?,
    position : FacePosition?
) {
    val commendText =
    when(position) {
        FacePosition.RIGHT -> "Tilt your head gently to the ${positionText?.lowercase()} side"
        FacePosition.LEFT -> "Tilt your head gently to the ${positionText?.lowercase()} side"
        FacePosition.TOP -> "Tilt your head to the ${positionText?.lowercase()} side"
        FacePosition.BOTTOM -> "Tilt your head gently to the ${positionText?.lowercase()} side"
        FacePosition.STRAIGHT -> "Keep your head ${positionText?.lowercase()}"
        else -> "Successfully Captured !"
    }
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ), label = "Blink Animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DrawDashedOval(isFaceDetected)

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            modifier = Modifier.alpha(alpha),
            text = commendText,
            fontSize = 16.sp,
            color = if (commendText == "Successfully Captured !") colorResource(id = R.color.green) else Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DrawDashedOval(isFaceDetected : Boolean) {

    val ovalColor = if (isFaceDetected) Color.Green else Color.Red

    var left by remember { mutableFloatStateOf(0f) }
    var top by remember { mutableFloatStateOf(0f) }
    var right by remember { mutableFloatStateOf(0f) }
    var bottom by remember { mutableFloatStateOf(0f) }

    Log.e("isFaceDe","true or false in comp = $isFaceDetected")

    Canvas(modifier = Modifier
        .size(260.dp, 320.dp)
        .onGloballyPositioned { coordinates ->

            val position = coordinates.positionInRoot()
            val size = coordinates.size

            left = position.x
            top = position.y
            right = left + size.width
            bottom = top + size.height
        }) {
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawOval(
            color = ovalColor,
            size = Size(size.width, size.height),
            style = Stroke(width = 4f, pathEffect = dashEffect)
        )
        drawOval(
            color = Color.Transparent,
            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
            size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
            blendMode = BlendMode.Clear
        )
    }
}

@Composable
fun CircleCard(
    onTakePhotoClick: () -> Unit,
    storagePermission: Boolean,
    isFaceDetected: Boolean
) {
    var isStorageGranted by remember { mutableStateOf(storagePermission) }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isStorageGranted = true
            } else {
                Log.d("Camera is Granted", "$isStorageGranted")
            }
        }
    Card(
        shape = CircleShape,
        border = BorderStroke(1.5.dp, if(isFaceDetected)Color.White else Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .padding(10.dp)
            .size(50.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = {
            if (!isStorageGranted){
                launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            onTakePhotoClick()

        },
        enabled = isFaceDetected,
    ) {}
}

@Preview
@Composable
fun BottomBarPreview(){
    BottomBar(
        storagePermission = true,
        capturedFaces = listOf(),
    )
}

@Composable
fun BottomBar(
    storagePermission: Boolean,
    capturedFaces : List<CapturedData>,
) {
    Column {
        Box(modifier = Modifier.background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            BottomSheet(capturedFaces,storagePermission)
        }
    }
}

@Composable
fun BottomSheet(capturedFaces : List<CapturedData>, storagePermission: Boolean) {
    val listState = rememberLazyListState()
    val targetIndex = capturedFaces.indexOfFirst { it.image == null }

    LaunchedEffect(targetIndex) {
        if (targetIndex >= 3) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            LazyRow (state = listState) {
                items(capturedFaces) { data ->
                    BottomSheetItem(data)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SubmitButton(storagePermission, isEnable = !capturedFaces.all { it.image == null })
        }
    }
}

@Composable
fun BottomSheetItem(
    capturedFaces : CapturedData,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val color = if (capturedFaces.image?.asImageBitmap() != null){
        colorResource(id = R.color.green)
    } else {
        colorResource(id = R.color.light_black)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(96.dp)
                .height(110.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(10.dp))
                .border(
                    BorderStroke(1.dp, color),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    viewModel.onEvent(CameraScreenEvents.EditImage(capturedFaces))
                    Log.e("edit", "${capturedFaces.facePosition}")
                },
            contentAlignment = Alignment.Center
        ) {
            capturedFaces.image?.asImageBitmap()?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Captured Face",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .graphicsLayer {
                            scaleX = -1f
                        },
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.green_tick),
                        contentDescription = null,
                        tint = colorResource(id = R.color.green),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(4.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.edit_icon),
                    tint = colorResource(id = R.color.light_black),
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (capturedFaces.facePosition != null){
            Text(
                modifier = Modifier,
                text = capturedFaces.facePosition.name.lowercase(),
                fontSize = 14.sp,
                color = Color.Black,
            )
        }
    }
}

@Composable
fun SubmitButton(storagePermission: Boolean, isEnable : Boolean) {
    var isStorageGranted by remember { mutableStateOf(storagePermission) }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isStorageGranted = true
            } else {
                Log.d("Camera is Granted", "$isStorageGranted")
            }
        }
    Button(
        onClick = {
            if (!isStorageGranted){
                launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = if (isEnable) { Color(0xFF1E88E5) } else Color(0xFF1E88E5).copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(48.dp),
        enabled = isEnable
    ) {
        Text(text = "Submitted", color = Color.White, fontWeight = FontWeight.Bold)
    }
}