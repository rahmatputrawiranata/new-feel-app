package com.feel.feel.screen.feed

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.feel.feel.API
import com.feel.feel.R
import com.feel.feel.Util
import com.feel.feel.data.Video
import com.feel.feel.videorecyclerview.VideoRecyclerView
import com.feel.feel.videorecyclerview.VideoRecyclerViewAdapter
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.activity_upload.view.*
import kotlinx.android.synthetic.main.fragment_feed_list.*
import kotlinx.android.synthetic.main.fragment_feed_list.view.*
import kotlinx.android.synthetic.main.item_feed.view.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FeedListFragment : Fragment() {

    private var priceCategoryId: Int = 0
    private var price = 0

    private var is_loading = false
    private var is_at_end = false
    private var page = 1
    private val items_per_page = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater
            .inflate(R.layout.fragment_feed_list, container, false)

        price = arguments?.getInt("pricepoint")!!

        val requestOption = RequestOptions
            .placeholderOf(R.drawable.white_background)
            .error(R.drawable.white_background)

        val glide = Glide.with(this)
            .setDefaultRequestOptions(requestOption)

        val viewManager = LinearLayoutManager(context)
        val viewAdapter = VideoRecyclerViewAdapter(mutableListOf(), glide)
        val videoRecyclerView = view.videoRecyclerView
        videoRecyclerView.setHasFixedSize(true)
        videoRecyclerView.layoutManager = viewManager
        videoRecyclerView.adapter = viewAdapter

        initLoadingScrollListener(videoRecyclerView)

        view.swipeRefreshLayout.setOnRefreshListener { getData(viewAdapter) }
        getData(viewAdapter)


        return view
    }

    fun initLoadingScrollListener(videoRecyclerView: VideoRecyclerView) {
        videoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val adapter = videoRecyclerView.adapter as VideoRecyclerViewAdapter
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (!is_loading && !is_at_end) {
                    if (lm.findLastCompletelyVisibleItemPosition() == adapter.videos.size - 1) {
                        adapter.videos.add(null)
                        adapter.notifyItemInserted(adapter.videos.size - 1)
                        is_loading = true

                        loadMoreItem(adapter)
                    }
                }
            }
        })
    }

    fun loadMoreItem(adapter: VideoRecyclerViewAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val api = Util.getApiClient()
                val videos = api.getVideos(price, page * items_per_page, items_per_page)
                page++

                adapter.videos.removeAt(adapter.videos.size-1)
                adapter.notifyItemRemoved(adapter.videos.size)


                if (videos.size == 0) {
                    is_at_end = true
                } else {
                    adapter.videos.addAll(videos)
                    adapter.notifyItemRangeInserted(adapter.videos.size - videos.size, videos.size)
                }
                is_loading = false

            } catch (e: Exception) {
                Toast
                    .makeText(context, "Error, Check Connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getData(viewAdapter: VideoRecyclerViewAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                page = 1
                is_at_end = false
                val api = Util.getApiClient()
                val videos = api.getVideos(price, 0, items_per_page)

                viewAdapter.videos = videos as MutableList<Video?>
                viewAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast
                    .makeText(context, "Error, Check Connection", Toast.LENGTH_SHORT)
                    .show()
            }

            swipeRefreshLayout?.isRefreshing = false
            hideLoading()

        }
    }

    fun hideLoading() {
        videoRecyclerView?.visibility = View.VISIBLE
        lottieBar?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        Log.d("video_state", "Paused")
        pauseVideoPlayer()
    }

    override fun onResume() {
        super.onResume()
        Log.d("video_state", "Resumed")
        parentFragment?.let {
            if (!it.isHidden) {
                resumeVideoPlayer()
            }
        }
    }

    fun pauseVideoPlayer() {
        videoRecyclerView.videoPlayer.playWhenReady = false
        videoRecyclerView.hidden = true
    }

    fun resumeVideoPlayer() {
        videoRecyclerView.videoPlayer.playWhenReady = true
        videoRecyclerView.hidden = false
    }

}