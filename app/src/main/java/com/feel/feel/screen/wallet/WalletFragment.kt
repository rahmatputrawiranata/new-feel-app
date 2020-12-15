package com.feel.feel.screen.wallet


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.feel.feel.screen.wallet.BuyVideoFragment
import com.feel.feel.screen.wallet.SellVideoFragment
import com.feel.feel.R
import com.feel.feel.Util
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*

class WalletFragment() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()

        nameTextView.text = Util.getUserData().name
        moreButton.setOnClickListener {openDrawer()}
    }

    private fun openDrawer () {
        val drawer = activity?.findViewById<DrawerLayout>(R.id.navigationDrawer)
        drawer?.openDrawer(GravityCompat.END)
        val balanceTextView = drawer?.findViewById<TextView>(R.id.balanceTextView)
        balanceTextView?.text = Util.getBalance().toString()
    }
    private fun setupViewPager() {

        val adapter = WalletPagerAdapter(childFragmentManager)
        adapter.fragments.add(BuyVideoFragment())
        adapter.fragments.add(SellVideoFragment())
        walletPager.adapter = adapter
        walletTab.setupWithViewPager(walletPager)
    }

    class WalletPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        val fragments = mutableListOf<Fragment>()
        private val fragmentsTitle = arrayOf("Buy", "Sell")

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        override fun getPageTitle(position: Int) = fragmentsTitle[position]
    }
}
