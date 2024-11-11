package com.pdm.ml_face_detection.new

import android.content.ContentResolver
import android.content.Context
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Base64


fun DrawScope.drawBounds(topLeft: PointF, size: Size, color: Color, stroke: Float) {
    drawRect(
        color = color,
        size = size,
        topLeft = Offset(topLeft.x, topLeft.y),
        style = Stroke(width = stroke)
    )
}


@RequiresApi(Build.VERSION_CODES.O)
fun Context.fileUriToBase64(uri: Uri): String? {
    var encodedBase64: String? = ""
    try {
        val bytes = readBytes(uri, contentResolver)
        encodedBase64 = String(Base64.getEncoder().encode(bytes))
    } catch (e1: IOException) {
        e1.printStackTrace()
    }
    return encodedBase64
}

@Throws(IOException::class)
private fun readBytes(uri: Uri, resolver: ContentResolver): ByteArray {
    // this dynamically extends to take the bytes you read
    val inputStream: InputStream? = resolver.openInputStream(uri)
    val byteBuffer = ByteArrayOutputStream()

    // this is storage overwritten on each iteration with bytes
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)

    // we need to know how may bytes were read to write them to the
    // byteBuffer
    var len = 0
    while (inputStream?.read(buffer).also {
            if (it != null) {
                len = it
            }
        } != -1) {
        byteBuffer.write(buffer, 0, len)
    }
    // and then we can return your byte array.
    return byteBuffer.toByteArray()
}

