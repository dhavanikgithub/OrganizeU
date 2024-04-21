package com.dk.organizeu.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class AcademicDetailsTabAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragmentList: MutableList<Fragment> = ArrayList()
    private val fragmentTitleList: MutableList<String> = ArrayList()
    companion object{
        const val TAG = "OrganizeU-AcademicDetailsTabAdapter"
    }

    override fun getItem(position: Int): Fragment {
        try {
            return fragmentList[position]
        } catch (e: Exception) {
            Log.e(BatchAdapter.TAG,e.message.toString())
            throw e
        }
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence {
        try {
            return fragmentTitleList[position]
        } catch (e: Exception) {
            Log.e(BatchAdapter.TAG,e.message.toString())
            throw e
        }
    }
}
