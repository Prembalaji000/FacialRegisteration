package com.example.facialtest.UiScreen.components

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import coil.compose.rememberImagePainter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facialtest.R
import com.example.facialtest.viewModel.CameraViewModel

@Preview
@Composable
fun CameraPreviews(){
    NewCameraScreen(onTakePhotoClick = {}, storagePermission = true)
}

@Composable
fun NewCameraScreen(
    onTakePhotoClick: () -> Unit,
    storagePermission: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DrawDashedOval()

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Tilt your head gently to the left side",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        CircleCard(onTakePhotoClick, storagePermission)
    }
}

@Composable
fun DrawDashedOval() {
    Canvas(modifier = Modifier.size(260.dp, 320.dp)) {
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawOval(
            color = Color.Green,
            size = Size(size.width, size.height),
            style = Stroke(width = 4f, pathEffect = dashEffect)
        )
        // Draw an inner transparent oval to "clear" the center
        drawOval(
            color = Color.Transparent,
            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), // Offset slightly inward to match stroke width
            size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()), // Shrink size to match stroke width
            blendMode = BlendMode.Clear
        )
    }
}

@Composable
fun CircleCard(
    onTakePhotoClick: () -> Unit,
    storagePermission: Boolean
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
        border = BorderStroke(1.5.dp, Color.White),
        modifier = Modifier
            .padding(10.dp)
            .size(50.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = {
            if (!isStorageGranted){
                launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            onTakePhotoClick()
        }
    ) {}
}

@Preview
@Composable
fun MainScreenPreview(){
    MainScreens(onTakePhotoClick = {}, storagePermission = true)
}

@Composable
fun MainScreens(onTakePhotoClick: () -> Unit, storagePermission: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        NewCameraScreen(onTakePhotoClick, storagePermission)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            BottomSheet()
        }
    }
}

@Composable
fun BottomSheet() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            //.padding(bottom = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LazyRow {
                items(8) {
                    BottomSheetItem()
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            SubmitButton()
        }
    }
}

@Composable
fun BottomSheetItem() {
    /*val imageSavedUri by remember { mutableStateOf(viewModel.imageSavedUri) }
    val capturedFaces by viewModel.capturedFaces.collectAsState()
    val imageFaces by viewModel.capturedData.collectAsState()
    Log.d("Composable", "Recomposing BottomSheetItem")
    Log.d("Composable", "Captured faces count in composable: ${capturedFaces.size}")
    Log.d("Composable", "Captured faces count in composable: ${imageFaces}")*/
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
            //.horizontalScroll(rememberScrollState())  // Enable horizontal scrolling for many images
    ) {
/*        if (imageFaces.imageUri != null){
            Image(
                painter = rememberImagePainter(data = imageSavedUri),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop)
        } else {
            Text("No image saved", modifier = Modifier.padding(16.dp))
        }*/
       /* if (capturedFaces.isEmpty()) {
            Text("No faces captured", modifier = Modifier.padding(16.dp))
        } else {
            capturedFaces.forEachIndexed { index, faceBitmap ->
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(110.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = faceBitmap.asImageBitmap(),
                        contentDescription = "Captured Face $index",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))  // Space between boxes
            }
        }*/

        Box(
            modifier = Modifier
                .width(96.dp)
                .height(110.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                Icon(
                    painter = painterResource(id = R.drawable.edit_icon),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(12.dp))
                Icon(
                    painter = painterResource(id = R.drawable.green_tick),
                    contentDescription = null,
                    tint = colorResource(id = R.color.green),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun SubmitButton() {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(48.dp)
    ) {
        Text(text = "Submitted", color = Color.White, fontWeight = FontWeight.Bold)
    }
}