package com.feel.feel.screen

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.add
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.feel.feel.R
import com.feel.feel.Util
import com.feel.feel.data.RefreshTokenRequestBody
import com.feel.feel.screen.feed.FeedFragment
import com.feel.feel.screen.feed.FeedListFragment
import com.feel.feel.screen.wallet.WalletFragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val feedFragment = FeedFragment()
    private val walletFragment = WalletFragment()

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNavigationBottom()
        setupNavigationDrawer()

    }

    private fun setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener)

    }

    private val onNavigationItemSelectedListener = object : NavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(p0: MenuItem): Boolean {
            when (p0.itemId) {
                R.id.menu_logout -> doLogout()
                R.id.menu_feedback -> goToFeedback()
                R.id.menu_policy -> goToPolicy()
                R.id.menu_wallet -> goToWallet()
            }
            return true
        }
    }

    private fun goToPolicy() {
        val intent = Intent(this, PolicyActivity::class.java)
        startActivity(intent)
    }

    private fun goToFeedback() {
        val appPackageName = packageName
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName))
        startActivity(intent)
    }

    private fun goToWallet() {
        val intent = Intent(this, WalletActivity::class.java)
        startActivity(intent)
    }

    private fun doLogout() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun setupNavigationBottom() {
        val adapter = MainFragmentPagerAdapter(supportFragmentManager)
        adapter.fragments.add(walletFragment)
        adapter.fragments.add(feedFragment)
        viewPager.adapter = adapter
        viewPager.currentItem = 1
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }
    class MainFragmentPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        val fragments = mutableListOf<Fragment>()

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size
    }
}
