package com.feel.feel.screen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Outline
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.feel.feel.R
import com.feel.feel.Util
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.activity_playback.*
import kotlinx.android.synthetic.main.activity_playback.mediaContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaybackActivity : AppCompatActivity() {
    companion object{
        const val NOTIFICATION = 1
        const val CHANNEL_ID = "channel_1"
        val CHANNEL_NAME: CharSequence = "feel_channel"
    }

    private var videoPlayer: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)
        setupPlayer()

       window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
       window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
       window.statusBarColor = ContextCompat.getColor(this@PlaybackActivity, R.color.playback_notification_bar)
       window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            val videoId = Util.getPlaybackData()
            val api = Util.getApiClient()
            val data = api.getVideoDetail(videoId)
            author.text = data.author.name
            genre.text = data.genre
            description.text = data.description
            val categoryString = when (data.price) {
                2000 -> "1"
                6000 -> "3"
                10000 -> "5"
                else -> "PENDING"
            }
            category.text = categoryString

            val videoUrl = data.url
            val mediaSource = createMediaSourceFromUrl(videoUrl)

            videoPlayer?.prepare(mediaSource)
            playbackProgressBar.visibility = View.GONE
            content.visibility = View.VISIBLE

            playerView?.setOnClickListener {
                if(videoPlayer?.playWhenReady == true)
                    onPause()
                else
                    onStart()
            }

            share.setOnClickListener{
                buildNotification()
                Toast.makeText(this@PlaybackActivity, "Please screenshot this screen", Toast.LENGTH_LONG).show()
            }
        }
    }

        private val videoReadyListener = object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        playerView?.visibility = View.VISIBLE
                        progressBar?.visibility = View.GONE
                        Log.d("PLAYER", "READY")
                    }
                    Player.STATE_ENDED -> videoPlayer?.seekTo(0)
                    else -> {
                    }
                }
            }

        }

        private fun setupPlayer() {
            videoPlayer = SimpleExoPlayer.Builder(this).build()
            videoPlayer?.addListener(videoReadyListener)
            videoPlayer?.playWhenReady = true
            playerView = PlayerView(this)
            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            playerView?.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            playerView?.useController = true
            playerView?.player = this.videoPlayer
            playerView?.visibility = View.INVISIBLE
            playerView?.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, 80F)
                }
            }

            playerView?.clipToOutline = true
            mediaContainer.addView(playerView)
        }

        private fun createMediaSourceFromUrl(url: String): MediaSource {
            val dataSourceFactory =
                DefaultDataSourceFactory(
                    this,
                    com.google.android.exoplayer2.util.Util.getUserAgent(this, "Feel")
                )
            val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
            return mediaSourceFactory.createMediaSource(Uri.parse(url))
        }

   /* fun setupAnimation(){
        val animation = findViewById<LottieAnimationView>(R.id.progressBar)
        animation.speed = 2.5F // How fast does the animation play
        animation.progress = 1F // Starts the animation from 50% of the beginning
        animation.addAnimatorUpdateListener {
            // Called everytime the frame of the animation changes
        }
        animation.repeatMode = LottieDrawable.RESTART // Restarts the animation (you can choose to reverse it as well)
        animation.cancelAnimation() // Cancels the animation
    }*/

    private fun notificationIntent(): PendingIntent {
        val intent = Intent(this@PlaybackActivity, PlaybackActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, 0)
    }

    private fun buildNotification(){
        val notificationIntent = notificationIntent()
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(notificationIntent)
            .setSmallIcon(R.drawable.bg_splash_new)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.bg_splash_new))
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000, 1000))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            mBuilder.setChannelId(CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }

        val notification = mBuilder.build()
        mNotificationManager.notify(NOTIFICATION, notification)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        videoPlayer?.release()
        finish()
    }

    override fun onPause() {
        super.onPause()
        videoPlayer?.playWhenReady = false
        videoPlayer?.playbackState
    }

    override fun onStart() {
        super.onStart()
        videoPlayer?.playWhenReady = true
        videoPlayer?.playbackState
    }
}