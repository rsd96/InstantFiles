package com.rsd96.instantfiles

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by Ramshad on 8/7/15.
 */
class TabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val FragmentList = mutableListOf<Fragment>()
    private val FragmentTitles = mutableListOf<String>()

    fun addFragment(fragment: Fragment, title: String) {
        fragment.retainInstance = true
        FragmentList.add(fragment)
        FragmentTitles.add(title)
    }

    override fun getItem(position: Int): Fragment {
        return FragmentList.get(position)
    }

    override fun getCount(): Int {
        return FragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return FragmentTitles.get(position)
    }

}
