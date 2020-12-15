package com.feel.feel.screen.feed


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ToxicBakery.viewpager.transforms.ZoomOutSlideTransformer
import com.feel.feel.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_feed.*

class FeedFragment : Fragment() {
    private var feedListFragments = mutableListOf<FeedListFragment>()
    private var pageTitles = mutableListOf<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initFragment()
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    fun initFragment() {
        val bundle2000 = Bundle()
        bundle2000.putInt("pricepoint", 1)
        val fragment2000 = FeedListFragment()
        fragment2000.arguments = bundle2000
        pageTitles.add("1 Tic")

        val bundle6000 = Bundle()
        bundle6000.putInt("pricepoint", 3)
        val fragment6000 = FeedListFragment()
        fragment6000.arguments = bundle6000
        pageTitles.add("3 Tic")

        val bundle10000 = Bundle()
        bundle10000.putInt("pricepoint", 5)
        val fragment10000 = FeedListFragment()
        fragment10000.arguments = bundle10000
        pageTitles.add("5 Tic")

        feedListFragments.add(fragment2000)
        feedListFragments.add(fragment6000)
        feedListFragments.add(fragment10000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FEED FRAGMENT", "ON VIEW CREATED")
        setupViewPager()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.d("OnHidden", hidden.toString())
        val currentItem = feedViewPager.currentItem

        if (hidden) {
            feedListFragments[currentItem].pauseVideoPlayer()
        }
        else {
            feedListFragments[currentItem].resumeVideoPlayer()
        }
    }

    private fun setupViewPager() {
        val adapter = FeedFragmentPagerAdapter(childFragmentManager)
        adapter.pageTitles.addAll(pageTitles)
        adapter.fragments.addAll(feedListFragments)
        feedViewPager.adapter = adapter
        feedViewPager.setPageTransformer(false, ZoomOutSlideTransformer())
        tabLayout.setupWithViewPager(feedViewPager)
    }

    class FeedFragmentPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        val fragments = mutableListOf<FeedListFragment>()
        val pageTitles = mutableListOf<String>()

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        override fun getPageTitle(position: Int): CharSequence? {
            return pageTitles[position]
        }
    }
}
