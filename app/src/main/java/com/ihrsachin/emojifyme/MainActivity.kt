package com.ihrsachin.emojifyme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException



class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_STORAGE_PERMISSION = 1

    private val FILE_PROVIDER_AUTHORITY = "com.ihrsachin.fileprovider"

    private var mImageView: ImageView? = null

    private var mEmojifyButton: Button? = null
    private var mShareFab: FloatingActionButton? = null
    private var mSaveFab: FloatingActionButton? = null
    private var mClearFab: FloatingActionButton? = null

    private var mTitleTextView: TextView? = null

    private var mTempPhotoPath: String? = null

    private var mResultsBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Bind the views
        // Bind the views
        mImageView = findViewById<ImageView>(R.id.image_view)
        mEmojifyButton = findViewById<Button>(R.id.emojify_button)
        mShareFab = findViewById<FloatingActionButton>(R.id.share_button)
        mSaveFab = findViewById<FloatingActionButton>(R.id.save_button)
        mClearFab = findViewById<FloatingActionButton>(R.id.clear_button)
        mTitleTextView = findViewById<TextView>(R.id.title_text_view)
    }

    /**
     * OnClick method for "Emojify Me!" Button. Launches the camera app.
     *
     * @param view The emojify me button.
     */
    fun emojifyMe(view: View?) {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION)
        } else {
            // Launch the camera if the permission exists
            launchCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Called when you request permission to read and write to external storage
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // If you get permission, launch the camera
                    launchCamera()
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    private fun launchCamera() {

        // Create the capture image intent
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the temporary File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = BitmapUtils().createTempImageFile(this)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.absolutePath

                // Get the content URI for the image file
                val photoURI = FileProvider.getUriForFile(this,
                    FILE_PROVIDER_AUTHORITY,
                    photoFile)

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage()
        } else {

            // Otherwise, delete the temporary image file
            BitmapUtils().deleteImageFile(this, mTempPhotoPath)
        }
    }

    /**
     * Method for processing the captured image and setting it to the TextView.
     */
    private fun processAndSetImage() {

        // Toggle Visibility of the views
        mEmojifyButton!!.visibility = View.GONE
        mTitleTextView!!.visibility = View.GONE
        mSaveFab!!.visibility = View.VISIBLE
        mShareFab!!.visibility = View.VISIBLE
        mClearFab!!.visibility = View.VISIBLE

        // Resample the saved image to fit the ImageView
        mResultsBitmap = BitmapUtils().resamplePic(this, mTempPhotoPath)

        // Set the new bitmap to the ImageView
        mImageView!!.setImageBitmap(Emojifier().detectFacesAndOverlayEmoji(this,mResultsBitmap))
    }


    /**
     * OnClick method for the save button.
     *
     * @param view The save button.
     */
    fun saveMe(view: View?) {
        // Delete the temporary image file
        BitmapUtils().deleteImageFile(this, mTempPhotoPath)

        // Save the image
        BitmapUtils().saveImage(this, mResultsBitmap!!)
    }

    /**
     * OnClick method for the share button, saves and shares the new bitmap.
     *
     * @param view The share button.
     */
    fun shareMe(view: View?) {
        //delete temp file
        BitmapUtils().deleteImageFile(this, mTempPhotoPath)

        // Save the image
        val savedImagePath = BitmapUtils().saveImage(this, mResultsBitmap!!)

        //share the save file
        BitmapUtils().shareImage(this, savedImagePath)
    }

    /**
     * OnClick for the clear button, resets the app to original state.
     *
     * @param view The clear button.
     */

    public fun clearImage(view: View) {
        mImageView!!.setImageResource(0)
        mEmojifyButton!!.visibility = View.VISIBLE
        mTitleTextView!!.visibility = View.VISIBLE
        mShareFab!!.visibility = View.GONE
        mSaveFab!!.visibility = View.GONE
        mClearFab!!.visibility = View.GONE

        // Delete the temporary image file

        // Delete the temporary image file
        BitmapUtils().deleteImageFile(this, mTempPhotoPath)
    }
}