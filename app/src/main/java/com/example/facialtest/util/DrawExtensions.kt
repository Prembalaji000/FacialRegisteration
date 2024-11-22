package com.pdm.ml_face_detection.new

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Base64


fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
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

@SuppressLint("Recycle")
@Throws(IOException::class)
private fun readBytes(uri: Uri, resolver: ContentResolver): ByteArray {
    val inputStream: InputStream? = resolver.openInputStream(uri)
    val byteBuffer = ByteArrayOutputStream()
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)
    var len = 0
    while (inputStream?.read(buffer).also {
            if (it != null) {
                len = it
            }
        } != -1) {
        byteBuffer.write(buffer, 0, len)
    }
    return byteBuffer.toByteArray()
}

