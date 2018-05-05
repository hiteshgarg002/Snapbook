package com.hrrock.snapbook.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gigamole.navigationtabstrip.NavigationTabStrip
import com.hrrock.snapbook.R
import com.hrrock.snapbook.fragments.ProfileGridViewFragment
import com.hrrock.snapbook.fragments.ProfileListViewFragment
import com.hrrock.snapbook.networks.VolleyConnect
import com.hrrock.snapbook.utils.GlideApp
import com.hrrock.snapbook.utils.ScrollableViewPager
import com.wang.avi.AVLoadingIndicatorView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_user_profile.*
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee

class UserProfileActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var dp: CircleImageView? = null
    private var about: TextView? = null
    private var profile: CoordinatorLayout? = null
    private var profileProgress: RelativeLayout? = null
    private var preferences: SharedPreferences? = null
    private var content: NestedScrollView? = null
    private var notice: NestedScrollView? = null
    private var followButton: ImageView? = null
    private var followProgress: AVLoadingIndicatorView? = null

    private companion object {
        private const val USER_PREFERENCE = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        ctx = this
        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx?.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)

        dp = findViewById(R.id.dpOnUserProfile)
        about = findViewById(R.id.aboutOnUserProfile)
        profileProgress = findViewById(R.id.rel_userProfileProgress)
        profile = findViewById(R.id.coordinator_userProfile)
        content = findViewById(R.id.scrollView)
        notice = findViewById(R.id.noticeView)
        followButton = findViewById(R.id.followStatusOnProfile)
        followProgress = findViewById(R.id.followProgress)

        loadProfilePic(getSearchIntent())
        getFollowStatus(getSearchIntent())
        getProfile(getSearchIntent())
        setNavigationStatusBarColor()
        setUpTabLayout()
        onFollowButtonClick(getSearchIntent())
        viewFollowersFollowingList()
    }

    private fun loadProfilePic(username: String) {
        val dpURL = "http://" + getString(R.string.ip) + "/Snapbook/images/profile/$username.jpg"
        GlideApp.with(ctx!!)
                .load(dpURL)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                //.fit()
                .placeholder(R.drawable.no_dp_big)
                .into(dp!!)
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

    private fun setUpTabLayout() {
        val viewPager = findViewById<ScrollableViewPager>(R.id.userProfileViewPager)
        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment? {
                val bundle = Bundle()
                var fragment: Fragment? = null
                if (position == 0) {
                    bundle.putString("username", getSearchIntent())
                    fragment = ProfileGridViewFragment()
                    fragment.arguments = bundle
                }
                if (position == 1) {
                    bundle.putString("username", getSearchIntent())
                    fragment = ProfileListViewFragment()
                    fragment.arguments = bundle
                }
                return fragment
            }

            override fun getCount(): Int {
                return 2
            }
        }

        val navigationTabStrip = findViewById<View>(R.id.userProfileTabs) as NavigationTabStrip
        navigationTabStrip.setTitles("Snap Grid", "Snap List")
        navigationTabStrip.titleSize = 32f
        navigationTabStrip.stripColor = Color.GREEN
        navigationTabStrip.setStripWeight(6f)
        navigationTabStrip.stripFactor = 2f
        navigationTabStrip.stripType = NavigationTabStrip.StripType.POINT
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        navigationTabStrip.cornersRadius = 3f
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY
        navigationTabStrip.activeColor = Color.WHITE

        navigationTabStrip.setViewPager(viewPager, 0)
        navigationTabStrip.setTabIndex(0, true)

        viewPager.setCanScroll(false)
    }

    private fun getProfile(username: String) {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/SearchController/getUserProfile?username=$username"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            // Toast.makeText(ctx,""+response,Toast.LENGTH_SHORT).show()
            if (response.length() > 0) {
                val jsonObject = response.optJSONObject(0)

                nameOnUserProfile.text = jsonObject.optString("name")
                followers.text = jsonObject.optString("followerscount")
                following.text = jsonObject.optString("followingcount")
                posts.text = jsonObject.optString("postscount")

                profileProgress?.visibility = View.INVISIBLE
                profile?.visibility = View.VISIBLE
            }
        }, Response.ErrorListener { error ->

        })

        requestQueue?.add(jsonArrayRequest)
    }

    private fun getSearchIntent(): String {
        return intent.extras.getString("username")
    }

    private fun getFollowStatus(username: String) {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/FollowController/getFollowStatus?follower=${preferences?.getString("username", "")}&following=$username"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            followProgress?.visibility = View.INVISIBLE
            followButton?.visibility = View.VISIBLE
            when (response.toString()) {
                "follow" -> {
                    followButton?.setImageResource(R.drawable.follow)
                    followButton?.tag = "follow"
                    content?.visibility = View.INVISIBLE
                    notice?.visibility = View.VISIBLE
                    linearFollowingOnUserProfile.isClickable = false
                    linearFollowersOnUserProfile.isClickable = false
                }
                "following" -> {
                    followButton?.setImageResource(R.drawable.following)
                    followButton?.tag = "unfollow"
                    notice?.visibility = View.INVISIBLE
                    content?.visibility = View.VISIBLE
                    linearFollowingOnUserProfile.isClickable = true
                    linearFollowersOnUserProfile.isClickable = true
                }
            }
        }, Response.ErrorListener { error ->

        })

        requestQueue?.add(stringRequest)
    }

    private fun onFollowButtonClick(username: String) {
        followButton?.setOnClickListener({
            followButton?.visibility = View.INVISIBLE
            followProgress?.visibility = View.VISIBLE

            when (followButton?.tag) {
                "follow" -> doFollow(username)
                "unfollow" -> doUnFollow(username)
            }
        })
    }

    private fun doFollow(username: String) {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/FollowController/doFollow?follower=${preferences?.getString("username", "")}&following=$username"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            // Toast.makeText(ctx,""+response,Toast.LENGTH_LONG).show()
            when (response.toString()) {
                "success" -> {
                    getFollowStatus(username)
                    getProfile(getSearchIntent())
                }
                "failed" -> {
                }
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(ctx, "" + error, Toast.LENGTH_LONG).show()
        })

        requestQueue?.add(stringRequest)
    }

    private fun doUnFollow(username: String) {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/FollowController/doUnFollow?follower=${preferences?.getString("username", "")}&following=$username"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            when (response.toString()) {
                "success" -> {
                    getFollowStatus(username)
                    getProfile(getSearchIntent())
                }
                "failed" -> {
                }
            }
        }, Response.ErrorListener { error ->

        })

        requestQueue?.add(stringRequest)
    }

    private fun viewFollowersFollowingList() {
        linearFollowersOnUserProfile.setOnClickListener({
            val myIntent = Intent(ctx, FollowersActivity::class.java)
            myIntent.putExtra("username", getSearchIntent())
            startActivity(myIntent)
            Bungee.inAndOut(ctx)
        })

        linearFollowingOnUserProfile.setOnClickListener({
            val myIntent = Intent(ctx, FollowingActivity::class.java)
            myIntent.putExtra("username", getSearchIntent())
            startActivity(myIntent)
            Bungee.inAndOut(ctx)
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Bungee.slideRight(ctx)
    }
}
