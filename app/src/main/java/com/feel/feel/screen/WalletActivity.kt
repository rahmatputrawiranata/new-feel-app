package com.feel.feel.screen

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feel.feel.R
import com.feel.feel.Util
import com.feel.feel.data.ChargeRequestBody
import com.feel.feel.data.OwnVideoX
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.android.synthetic.main.item_transaction.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class WalletActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        val layoutManager = LinearLayoutManager(this)
        val adapter = TransactionRecyclerViewAdapter(listOf())
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(itemDecoration)

        topupButton.setOnClickListener { goToTopUpPage() }
        populateTransaction(adapter)
    }

    fun populateTransaction(adapter: TransactionRecyclerViewAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val api = Util.getApiClient()
                val response = api.getUserInfo(Util.getUserData().id)
                var transactions = mutableListOf<OwnVideoX>()
                response.ownVideos.forEach { transactions.add((it)) }
                adapter.data = transactions
                adapter.notifyDataSetChanged()


                nameTextView.text = response.name
                balanceTextView.text = response.balance.toString()
            } catch (e: Exception) {
                Toast
                    .makeText(this@WalletActivity, "Error, Check Connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun goToTopUpPage() {
        val ammount = ammountTV.text.toString().toInt() * 2000
        val dialog = AlertDialog.Builder(this)
            .setView(layoutInflater.inflate(R.layout.layout_progress_dialog, null))
            .create()

        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
            try {
                val api = Util.getApiClient()
                val body = ChargeRequestBody(ammount, Util.getUserData().id)
                val response = api.charge(body)
                val intent = Intent(this@WalletActivity, TopUpWebViewActivity::class.java)
                intent.putExtra("token", response.token)
                startActivity(intent)
                dialog.hide()
            } catch (e : Exception) {
                Toast
                    .makeText(this@WalletActivity, "Error, Check Connection", Toast.LENGTH_SHORT)
                    .show()
                dialog.hide()
            }
        }
    }

    class TransactionRecyclerViewAdapter(var data: List<OwnVideoX>): RecyclerView.Adapter<TransactionRecyclerViewViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TransactionRecyclerViewViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_transaction, parent, false)

            return TransactionRecyclerViewViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: TransactionRecyclerViewViewHolder, position: Int) {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val date = parser.parse(data[position].createdAt)
            val formater = SimpleDateFormat("EEE, d MMM yyyy")
            val dateString = formater.format(date)
            holder.itemView.title.text = dateString
            holder.itemView.price.text = data[position].price.toString()
        }
    }

    class TransactionRecyclerViewViewHolder(view: View): RecyclerView.ViewHolder(view)
}
