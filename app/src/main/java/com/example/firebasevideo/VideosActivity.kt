package com.example.firebasevideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasevideo.Adapter.AdapterVideo
import com.example.firebasevideo.Model.ModelVideo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class VideosActivity : AppCompatActivity() {

    //Arraylist for video list
    private lateinit var videoArrayList: ArrayList<ModelVideo>
    //Adapter
    private lateinit var adapterVideo: AdapterVideo
    private lateinit var dbRef : DatabaseReference

    private lateinit var addVideo : FloatingActionButton
    private lateinit var videoRV : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)


        title = "Videos"

        //function call to load videos from firebase
        loadVideosFromFirebase()

        addVideo = findViewById(R.id.addVideoFab)
        videoRV = findViewById(R.id.videoRv)
        videoRV.layoutManager = LinearLayoutManager(this.baseContext)
        videoRV.setHasFixedSize(true)

        addVideo.setOnClickListener {
            startActivity(Intent(this,AddVideosActivity::class.java))
        }
    }

    private fun loadVideosFromFirebase() {
        //init arraylist before adding data into it
        videoArrayList = ArrayList()

        //reference of firebase db
        dbRef = FirebaseDatabase.getInstance().getReference("Videos")
        dbRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
               //clear list before adding data into it
                videoArrayList.clear()
                for (ds in snapshot.children){
                    //get data as model
                    val modelVideo = ds.getValue(ModelVideo::class.java)
                    //add to Array List
                    videoArrayList.add(modelVideo!!)
                }
                //setup adapter
                adapterVideo = AdapterVideo(this@VideosActivity, videoArrayList)
                //set adapter to recyclerview
                videoRV.adapter = adapterVideo
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}