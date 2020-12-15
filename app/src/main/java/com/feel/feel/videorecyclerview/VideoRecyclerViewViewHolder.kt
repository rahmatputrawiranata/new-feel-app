package com.feel.feel.videorecyclerview

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.feel.feel.data.BuyVideoRequestBody
import com.feel.feel.data.Video
import com.feel.feel.data.VideoDetail
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.item_feed.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoRecyclerViewViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    lateinit var video: Video
    fun onBind(video: Video, requestManager: RequestManager) {
        view.tag = this
        this.video = video
        requestManager.load(video.thumbnailUrl).into(view.feedItemImageView)

        view.feedItemAuthor.text = video.author.name
        view.feedItemMusic.text = video.genre
        view.feedItemPricePoint.text  = video.price.toString()
        view.feedItemDescription.text = video.description
    }
}