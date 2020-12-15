package com.feel.feel.videorecyclerview

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.text.BoringLayout
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.feel.feel.R
import com.feel.feel.data.BuyVideoRequestBody
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.item_feed.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VideoRecyclerView(context: Context, attributeSet: AttributeSet?) :
    RecyclerView(context, attributeSet) {

    var hidden : Boolean = false
    private lateinit var playerView: PlayerView
    lateinit var videoPlayer: SimpleExoPlayer

    private var frameLayout: FrameLayout? = null
    private var thumbnail: ImageView? = null
    private var lottieBar: LottieAnimationView? = null
    private var viewHolder: VideoRecyclerViewViewHolder? = null
    private var currentPlayPosition: Int
    private var isVideoPlayed: Boolean = false

    fun createMediaSourceFromUrl(url: String): MediaSource {
        val dataSourceFactory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, "Feel"))
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        return mediaSourceFactory.createMediaSource(Uri.parse(url))
    }


    private val videoReadyListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_READY -> {
                    playerView.visibility = View.VISIBLE
                }
                Player.STATE_ENDED -> {
                    videoPlayer.seekTo(0)
                }
                else -> {}
            }
            playerView?.setOnClickListener {
                if(videoPlayer.playWhenReady){
                    videoPlayer.playWhenReady = false
                    videoPlayer.playbackState
                } else {
                    videoPlayer.playWhenReady = true
                    videoPlayer.playbackState
                }
            }
        }

    }

    private fun setupPlayer() {
        videoPlayer = SimpleExoPlayer.Builder(context).build()

        playerView = PlayerView(context)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
        playerView.useController = true
        playerView.player = this.videoPlayer
    }

    private fun updateCurrentItemComponent(position: Int) {
        viewHolder = getChildAt(position).tag as VideoRecyclerViewViewHolder
        frameLayout = viewHolder?.view?.mediaContainer
        thumbnail = viewHolder?.view?.feedItemImageView
        lottieBar = viewHolder?.view?.lottieBar
    }

    private fun getPlayPosition(endOfList: Boolean): Int {
        val linearLayoutManager = layoutManager as LinearLayoutManager
        val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
        val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

        if (endOfList) {
            return lastVisibleItem
        }

        val h1 = getVisibleItemHeight(firstVisibleItem)
        val h2 = getVisibleItemHeight(firstVisibleItem + 1)

        return if (h1 > h2) firstVisibleItem else firstVisibleItem + 1
    }

    private fun getVisibleItemHeight(itemPosition: Int): Int {
        val linearLayoutManager = layoutManager as LinearLayoutManager
        val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
        val at = itemPosition - firstVisibleItem

        val parentHeight = height

        val parentLocation = IntArray(2)
        getLocationInWindow(parentLocation)

        val view = getChildAt(at) ?: return 0
        val location = IntArray(2)
        view.getLocationInWindow(location)

        val y = location[1] - parentLocation[1]
        val height = view.height

        return if (y < 0) {
            y + height
        } else {
            kotlin.math.min(parentHeight - y, height)
        }
    }

    fun removePlayerView() {
        val parent = playerView.parent as? ViewGroup
        parent?.let {
            val indexOfChild = it.indexOfChild(playerView)
            it.removeViewAt(indexOfChild)
        }
        videoPlayer.playWhenReady = false
        lottieBar?.visibility = View.GONE
    }

    fun attachPlayerView() {
        frameLayout?.addView(playerView)
        playerView.visibility = View.INVISIBLE
        lottieBar?.visibility = View.VISIBLE
    }

    init {
        setupPlayer()
        currentPlayPosition = -1
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val linearLayoutManager = layoutManager as LinearLayoutManager
                    val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

                    val playPosition = getPlayPosition(!recyclerView.canScrollVertically(1))
                    if (currentPlayPosition == playPosition) {
                        return
                    }

                    currentPlayPosition = playPosition
                    val position = playPosition - firstVisibleItem

                    if (getChildAt(position).getTag(R.id.view_holder_type_tag) == 1) {
                        return
                    }

                    removePlayerView()
                    updateCurrentItemComponent(position)
                    attachPlayerView()

                    val userId = com.feel.feel.Util.getUserData().id
                    val video = viewHolder?.video!!
                    val videoId = video.id

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val api = com.feel.feel.Util.getApiClient()
                            val userDidBuyResponse = api.userDidBuy(userId, videoId)
                            val userDidBuy = userDidBuyResponse == "true"

                           val userInfo = api.getUserInfo(userId)
                            val balance = userInfo.balance

                            if (!userDidBuy && video.price > balance) {
                                val dialog = AlertDialog.Builder(context)
                                    .setMessage("Insufficient Balance, Please Top Up Your Balance By Swipping Right")
                                    .create()
                                dialog.show()

                                lottieBar?.visibility = View.GONE

                                return@launch
                            }

                            if (!userDidBuy) {
                                val response = api.buyVideo(BuyVideoRequestBody(userId, videoId))
                                com.feel.feel.Util.setBalance(response.balance)
                            }

                            val videoDetail = api.getVideoDetail(videoId)
                            val videoUrl = videoDetail.url
                            val mediaSource = createMediaSourceFromUrl(videoUrl)
                            videoPlayer.prepare(mediaSource)
                            videoPlayer.playWhenReady = !hidden


                        } catch (e: HttpException) {
                            Toast
                                .makeText(context, "Error, Check your internet connection", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        })
        videoPlayer.addListener(videoReadyListener)
    }

}

