package com.hrrock.snapbook.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.gigamole.navigationtabstrip.NavigationTabStrip
import com.hrrock.snapbook.R
import com.hrrock.snapbook.fragments.FollowingNotificationFragment
import com.hrrock.snapbook.fragments.YouNotificationFragment
import com.hrrock.snapbook.utils.BottomNavigationViewHelper
import com.hrrock.snapbook.utils.ScrollableViewPager
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import spencerstudios.com.bungeelib.Bungee

class NotificationActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var activity: Activity? = null
    private var bottomNavigationViewEx: BottomNavigationViewEx? = null
    private var menuItem: MenuItem? = null
    private var menu: Menu? = null
    private var preferences: SharedPreferences? = null

    private companion object {
        private const val ACTIVITY_NUM = 3
        private const val USER_PREFERENCES = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        ctx = this
        activity = this

        setUpTabs()
        setUpBottomNavigationView()
        setNavigationStatusBarColor()
    }

    private fun setUpBottomNavigationView() {
        bottomNavigationViewEx = findViewById(R.id.bottomNavigationView)
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx)
        BottomNavigationViewHelper.enableBottomNavigationView(ctx, activity, bottomNavigationViewEx)

        menu = bottomNavigationViewEx!!.menu
        menuItem = menu!!.getItem(ACTIVITY_NUM)
        menuItem!!.isChecked = true

        bottomNavigationViewEx!!.setIconTintList(ACTIVITY_NUM, ColorStateList.valueOf(resources.getColor(R.color.white, Resources.getSystem().newTheme())))
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.colorGray)
        window.navigationBarColor = resources.getColor(R.color.colorGray)
    }

    private fun setUpTabs() {
        val viewPager = findViewById<View>(R.id.notificationVP) as ScrollableViewPager
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment? {
                var F: Fragment? = null
                if (position == 0) {
                    F = FollowingNotificationFragment()
                }
                if (position == 1) {
                    F = YouNotificationFragment()
                }
                return F
            }

            override fun getCount(): Int {
                return 2
            }
        }

        val navigationTabStrip = findViewById<View>(R.id.notificationTabs) as NavigationTabStrip
        navigationTabStrip.setTitles("Following", "YOU")
        navigationTabStrip.titleSize = 33f
        navigationTabStrip.stripColor = Color.GREEN
        navigationTabStrip.setStripWeight(6f)
        navigationTabStrip.stripFactor = 2f
        navigationTabStrip.stripType = NavigationTabStrip.StripType.LINE
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        navigationTabStrip.cornersRadius = 3f
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY
        navigationTabStrip.activeColor = Color.WHITE

        navigationTabStrip.setViewPager(viewPager, 1)
        navigationTabStrip.setTabIndex(1, true)

        viewPager.setCanScroll(false)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(ctx, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        //overridePendingTransition(0, 0)
        Bungee.fade(ctx)
        activity!!.finish()
    }
}
