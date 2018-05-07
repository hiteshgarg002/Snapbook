package com.hrrock.snapbook.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.HomePagerAdapter
import com.hrrock.snapbook.networks.VolleyConnect
import de.mateware.snacky.Snacky
import java.io.File

class HomeActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var activity: Activity? = null
    private var viewPager: ViewPager? = null
    private var manager: FragmentManager? = null
    private var preferences: SharedPreferences? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null

    private companion object {
        private const val POSITION_NEWS_FEED = 1
        private const val USER_PREFERENCES = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ctx = this
        activity = this

        requestQueue = VolleyConnect.getInstance().requestQueue

        setUpViewPager()
        createApplicationFolder()
        setNavigationStatusBarColor()
        checkMomentUploadStatus()

        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)
        //  Toast.makeText(ctx,preferences!!.getString("firebaseToken","")+"", Toast.LENGTH_SHORT).show()
        // Toast.makeText(ctx,preferences!!.getString("firebaseID","")+"", Toast.LENGTH_LONG).show()
        FirebaseMessaging.getInstance().subscribeToTopic("global")

        storeFirebaseToken()
    }

    private fun storeFirebaseToken() {
        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/CredentialController/storeFirebaseToken?" +
                "token=${preferences!!.getString("firebaseToken", "")}&email=${preferences!!.getString("email", "")}"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->

        }, Response.ErrorListener { error -> })

        requestQueue!!.add(stringRequest)
    }

    private fun setUpViewPager() {
        viewPager = findViewById(R.id.homeVP)
        manager = supportFragmentManager
        viewPager!!.adapter = HomePagerAdapter(manager)
        viewPager!!.currentItem = POSITION_NEWS_FEED
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

    private fun createApplicationFolder() {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()
    }

    private fun checkMomentUploadStatus() {
        if (intent.flags == Intent.FLAG_ACTIVITY_NEW_TASK) {
            val status = intent.getBooleanExtra("status", false)
            if (status) {
                successSnack()
            } else {
                failureSnack()
            }
        }
    }

    private fun successSnack() {
        Snacky.builder()
                .setActivity(activity)
                .setText("Posted")
                .setDuration(Snacky.LENGTH_LONG)
                .success()
                .show()
    }

    private fun failureSnack() {
        Snacky.builder()
                .setActivity(activity)
                .setText("Error uploading post!")
                .setDuration(Snacky.LENGTH_LONG)
                .error()
                .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activity!!.finish()
    }
}
