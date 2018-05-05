package com.hrrock.snapbook.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.ProfileGridViewAdapter
import com.hrrock.snapbook.models.ProfilePostsGridViewModel
import com.hrrock.snapbook.networks.VolleyConnect
import org.json.JSONArray


/**
 * Created by hp-u on 2/23/2018.
 */
class ProfileGridViewFragment : Fragment() {
    private var ctx: Context? = null
    private var list: MutableList<ProfilePostsGridViewModel>? = null
    private var preferences: SharedPreferences? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var adapter: ProfileGridViewAdapter? = null
    private var recyclerView: RecyclerView? = null

    private companion object {
        private const val ACTIVITY_NUM = 4
        private const val USER_PREFERENCE = "userinfo"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_profile_grid_view, container, false)
        ctx = activity

        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)

        recyclerView = v.findViewById(R.id.gViewProfile)
        list = ArrayList()
        getPosts()

        recyclerView!!.layoutManager = GridLayoutManager(activity, 3)
        adapter = ProfileGridViewAdapter(ctx, list)
        recyclerView!!.adapter = adapter

        return v
    }

    private fun getPosts() {
        list?.clear()

        val photoURL = if (arguments != null) "http://" + ctx?.getString(R.string.ip) + "/Snapbook/index.php/PostsController/getProfilePosts?username=${arguments!!.getString("username")}"
        else "http://" + ctx?.getString(R.string.ip) + "/Snapbook/index.php/PostsController/getProfilePosts?username=" + preferences?.getString("username", "")

        jsonArrayRequest = JsonArrayRequest(photoURL, Response.Listener<JSONArray> { response ->
            list?.clear()
            for (i in 0 until response.length()) {
                val jsonObject = response.optJSONObject(i)
                val profileGridModel = ProfilePostsGridViewModel(jsonObject.optString("postid")
                        , jsonObject.optString("photo"))

                list?.add(profileGridModel)
            }
            adapter?.notifyDataSetChanged()

        }, Response.ErrorListener { error -> })

        requestQueue!!.add(jsonArrayRequest)
    }
}