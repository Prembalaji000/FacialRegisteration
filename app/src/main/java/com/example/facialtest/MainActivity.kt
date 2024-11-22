package com.example.facialtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import com.example.facialtest.UiScreen.CameraX
import com.example.facialtest.permission.getCameraPermission
import com.example.facialtest.permission.getStoragePermission
import com.example.facialtest.viewModel.CameraViewModel
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel : CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cameraPermission = getCameraPermission(this)
        val storagePermission = getStoragePermission(this)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding->
                CameraX(cameraPermission = cameraPermission, storagePermission = storagePermission, viewModel = viewModel, modifier = Modifier.padding(innerPadding) )
            }
        }
    }
}