package com.hrrock.snapbook.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.FollowersAdapter
import com.hrrock.snapbook.models.FollowModel
import com.hrrock.snapbook.networks.VolleyConnect
import kotlinx.android.synthetic.main.activity_followers.*
import kotlinx.android.synthetic.main.snippet_top_followers.*
import org.json.JSONArray

class FollowersActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: FollowersAdapter? = null
    private var list: MutableList<FollowModel>? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var preferences: SharedPreferences? = null
    private var relFollowers: RelativeLayout? = null
    private var jsonArrayRequest: JsonArrayRequest? = null

    private companion object {
        private const val USER_PREFERENCES = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers)
        ctx = this

        requestQueue = VolleyConnect.getInstance().requestQueue
        recyclerView = findViewById(R.id.followersRecyclerView)
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)
        relFollowers = findViewById(R.id.rel_recycler_followers)
        setUpFollowersRecyclerView()

        setNavigationStatusBarColor()

        onSearchTextChange()
        back.setOnClickListener({
            onBackPressed()
        })
    }

    private fun setUpFollowersRecyclerView() {
        list = ArrayList()

        recyclerView!!.layoutManager = LinearLayoutManager(ctx)

        // recyclerView!!.adapter = null
        adapter = FollowersAdapter(ctx, list)
        recyclerView!!.adapter = adapter
        getFollowers()

        adapter!!.notifyDataSetChanged()
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.appBarColor)

        window.navigationBarColor = resources.getColor(R.color.colorGray, null)
    }

    private fun getFollowers() {
        list!!.clear()
        adapter!!.notifyDataSetChanged()

        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/FollowController/getFollowers?" +
                "username=${if (getUsernameIntent() != "") {
                    getUsernameIntent()
                } else {
                    preferences!!.getString("username", "")
                }}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            if (response.length() > 0) {
                (0 until response.length()).forEach { i ->
                    val jsonObject = response.optJSONObject(i)
                    val followModel = FollowModel(jsonObject.optString("following"),
                            jsonObject.optString("follower"),
                            jsonObject.optString("followername"),
                            jsonObject.optString("followingstatus"),
                            jsonObject.optString("followerphoto"))

                    list!!.add(followModel)
                }
                adapter!!.notifyDataSetChanged()
                rel_progressFollowers.visibility = View.INVISIBLE
                rel_no_result.visibility = View.INVISIBLE
                relFollowers!!.visibility = View.VISIBLE
            } else {
                rel_progressFollowers.visibility = View.INVISIBLE
            }
        }, Response.ErrorListener { error -> })

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun onSearchTextChange() {
        searchFollowers.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                if (!text!!.isEmpty()) {
                    relFollowers!!.visibility = View.GONE
                    rel_no_result.visibility = View.INVISIBLE
                    rel_progressFollowers.visibility = View.VISIBLE
                    searchFollowers(text.toString())
                } else {
                    rel_no_result.visibility = View.INVISIBLE
                    getFollowers()
                    //setUpFollowersRecyclerView()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun searchFollowers(search: String) {
        list!!.clear()
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/FollowController/getFollowerBySearch?" +
                "follower=$search&following=${if (getUsernameIntent() != "") {
                    getUsernameIntent()
                } else {
                    preferences!!.getString("username", "")
                }}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            //   Toast.makeText(ctx,""+response,Toast.LENGTH_SHORT).show()
            if (response.length() > 0) {
                (0 until response.length()).forEach { i ->
                    val jsonObject = response.optJSONObject(i)
                    val followModel = FollowModel(jsonObject.optString("following"),
                            jsonObject.optString("follower"),
                            jsonObject.optString("followername"),
                            jsonObject.optString("followingstatus"),
                            jsonObject.optString("followerphoto"))

                    // Toast.makeText(ctx,""+jsonObject.optString("followername"),Toast.LENGTH_SHORT).show()

                    list!!.add(followModel)
                }
                adapter!!.notifyDataSetChanged()
                rel_progressFollowers.visibility = View.INVISIBLE
                relFollowers!!.visibility = View.VISIBLE
            } else {
                rel_progressFollowers.visibility = View.INVISIBLE
                rel_no_result.visibility = View.VISIBLE
            }
        }, Response.ErrorListener { error -> })

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun getUsernameIntent(): String {
        return if (intent.hasExtra("username")) {
            intent.extras.getString("username")
        } else {
            ""
        }
    }
}
