package com.example.firebasevideo

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddVideosActivity : AppCompatActivity() {

    lateinit var actioBar : ActionBar
    private lateinit var btnUpload : Button
    private lateinit var btnPickVideo : FloatingActionButton
    private lateinit var vvVideoView: VideoView
    private lateinit var ETTitle : TextView
    //constans to pick video
    private val VIDEO_PICK_GALLERY_CODE = 100
    private val VIDEO_PICK_CAMERA_CODE = 101
    //array for camera request permissions
    private val CAMERA_REQUEST_CODE = 102

    //constant to camera request permissions
    private lateinit var cameraPermission: Array<String>

    //progress bar
    private lateinit var progressDialog: ProgressDialog

    private var videoUri : Uri? = null //uri picked video
    private var title : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_videos)

        btnUpload = findViewById(R.id.btnUploadVideo)
        btnPickVideo = findViewById(R.id.btnPickVideo)
        ETTitle = findViewById(R.id.etTitle)
        vvVideoView = findViewById(R.id.videoView)

        actioBar = supportActionBar!!
        actioBar.title = "Add New Videos"
        actioBar.setDisplayHomeAsUpEnabled(true)
        actioBar.setDisplayShowHomeEnabled(true)

        cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

       // init progressbar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Uploading Video...")
        progressDialog.setCanceledOnTouchOutside(false)

        //HANDLE CLICK
        btnUpload.setOnClickListener {
            //get title
            title = ETTitle.text.toString().trim()
            if (TextUtils.isEmpty(title)){
                //no title entered
                Toast.makeText(this,"Title is required",Toast.LENGTH_SHORT).show()
            } else if (videoUri == null) {
                //video is not picked
                Toast.makeText(this,"Pick video first",Toast.LENGTH_SHORT).show()
            } else {
                //title enterd, video picked, so now upload video
                uploadVideoFirebase()
            }
        }
        //handle click,pick video
        btnPickVideo.setOnClickListener {
            videoPickDialog()
        }

    }

    private fun uploadVideoFirebase() {
        //show progress
        progressDialog.show()

        //timeStamp
        val timestamp = ""+System.currentTimeMillis()

        //file path and name in firebase storage
        val filePathAndName = "Videos/video/$timestamp"

        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)

        //upload video using uri of video storage
        storageReference.putFile(videoUri!!)
            .addOnSuccessListener { taskSnapshot ->
            //uploaded, get url of uploaded video
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result
                if (uriTask.isSuccessful){
                    //video url is received successfully

                    //now we can add video detail to firebase db
                    val hashMap = HashMap<String, Any>()
                    hashMap["id"] = "$timestamp"
                    hashMap["title"] = "$title"
                    hashMap["videoUri"] = "$downloadUri"

                    //put the above into to db
                    val dbReference = FirebaseDatabase.getInstance().getReference("Videos")
                    dbReference.child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener { taskSnapshot ->
                            //video into added successfully
                            progressDialog.dismiss()
                            Toast.makeText(this,"Video Uploaded",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{ e ->
                            //failed adding video info
                            progressDialog.dismiss()
                            Toast.makeText(this, "{${e.message}}",Toast.LENGTH_SHORT).show()
                        }
                }
        }
            .addOnFailureListener({
                //failed uploading
                progressDialog.dismiss()
                Toast.makeText(this,"",Toast.LENGTH_SHORT).show()
            })
    }

    private fun setVideotoVideoView() {
        //set the picked video to video view
        val mediaController = MediaController(this)
        mediaController.setAnchorView(vvVideoView)

        //set media controller
        vvVideoView.setMediaController(mediaController)
        //set video uri
        vvVideoView.setVideoURI(videoUri)
        vvVideoView.requestFocus()
        vvVideoView.setOnPreparedListener {
            //when video is ready, by default don't play he automatically
            vvVideoView.pause()
        }
    }

    private fun videoPickDialog() {
        //options to display in dialog
        val option = arrayOf("Camera", "Gallery")
        //alert dialog
        val builder = AlertDialog.Builder(this)
        //title
        builder.setTitle("Pick Video From")
            .setItems(option) { dialogInterface, i ->
                //handle item clicks
                if (i == 0) {
                    //camera clicked
                    if (!checkCameraPermissions()) {
                        //permissions was not allowed, request
                        requestCameraPermisson()
                    } else {
                        //permissions was allowed, pick video
                        videoPickCamera()
                    }
                } else {
                    //gallery clicked
                    videoPickGallery()
                }
            }
            .show()
    }
    private fun requestCameraPermisson(){
        ActivityCompat.requestPermissions(
            this, cameraPermission, CAMERA_REQUEST_CODE
        )
    }
    private fun checkCameraPermissions():Boolean {
        //check if camera permission i.e camera and storage is allowed or not
        val result1 = ContextCompat.checkSelfPermission(
            this,android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result2 = ContextCompat.checkSelfPermission(
            this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        //return to result as true/false
        return result1 && result2
    }

    private fun videoPickGallery(){
        //video pick intent gallery
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent,"Choose Video"),
            VIDEO_PICK_GALLERY_CODE
        )
    }

    private fun videoPickCamera(){
        //video pick intent camera
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent,VIDEO_PICK_CAMERA_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // go to previous activity
        return super.onSupportNavigateUp()
    }
    /*handle permission result*/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            CAMERA_REQUEST_CODE ->
                if (grantResults.size > 0){
                    //check if permission allowed or denied
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccpeted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccpeted) {
                        //both permissions allowed
                        videoPickCamera()
                    } else {
                        //both on of those are denied
                        Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /*handle video pick result*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            //video is picked from camera or gallery
            if(requestCode == VIDEO_PICK_CAMERA_CODE){
                //video picked from camera
                videoUri == data!!.data
                setVideotoVideoView()
            } else if (requestCode == VIDEO_PICK_GALLERY_CODE){
                //video picked from gallery
                videoUri = data!!.data
                setVideotoVideoView()
            }
        }
        else {
            //cancelled picking video
            Toast.makeText(this,"Cancelled", Toast.LENGTH_LONG).show()
        }

        super.onActivityResult(requestCode, resultCode, data)

    }



}