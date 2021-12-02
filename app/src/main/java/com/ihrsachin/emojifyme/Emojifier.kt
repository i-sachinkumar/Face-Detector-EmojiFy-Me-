package com.ihrsachin.emojifyme

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class Emojifier {

    fun detectFaces(context: Context, picture: Bitmap?){

        val realTdetector = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val mlImage = InputImage.fromBitmap(picture!!, 0)
        val detector = FaceDetection.getClient()

        val result = detector.process(mlImage).addOnCompleteListener{
            Toast.makeText(context, "${it.result.size}", Toast.LENGTH_SHORT).show()
        }
    }
}