package com.feel.feel.screen.wallet



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.feel.feel.R
import com.feel.feel.Util
import com.feel.feel.data.Video
import com.feel.feel.screen.PlaybackActivity
import com.feel.feel.screen.UploadActivity
import kotlinx.android.synthetic.main.fragment_sell_video.*
import kotlinx.android.synthetic.main.fragment_sell_video.progressBar
import kotlinx.android.synthetic.main.item_video.*
import kotlinx.android.synthetic.main.item_video.view.*
import kotlinx.android.synthetic.main.view_top_up.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SellVideoFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sell_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        sellVideoUploadFab.setOnClickListener { startUploadActivity() }
//        shareToOtherApps()
    }

    private fun startUploadActivity() {
        val uploadActivity = Intent(context, UploadActivity::class.java)
        startActivity(uploadActivity)
    }

    private var adapter: SellVideoAdapter? = null
    private fun setupRecyclerView() {
        val requestOptions = RequestOptions
            .placeholderOf(R.drawable.white_background)
            .error(R.drawable.white_background)
        val glide = Glide
            .with(this)
            .setDefaultRequestOptions(requestOptions)

        adapter = SellVideoAdapter(glide)
        sellVideoRecycler.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener { refresh() }
        refresh()

    }

    private fun refresh() {
        CoroutineScope(Dispatchers.Main).launch {
            val userId = Util.getUserData().id
            val api = Util.getApiClient()
            try {
                val response = api.getVideoByUser(userId)
                val data = response.filter { video -> video.status != "2" }
                val userInfo = api.getUserInfo(Util.getUserData().id)
                earningTextView.text = userInfo.earning.toString()

                adapter?.swapData(data)
            } catch (e: Exception) {
                Toast
                    .makeText(context, "Error, Check Connection", Toast.LENGTH_SHORT)
                    .show()
            }

            swipeRefreshLayout.isRefreshing = false
            hideLoading()
        }
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        sellVideoRecycler.visibility = View.VISIBLE
    }

    class SellVideoAdapter(val requestManager: RequestManager) :
        RecyclerView.Adapter<SellVideoAdapter.SellVideoHolder>() {

        private var data: List<Video> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellVideoHolder {
            return SellVideoHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video, parent, false)
            )
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: SellVideoHolder, position: Int) {
            holder.onBind(data[position], requestManager)
        }

        fun swapData(data: List<Video>) {
            this.data = data
            notifyDataSetChanged()
        }

        class SellVideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun onBind(video: Video, requestManager: RequestManager) {
                requestManager
                    .load(video.thumbnailUrl)
                    .into(itemView.thumbnailImg)

                when (video.status) {
                    "0" -> {
                        itemView.priceTextView.text = "PENDING"
                        val color =
                            ContextCompat.getColor(itemView.context, R.color.status_approved)
                        itemView.priceTextView.setBackgroundColor(color)
                    }
                    "1" -> {
                        itemView.priceTextView.text = video.price.toString()
                        val color =
                            ContextCompat.getColor(itemView.context, R.color.status_approved)
                        itemView.priceTextView.setBackgroundColor(color)
                    }
                }

                itemView.setOnClickListener {
                    Util.setPlaybackData(video.id)
                    val intent = Intent(itemView.context, PlaybackActivity::class.java)
                    itemView.context.startActivity(intent)
                }

            }
        }
    }



    override fun onResume() {
        super.onResume()
        adapter?.let { refresh() }
    }
}

