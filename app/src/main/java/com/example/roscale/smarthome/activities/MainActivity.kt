package com.example.roscale.smarthome.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.roscale.smarthome.R
import com.example.roscale.smarthome.fragments.GroupsFragment
import com.example.roscale.smarthome.fragments.LightsFragment
import kotlinx.android.synthetic.main.activity_main.*

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> GroupsFragment()
            1 -> LightsFragment()
            2 -> GroupsFragment()
            else -> GroupsFragment()
        }
    }

    override fun getCount(): Int = 3
}


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var activity: Activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> { viewPager.currentItem = 0; true }
                R.id.navigation_lights -> { viewPager.currentItem = 1; true }
                R.id.navigation_schedules -> { viewPager.currentItem = 2; true }
                else -> { viewPager.currentItem = 0; false }
            }
        }

        viewPager.adapter = PagerAdapter(supportFragmentManager)
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                navigation.selectedItemId = when (position) {
                    0 -> R.id.navigation_home
                    1 -> R.id.navigation_lights
                    2 -> R.id.navigation_schedules
                    else -> R.id.navigation_home
                }
            }
        })
    }
}
