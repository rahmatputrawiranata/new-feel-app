package com.feel.feel.videorecyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.feel.feel.data.Video
import com.feel.feel.R
import com.feel.feel.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoRecyclerViewAdapter(var videos: MutableList<Video?>, private val requestManager: RequestManager)
    : RecyclerView.Adapter<VideoRecyclerViewViewHolder>() {

    private final val ITEM_VIDEO = 0;
    private final val ITEM_LOADING = 1;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoRecyclerViewViewHolder {
        if (viewType == ITEM_VIDEO) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_feed, parent, false)
            view.setTag(R.id.view_holder_type_tag, ITEM_VIDEO)
            return VideoRecyclerViewViewHolder(view)
        }
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_loading, parent, false)
        view.setTag(R.id.view_holder_type_tag, ITEM_LOADING)
        return VideoRecyclerViewViewHolder(view)

    }

    override fun getItemCount() = videos.size

    override fun onBindViewHolder(holder: VideoRecyclerViewViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_VIDEO) {
            val video = videos[position]
            holder.onBind(video!!, requestManager)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (videos[position] == null) ITEM_LOADING else ITEM_VIDEO
    }
}