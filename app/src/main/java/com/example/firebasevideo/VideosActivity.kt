package com.example.firebasevideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideosActivity : AppCompatActivity() {

    private lateinit var addVideo : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)


        title = "Videos"

        addVideo = findViewById(R.id.addVideoFab)

        addVideo.setOnClickListener {
            startActivity(Intent(this,AddVideosActivity::class.java))
        }
    }
}