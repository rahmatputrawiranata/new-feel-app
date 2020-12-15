package com.feel.feel.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.feel.feel.R
import kotlinx.android.synthetic.main.activity_top_up_web_view.*

class TopUpWebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up_web_view)
        val token = intent.getStringExtra("token")!!
        val baseUrl = getString(R.string.midtrans_base_url)
        val url = baseUrl + token
        webView.webViewClient = TopUpWebViewClient(this, progressBar)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)

    }

    class TopUpWebViewClient(private val context: Context, private val progressBar: ProgressBar): WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
            view?.visibility = View.VISIBLE
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            url?.let {
                if (it.contains("gojek")) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(it)
                    }
                    context.startActivity(intent)
                    return true
                }
            }

            return false
        }
    }
}


