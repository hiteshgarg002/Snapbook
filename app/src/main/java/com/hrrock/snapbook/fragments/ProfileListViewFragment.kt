package com.hrrock.snapbook.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.activities.CommentsActivity
import com.hrrock.snapbook.adapters.ProfileListViewAdapter
import com.hrrock.snapbook.models.ProfilePostsListViewModel
import com.hrrock.snapbook.networks.VolleyConnect
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee

class ProfileListViewFragment : Fragment(), View.OnClickListener {
    private var ctx: Context? = null
    private var list: MutableList<ProfilePostsListViewModel>? = null
    private var preferences: SharedPreferences? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var adapter: ProfileListViewAdapter? = null
    private var recyclerView: RecyclerView? = null

    private companion object {
        private const val ACTIVITY_NUM = 4
        private const val USER_PREFERENCE = "userinfo"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_profile_list_view, container, false)
        ctx = activity

        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
        recyclerView = v.findViewById(R.id.profilePostsRecyclerView)

        list = ArrayList()

        getPosts()

        recyclerView!!.layoutManager = LinearLayoutManager(ctx)
        adapter = ProfileListViewAdapter(ctx, list, this)
        recyclerView!!.adapter = adapter

        return v
    }

    private fun getPosts() {
        list?.clear()

        val postsURL = if (arguments != null) "http://${getString(R.string.ip)}/Snapbook/index.php/PostsController/getProfilePostsListView?username=${arguments!!.getString("username")}"
        else "http://" + ctx?.getString(R.string.ip) + "/Snapbook/index.php/PostsController/getProfilePostsListView?username=${preferences!!.getString("username", "")}"

        jsonArrayRequest = JsonArrayRequest(postsURL, Response.Listener<JSONArray> { response ->
            //   Toast.makeText(ctx, response.toString() + "", Toast.LENGTH_SHORT).show()
            list!!.clear()

            for (i in 0 until response.length()) {
                // Toast.makeText(ctx, i.toString() + "", Toast.LENGTH_SHORT).show()
                val jsonObject = response.optJSONObject(i)
                val newsFeedModel = ProfilePostsListViewModel(jsonObject.optString("postid"),
                        jsonObject.optString("username"),
                        jsonObject.optString("caption"),
                        jsonObject.optString("timestamp"),
                        jsonObject.optString("photo"),
                        jsonObject.optString("type"),
                        jsonObject.optString("date"),
                        jsonObject.optString("pollstatus"),
                        jsonObject.optString("voteup"),
                        jsonObject.optString("votedown"),
                        jsonObject.optString("firebasetoken"))

                // Toast.makeText(ctx,newsFeedModel.postId+"",Toast.LENGTH_SHORT).show()

                list!!.add(newsFeedModel)
            }
            adapter!!.notifyDataSetChanged()

        }, Response.ErrorListener { error ->
            Toast.makeText(ctx, "" + error, Toast.LENGTH_SHORT).show()
        })

        requestQueue!!.add(jsonArrayRequest)
    }

    override fun onClick(view: View?) {
        val position = view?.getTag(R.id.key_position) as Int
        if (view.id == R.id.comments_OnProfilePostsListView) {
            // Toast.makeText(ctx, "Found View ID", Toast.LENGTH_LONG).show()
            val listViewModel = list!![position]

            showComments(listViewModel.postId)
        }
    }

    private fun showComments(postid: String) {
        val commentIntent = Intent(ctx, CommentsActivity::class.java)
        commentIntent.putExtra("postid", postid)
        startActivity(commentIntent)

        Bungee.slideLeft(ctx)
    }
}