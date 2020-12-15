package com.feel.feel.screen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.feel.feel.API
import com.feel.feel.R
import com.feel.feel.Util
import com.feel.feel.data.GoogleLoginRequestBody
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var client: GoogleSignInClient
    private val RC_GSO_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        initClient()
        val animation = findViewById<LottieAnimationView>(R.id.authProgressBarr)
        loginButton.setOnClickListener { googleLogin() }

        if (Util.hasLogin()) {
            signOut()
        }
    }

    private fun signOut() {
        showProgressBar()
        Util.deleteToken()
        client.signOut()
        client.revokeAccess()
        showSignInButton()
    }

    private fun initClient() {
        val options = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.client_id))
            .build()

        client = GoogleSignIn.getClient(this, options)
    }


    private fun googleLogin() {
        showProgressBar()
        val signInIntent = client.signInIntent
        startActivityForResult(signInIntent, RC_GSO_SIGN_IN)
    }

    private fun showProgressBar() {
        loginButton.visibility = View.GONE
        setupAnimation()
    }

    private fun showSignInButton() {
        stopAnimation()
        loginButton.visibility = View.VISIBLE

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GSO_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val token = account?.idToken!!

                    val response = API().googleLogin(GoogleLoginRequestBody(token))
                    val accessToken = response.data.accessToken
                    val refreshToken = response.data.refreshToken

                    Util.setToken(accessToken)
                    Util.setRefreshToken(refreshToken)
                    Util.setuserData(response.data)
                    Toast.makeText(this@AuthActivity, "Login Success", Toast.LENGTH_SHORT).show()

                    startMainAcitivity()
                    finish()
                } catch (e: Exception) {
                    throw e
                }

            }

        } catch (e: ApiException) {
            showSignInButton()
            Toast.makeText(this, "Login Failed, Check Connection", Toast.LENGTH_SHORT).show()
        }

    }

    fun setupAnimation(){
        val animation = findViewById<LottieAnimationView>(R.id.authProgressBarr)
        animation.speed = 2.0F // How fast does the animation play
        animation.progress = 0F // Starts the animation from 50% of the beginning
        animation.playAnimation()
        animation.repeatCount
        animation.addAnimatorUpdateListener {

        }
        authProgressBarr.visibility = View.VISIBLE
    //    animation.repeatMode = LottieDrawable.RESTART // Restarts the animation (you can choose to reverse it as well)
     //   animation.cancelAnimation() // Cancels the animation
    }

    fun stopAnimation(){
        val animation = findViewById<LottieAnimationView>(R.id.authProgressBarr)
        animation.cancelAnimation()
        authProgressBarr.visibility = View.GONE
    }

    private fun startMainAcitivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
