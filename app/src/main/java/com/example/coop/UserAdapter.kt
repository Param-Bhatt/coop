package com.example.coop

import android.content.Context
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.jetbrains.anko.db.NULL

internal class UserAdapter(var context: Context, fm: FragmentManager?, var totalTabs: Int) :

    FragmentPagerAdapter(fm!!) {
    override fun getItem(position: Int): Fragment {
        if(position == 0) {
            return UserInfo()
        }
        else if(position == 1) {
            return UserPosts()
        }
        else if(position == 2) {
            return UserComments()
        }
        return Fragment()
    }

    override fun getCount(): Int {
        return totalTabs
    }
}