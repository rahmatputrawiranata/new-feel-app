package com.feel.feel.screen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.feel.feel.BuildConfig
import com.feel.feel.R
import com.feel.feel.Util
import com.feel.feel.data.RefreshTokenRequestBody
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val versionName = BuildConfig.VERSION_NAME
        appVersionTv.text = "App Version " + versionName
        Handler().postDelayed({
            val destionation =
                if (Util.hasLogin()) MainActivity::class.java else AuthActivity::class.java
            val intent = Intent(this, destionation)
            startActivity(intent)
            finish()
        }, 1000)
    }
}
