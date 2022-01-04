package com.ihrsachin.emojifyme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


class Emojifier {

    val SMILING_THRESHOLD: Float = 0.15F
    val EYE_OPEN_THRESHOLD: Float = 0.5F


/********************** Type of Emoji as per facial  ****************************************************************************/
    private enum class Emoji {
        SMILE, FROWN,
        LEFT_WINK, RIGHT_WINK,
        LEFT_WINK_FROWN, RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE, CLOSED_EYE_FROWN
    }


    fun detectFacesAndOverlayEmoji(context: Context, picture: Bitmap?): Bitmap? {

/******************************* FACE DETECTOR ************************************************************************/
        val options = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val mlImage = InputImage.fromBitmap(picture!!, 0)
        val detector = FaceDetection.getClient(options)
/********************************************************************************************************************/



/******** Processing Image & setting emojis to all faces as per mood ************************************************************************/

        var finalBitmap: Bitmap = picture.copy(Bitmap.Config.ARGB_8888, true)
        detector.process(mlImage)
            .addOnCompleteListener { it ->

                // if no face is detected
                if (it.result.size == 0) {
                    Toast.makeText(context,
                        "No Face Detected",
                        Toast.LENGTH_SHORT).show()

                }
                else {
                    for (face in it.result) {
                        val emoji: Emoji? = whichEmoji(face)

                        val emojiBitmap = when (emoji) {
                            Emoji.RIGHT_WINK -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.rightwink)
                            Emoji.LEFT_WINK -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.leftwink)
                            Emoji.SMILE -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.smile)
                            Emoji.CLOSED_EYE_SMILE -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.closed_smile)
                            Emoji.RIGHT_WINK_FROWN -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.rightwinkfrown)
                            Emoji.LEFT_WINK_FROWN -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.rightwinkfrown)
                            Emoji.FROWN -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.frown)
                            Emoji.CLOSED_EYE_FROWN -> BitmapFactory.decodeResource(context.resources,
                                R.drawable.closed_frown)
                            else -> BitmapFactory.decodeResource(context.resources, R.drawable.smile)
                        }
                        if(emojiBitmap!= null) addBitmapToFace(finalBitmap,emojiBitmap,face)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }


        return finalBitmap
    }

    private fun whichEmoji(face: Face): Emoji? {

/************************************** Facial Expression Details ***********************************************************/
        val leftEyeOpenProbability: Float = face.leftEyeOpenProbability!!
        val rightEyeOpenProbability: Float = face.rightEyeOpenProbability!!
        val smilingProbability: Float = face.smilingProbability!!


/*************************** Smiling Case *******************************************************************************/
        if (smilingProbability >= SMILING_THRESHOLD) {
            if (leftEyeOpenProbability >= EYE_OPEN_THRESHOLD && rightEyeOpenProbability < EYE_OPEN_THRESHOLD) {
                return Emoji.RIGHT_WINK
            }
            if (leftEyeOpenProbability < EYE_OPEN_THRESHOLD && rightEyeOpenProbability >= EYE_OPEN_THRESHOLD) {
                return Emoji.LEFT_WINK
            }
            if (leftEyeOpenProbability >= EYE_OPEN_THRESHOLD && rightEyeOpenProbability >= EYE_OPEN_THRESHOLD) {
                return Emoji.SMILE
            }
            if (leftEyeOpenProbability < EYE_OPEN_THRESHOLD && rightEyeOpenProbability < EYE_OPEN_THRESHOLD) {
                return Emoji.CLOSED_EYE_SMILE
            }
        }


/****************************** Not Smiling Case ********************************************************************************/
        if (smilingProbability < SMILING_THRESHOLD) {
            if (leftEyeOpenProbability >= EYE_OPEN_THRESHOLD && rightEyeOpenProbability < EYE_OPEN_THRESHOLD) {
                return Emoji.RIGHT_WINK_FROWN
            }
            if (leftEyeOpenProbability < EYE_OPEN_THRESHOLD && rightEyeOpenProbability >= EYE_OPEN_THRESHOLD) {
                return Emoji.LEFT_WINK_FROWN
            }
            if (leftEyeOpenProbability >= EYE_OPEN_THRESHOLD && rightEyeOpenProbability >= EYE_OPEN_THRESHOLD) {
                return Emoji.FROWN
            }
            if (leftEyeOpenProbability < EYE_OPEN_THRESHOLD && rightEyeOpenProbability < EYE_OPEN_THRESHOLD) {
                return Emoji.CLOSED_EYE_FROWN
            }
        }

        return null
    }

    private fun addBitmapToFace(backgroundBitmap: Bitmap, bitEmoji: Bitmap, face: Face): Bitmap? {

/********************** Main Image as Background ****************************************************************************/
        // crate a mutable bitmap as a copy of original bitmap
        val bitmap = Bitmap.createBitmap(backgroundBitmap.width,
            backgroundBitmap.height,
            backgroundBitmap.config)

        // get position of face
        val emojiPositionX: Float = face.boundingBox.centerX().toFloat()
        val emojiPositionY = face.boundingBox.centerY().toFloat()
/**************************************************************************************************************************/



/************************* Emoji bitmap *******************************************************************************************/
        // scale factor of emojiBitmap
        val scaleFactor = 1

        // determine face size to add emoji on it
        val emojiWidth = face.boundingBox.width() * scaleFactor
        val emojiHeight = face.boundingBox.height() * scaleFactor

        // scale emoji as per face size
//        bitEmoji.width = emojiWidth.toInt()
//        bitEmoji.height = emojiHeight.toInt()
//        val emojiBitmap = Bitmap.createBitmap(emojiWidth.toInt(),
//            emojiHeight.toInt(),
//            bitEmoji.config)
/*************************************************************************************************************************/



/*************** Adding emoji on the top of face ***************************************************************************/
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(backgroundBitmap, 0F, 0F, null)
        canvas.drawBitmap(bitEmoji, emojiPositionX, emojiPositionY, null)
/*************************************************************************************************************************/

        return bitmap
    }
}