package com.example.facialtest.UiScreen.components

import android.graphics.Bitmap
import android.net.Uri

data class Capturedata (
    val imageUri : Uri?,
    val faceBitmap : List<Bitmap>,
)