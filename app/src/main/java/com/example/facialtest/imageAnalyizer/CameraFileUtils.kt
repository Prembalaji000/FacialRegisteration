package com.pdm.ml_face_detection.new

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.impl.utils.MatrixExt.postRotate
import androidx.exifinterface.media.ExifInterface
import com.example.facialtest.viewModel.CameraViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Locale
import java.util.concurrent.Executors

object CameraFileUtils {

  fun takePicture(
    imageCapture: ImageCapture,
    context: Context,
    viewModel: CameraViewModel
    ) {
    val options = FaceDetectorOptions.Builder()
      .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
      .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
      .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
      .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
      .enableTracking()
      .build()
    val detector = FaceDetection.getClient(options)
    val ioExecutor = Executors.newCachedThreadPool()
    val photoFile = File(
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
      SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpeg"
    )

    Log.d("CameraLool", photoFile.name.toString())
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    Log.e("cameraAuth","$outputFileOptions")
    imageCapture.takePicture(outputFileOptions, ioExecutor,
      object : ImageCapture.OnImageSavedCallback {
        override fun onError(error: ImageCaptureException) {
          Log.e("Saving Capture Error", error.message.toString())
          Log.e("Saving Capture Error", error.cause.toString())
          Log.e("Saving Capture Error", error.imageCaptureError.toString())
        }


        @RequiresApi(Build.VERSION_CODES.O)
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
          val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
          viewModel.setImageSavedUri(savedUri)
          val image: InputImage

          try {
            image = InputImage.fromFilePath(context, savedUri)
            image.mediaImage
            detector.process(image)
              .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                  val faces = task.result
                  val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                  val rotatedBitmap = rotateImageIfRequired(photoFile.absolutePath, bitmap)
                  val mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true)

                  if (faces.isNotEmpty()) {
                    val face = faces[0]
                    val boundingBox = face.boundingBox

                    val cropX = boundingBox.left.coerceAtLeast(0)
                    val cropY = boundingBox.top.coerceAtLeast(0)
                    val cropWidth = boundingBox.width().coerceAtMost(mutableBitmap.width - cropX)
                    val cropHeight = boundingBox.height().coerceAtMost(mutableBitmap.height - cropY)

                    if (cropWidth > 0 && cropHeight > 0) {
                      val faceBitmap = Bitmap.createBitmap(mutableBitmap, cropX, cropY, cropWidth, cropHeight)
                      viewModel.addCapturedFace(faceBitmap)
                    } else {
                      Log.d("CameraLool", "Invalid crop dimensions: width=$cropWidth, height=$cropHeight")
                    }

                    /*val faceBitmap = Bitmap.createBitmap(mutableBitmap, cropX, cropY, cropWidth, cropHeight)

                    val croppedFaceFile = File(photoFile.parent, "face_${photoFile.name}")
                    FileOutputStream(croppedFaceFile).use { out ->
                      faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }

                    // Add the cropped face bitmap to the ViewModel
                    Log.e("CameraViewModel", "Adding captured face: $faceBitmap")
                    viewModel.addCapturedFace(faceBitmap)*/
                  } else {
                    Log.d("CameraLool", "No face detected in the image")
                  }
                }
              }
          } catch (e: Exception) {
            Log.e("Error", "$e")
          }
        }

      }
    )
  }



  private fun rotateImageIfRequired(filePath: String, bitmap: Bitmap): Bitmap {
    val exif = ExifInterface(filePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
    return when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
      else -> bitmap
    }
  }

  private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }
}

