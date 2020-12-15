package com.feel.feel.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.feel.feel.Util
import com.feel.feel.data.UploadVideoRequestBody
import kotlinx.android.synthetic.main.activity_upload.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.HttpException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class UploadActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    private val PICK_VIDEO_CODE = 201

    private var videoUri: Uri? = null
    private var videoPicked = false
    private var descriptionShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.feel.feel.R.layout.activity_upload)

        usernameTextField.text = Util.getUserData().name
        setButtonAction()
        requestPermission()

        uploadButton.isEnabled = true
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        }

    }

    private fun setButtonAction() {
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
        }
        uploadButton.setOnClickListener { if (videoPicked) upload() else pickVideo() }
        usernameTextField.setOnClickListener { toggleDescription() }
    }

    private fun toggleDescription() {
        descriptionShown = !descriptionShown
        if (descriptionShown) {
            uploadFormContainer.visibility = View.VISIBLE
        } else {
            uploadFormContainer.visibility = View.GONE
        }
    }

    private fun pickVideo() {
        val videoPickerIntent =
            Intent().setAction(Intent.ACTION_GET_CONTENT).setType("video/mp4")
        val videoPickerChooserIntent = Intent.createChooser(videoPickerIntent, "Select Video")
        startActivityForResult(videoPickerChooserIntent, PICK_VIDEO_CODE)
    }

    private fun upload() {
        videoView.stopPlayback()
        uploadButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        val videoIS = contentResolver.openInputStream(videoUri!!)
        val targetVideoFile = File(this@UploadActivity.application.filesDir, "videotmp." + contentResolver.getType(videoUri!!)!!.split("/")[1])
        Files.copy(videoIS, targetVideoFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        videoIS?.close()
        val fileBody = targetVideoFile.asRequestBody("video/*".toMediaType())
        val videoRequestBody = MultipartBody.Part.createFormData("file", targetVideoFile.name, fileBody)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val api = Util.getApiClient()
                val videoUploadResponse = api.uploadFile(videoRequestBody)

                val description = descriptionTextField.text.toString()
                val genre = genreTextField.text.toString()

                val authorId = Util.getUserData().id
                val uploadVideoRequestBody = UploadVideoRequestBody(
                    authorId,
                    description,
                    genre,
                    videoUploadResponse.data.thumbnail,
                    videoUploadResponse.data.url
                )

                val response = api.uploadVideo(uploadVideoRequestBody)

                Toast.makeText(this@UploadActivity, "Upload Succes", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: HttpException) {
                Toast.makeText(
                    this@UploadActivity,
                    "Upload Failed, Check you connection",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun updateUploadButtonState() {
        if (videoPicked) {
            uploadButton.text = "UPLOAD"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == PICK_VIDEO_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data.data
            uri?.let {
                videoView.setVideoURI(uri)
                videoView.start()
                videoView.visibility = View.VISIBLE
            }
            videoUri = uri
            videoPicked = true
            playbutton.visibility = View.GONE
            updateUploadButtonState()

        }
    }

    override fun onResume() {
        super.onResume()
        //videoView.start()
    }
}
