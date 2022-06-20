package com.example.firebasevideo.Adapter

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasevideo.Model.ModelVideo
import com.example.firebasevideo.R
import java.util.*
import kotlin.collections.ArrayList

class AdapterVideo (
    private var context: Context,
    private var videoArrayList: ArrayList<ModelVideo>
        ): RecyclerView.Adapter<AdapterVideo.HolderVideo>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderVideo {
        //inflate layout row_video.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_video, parent, false)
        return HolderVideo(view)
    }

    override fun onBindViewHolder(holder: HolderVideo, position: Int) {
        /*---------get data,set data, handle click, etc----------*/
        //get data
        val modelVideo = videoArrayList!![position]

        //get spesific data
        val id : String? = modelVideo.id
        val title: String?= modelVideo.title
        val timestamp : String? = modelVideo.timestamp
        val videoUri : String? = modelVideo.videoUri

        //format date e.g 20/06/2022 11:43
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp!!.toLong()
        val dateFormat = android.text.format.DateFormat
            .format("dd/MM/yyyy K:mm a", calendar).toString()

        //set Data
        holder.titleTv.text = title
        holder.timeTv.text = dateFormat
        setVideoUrl(modelVideo, holder)
    }

    private fun setVideoUrl(modelVideo: ModelVideo, holder: HolderVideo) {
        //show progress
//        holder.progressBar.visibility = View.VISIBLE

        //get video url
        val videoUrl : String? = modelVideo.videoUri

        //MediaController for play/pause/time, etc.
        val mediaController = MediaController(context)
        mediaController.setAnchorView(holder.videoView)
        val videoUri = Uri.parse(videoUrl)

        holder.videoView.setMediaController(mediaController)
        holder.videoView.setVideoURI(videoUri)
        holder.videoView.requestFocus()

        holder.videoView.setOnPreparedListener { mediaPlayer ->
            //video is prepared to play
            mediaPlayer.start()
        }
        holder.videoView.setOnInfoListener(MediaPlayer.OnInfoListener{mp, what, extra ->
            //check if buffering / rendering etc
            when(what){
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    //Rendering started
//                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    //buffering started
//                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    //buffering ended
//                    holder.progressBar.visibility= View.GONE
                    return@OnInfoListener true
                }
            }
            false
        })
        holder.videoView.setOnCompletionListener { mediaPlayer ->
            //restart video when completed | loop video
            mediaPlayer.start()

        }
    }

    override fun getItemCount(): Int {
        return videoArrayList!!.size //return size/lenght or the arrayList
    }

    //view holder class holds and inits UI Views or row_video.xml
    class HolderVideo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init UI Views
        var videoView : VideoView = itemView.findViewById(R.id.rowVideoView)
        var titleTv : TextView = itemView.findViewById(R.id.titleTV)
        var timeTv : TextView = itemView.findViewById(R.id.timeTV)
//        var progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)
    }

}