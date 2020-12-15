package com.feel.feel.screen.wallet


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.feel.feel.data.Author
import com.feel.feel.data.ChargeRequestBody
import com.feel.feel.data.OwnVideo
import com.feel.feel.data.Video
import com.feel.feel.screen.PlaybackActivity
import com.feel.feel.screen.TopUpWebViewActivity
import com.feel.feel.screen.WalletActivity
import kotlinx.android.synthetic.main.fragment_buy_video.*
import kotlinx.android.synthetic.main.item_video.view.*
import kotlinx.android.synthetic.main.view_top_up.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class BuyVideoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buy_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        button50k.setOnClickListener { goToTopUpPage(10000) }
        button100k.setOnClickListener { goToTopUpPage(30000) }
        button500k.setOnClickListener { goToTopUpPage(60000) }
        buttonMore.setOnClickListener { goToWallet() }
    }

    fun goToWallet() {
        val intent = Intent(context, WalletActivity::class.java)
        startActivity(intent)
    }
    fun goToTopUpPage(ammount: Int) {

        val dialog = AlertDialog.Builder(context)
            .setView(layoutInflater.inflate(R.layout.layout_progress_dialog, null))
            .create()

        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
            try {
                val api = Util.getApiClient()
                val body = ChargeRequestBody(ammount, Util.getUserData().id)
                val response = api.charge(body)
                val intent = Intent(this@BuyVideoFragment.context, TopUpWebViewActivity::class.java)
                intent.putExtra("token", response.token)
                startActivity(intent)
                dialog.hide()
            } catch (e : Exception) {
                Toast
                    .makeText(context, "Error, Check Connection", Toast.LENGTH_SHORT)
                    .show()
                dialog.hide()
            }
        }
    }

    private fun setupRecyclerView() {
        val requestOptions = RequestOptions
            .placeholderOf(R.drawable.white_background)
            .error(R.drawable.white_background)
        val glide = Glide
            .with(this)
            .setDefaultRequestOptions(requestOptions)

        val buyVideoAdapter = BuyVideoAdapter(glide)
        buyVideoRecycler.apply {
            adapter = buyVideoAdapter
        }

        swipeRefreshLayout.setOnRefreshListener { refresh(buyVideoAdapter) }
        refresh(buyVideoAdapter)

    }

    fun refresh(buyVideoAdapter: BuyVideoAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            val api = Util.getApiClient()
           val userId = Util.getUserData().id
            try {
                val response = api.getOwnedVideoByUser(userId)
                val ownedVideos = response.ownVideos
                buyVideoAdapter.swapData(ownedVideos)

                val userInfo = api.getUserInfo(Util.getUserData().id)
                balanceTextView.text = userInfo.balance.toString()
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
        buyVideoRecycler.visibility = View.INVISIBLE
    }

    class BuyVideoAdapter(val requestManager: RequestManager) : RecyclerView.Adapter<BuyVideoAdapter.BuyVideoHolder>() {

        private var data: List<OwnVideo> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyVideoHolder {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_video, parent, false)
            return BuyVideoHolder(view)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: BuyVideoHolder, position: Int) {
            holder.onBind(data[position], requestManager)
        }

        fun swapData(data: List<OwnVideo>) {
            this.data = data
            notifyDataSetChanged()
        }

        class BuyVideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun onBind(video: OwnVideo, requestManager: RequestManager) {
                val price = video.price
                requestManager.load(video.thumbnailUrl).into(itemView.thumbnailImg)
                itemView.priceTextView.setText(price.toString())

                val color = ContextCompat.getColor(itemView.context, R.color.status_approved)
                itemView.priceTextView.setBackgroundColor(color)

                itemView.setOnClickListener {
                    Util.setPlaybackData(video.id)
                    val intent = Intent(itemView.context, PlaybackActivity::class.java)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }
}
