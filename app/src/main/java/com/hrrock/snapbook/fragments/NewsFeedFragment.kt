package com.hrrock.snapbook.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.baoyz.widget.PullRefreshLayout
import com.hrrock.snapbook.R
import com.hrrock.snapbook.activities.CommentsActivity
import com.hrrock.snapbook.activities.DealsActivity
import com.hrrock.snapbook.adapters.NewsFeedAdapter
import com.hrrock.snapbook.adapters.NewsFeedAdapterOld
import com.hrrock.snapbook.models.NewsFeedModel
import com.hrrock.snapbook.networks.VolleyConnect
import com.hrrock.snapbook.utils.BottomNavigationViewHelper
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import org.json.JSONArray
import org.json.JSONException
import spencerstudios.com.bungeelib.Bungee
import java.io.UnsupportedEncodingException

/**
 * Created by hp-u on 2/18/2018.
 */
class NewsFeedFragment : Fragment(), View.OnClickListener {
    private var ctx: Context? = null
    private var menuItem: MenuItem? = null
    private var menu: Menu? = null
    private var bottomNavigationViewEx: BottomNavigationViewEx? = null
    private var preferences: SharedPreferences? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var list: MutableList<NewsFeedModel>? = null
   // private var adapter: NewsFeedAdapterOld? = null
    private var listView: ListView? = null
    private var refreshNewsFeed: PullRefreshLayout? = null
    private var dealMode: ImageView? = null
    private var progressRel:RelativeLayout?=null
    private var relNoNetwork:RelativeLayout?=null
    private var adapter: NewsFeedAdapter? = null
    private var recyclerView: RecyclerView? = null

    private companion object {
        private const val ACTIVITY_NUM = 0
        private const val POSITION_NEWS_FEED = 1
        private const val USER_PREFERENCE = "userinfo"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_news_feed, container, false)
        ctx = activity

        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)

        list = ArrayList()

        recyclerView = v.findViewById(R.id.newsFeedPostsList)
        refreshNewsFeed = v.findViewById(R.id.refreshNewsFeed)
        dealMode = v.findViewById(R.id.dealMode)
        progressRel=v.findViewById(R.id.rel_newsFeed_progress)
        relNoNetwork=v.findViewById(R.id.rel_no_network)

        setUpBottomNavigationView(v)

       // adapter = NewsFeedAdapterOld(ctx!!, R.layout.layout_news_feed_post_photo_row, list!!, this)
     //   listView!!.adapter = adapter

        recyclerView!!.layoutManager = LinearLayoutManager(ctx)
        adapter = NewsFeedAdapter(ctx!!, list!!, this)
        getPosts()
        recyclerView!!.adapter = adapter

        refreshNewsFeed!!.setOnRefreshListener(PullRefreshLayout.OnRefreshListener(
                this@NewsFeedFragment::getPosts
        ))

        jumpToDealsMode()

        return v
    }

    private fun jumpToDealsMode() {
        dealMode?.setOnClickListener({
            startActivity(Intent(ctx, DealsActivity::class.java))
            Bungee.slideLeft(ctx)
        })
    }

    private fun setUpBottomNavigationView(v: View) {
        bottomNavigationViewEx = v.findViewById(R.id.bottomNavigationView)
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx)
        BottomNavigationViewHelper.enableBottomNavigationView(ctx, activity, bottomNavigationViewEx)

        menu = bottomNavigationViewEx!!.menu
        menuItem = menu!!.getItem(ACTIVITY_NUM)
        menuItem!!.isChecked = true

        bottomNavigationViewEx!!.setIconTintList(ACTIVITY_NUM, ColorStateList.valueOf(resources.getColor(R.color.white, Resources.getSystem().newTheme())))
    }

    private fun getPosts() {
        val postsURL = "http://" + getString(R.string.ip) + "/Snapbook/index.php/PostsController/getPosts?follower=${preferences?.getString("username", "")}"

        val cache = VolleyConnect.getInstance().requestQueue.cache
        val entry = cache.get(postsURL)

        if (entry != null) {
            // fetch the data from cache
            try {
                val data = String(entry.data)
                list!!.clear()
                cache.clear()
                val jsonArray = JSONArray(data)
                //pullRefreshLayout.setRefreshing(false)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i)
                    val newsFeedModel = NewsFeedModel(jsonObject.optString("postid"),
                            jsonObject.optString("username"),
                            jsonObject.optString("caption"),
                            jsonObject.optString("timestamp"),
                            jsonObject.optString("photo"),
                            jsonObject.optString("type"),
                            jsonObject.optString("dealid"),
                            jsonObject.optString("dealname"),
                            jsonObject.optString("dealcategory"),
                            jsonObject.optString("dealcondition"),
                            jsonObject.optString("dealstatus"),
                            jsonObject.optString("dealcost"),
                            jsonObject.optString("dealdescription"),
                            jsonObject.optString("date"),
                            jsonObject.optString("pollstatus"),
                            jsonObject.optString("voteup"),
                            jsonObject.optString("votedown"),
                            jsonObject.optString("firebasetoken"))

                  //  Toast.makeText(ctx,jsonObject.optString("firebasetoken")+"",Toast.LENGTH_SHORT).show()

                    list!!.add(newsFeedModel)
                }
                refreshNewsFeed!!.setRefreshing(false)
                adapter!!.notifyDataSetChanged()

                progressRel!!.visibility = View.GONE
                refreshNewsFeed!!.visibility = View.VISIBLE
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            jsonArrayRequest = JsonArrayRequest(postsURL, Response.Listener<JSONArray> { response ->
                //   Toast.makeText(ctx, response.toString() + "", Toast.LENGTH_SHORT).show()
                list!!.clear()

                for (i in 0 until response.length()) {
                    // Toast.makeText(ctx, i.toString() + "", Toast.LENGTH_SHORT).show()
                    val jsonObject = response.optJSONObject(i)
                    val newsFeedModel = NewsFeedModel(jsonObject.optString("postid"),
                            jsonObject.optString("username"),
                            jsonObject.optString("caption"),
                            jsonObject.optString("timestamp"),
                            jsonObject.optString("photo"),
                            jsonObject.optString("type"),
                            jsonObject.optString("dealid"),
                            jsonObject.optString("dealname"),
                            jsonObject.optString("dealcategory"),
                            jsonObject.optString("dealcondition"),
                            jsonObject.optString("dealstatus"),
                            jsonObject.optString("dealcost"),
                            jsonObject.optString("dealdescription"),
                            jsonObject.optString("date"),
                            jsonObject.optString("pollstatus"),
                            jsonObject.optString("voteup"),
                            jsonObject.optString("votedown"),
                            jsonObject.optString("firebasetoken"))

                    // Toast.makeText(ctx,newsFeedModel.postId+"",Toast.LENGTH_SHORT).show()

                    list!!.add(newsFeedModel)
                }
                refreshNewsFeed!!.setRefreshing(false)
                adapter!!.notifyDataSetChanged()

                progressRel!!.visibility = View.GONE
                relNoNetwork!!.visibility=View.GONE
                refreshNewsFeed!!.visibility = View.VISIBLE
                recyclerView!!.visibility=View.VISIBLE

            }, Response.ErrorListener { error ->
                progressRel!!.visibility = View.GONE
                recyclerView!!.visibility=View.INVISIBLE
                refreshNewsFeed!!.visibility = View.VISIBLE
                refreshNewsFeed!!.setRefreshing(false)
                relNoNetwork!!.visibility=View.VISIBLE
            })

            requestQueue!!.add(jsonArrayRequest)
        }
      /*  jsonArrayRequest = JsonArrayRequest(postsURL, Response.Listener<JSONArray> { response ->
            //   Toast.makeText(ctx, response.toString() + "", Toast.LENGTH_SHORT).show()
            list!!.clear()

            for (i in 0 until response.length()) {
                // Toast.makeText(ctx, i.toString() + "", Toast.LENGTH_SHORT).show()
                val jsonObject = response.optJSONObject(i)
                val newsFeedModel = NewsFeedModel(jsonObject.optString("postid"),
                        jsonObject.optString("username"),
                        jsonObject.optString("caption"),
                        jsonObject.optString("timestamp"),
                        jsonObject.optString("photo"),
                        jsonObject.optString("type"),
                        jsonObject.optString("dealid"),
                        jsonObject.optString("dealname"),
                        jsonObject.optString("dealcategory"),
                        jsonObject.optString("dealcondition"),
                        jsonObject.optString("dealstatus"),
                        jsonObject.optString("dealcost"),
                        jsonObject.optString("dealdescription"),
                        jsonObject.optString("date"),
                        jsonObject.optString("pollstatus"),
                        jsonObject.optString("voteup"),
                        jsonObject.optString("votedown"),
                        jsonObject.optString("firebasetoken"))

                // Toast.makeText(ctx,newsFeedModel.postId+"",Toast.LENGTH_SHORT).show()

                list!!.add(newsFeedModel)
            }
            refreshNewsFeed!!.setRefreshing(false)
            adapter!!.notifyDataSetChanged()

            progressRel!!.visibility = View.GONE
            relNoNetwork!!.visibility=View.GONE
            refreshNewsFeed!!.visibility = View.VISIBLE
            listView!!.visibility=View.VISIBLE

        }, Response.ErrorListener { error ->
            progressRel!!.visibility = View.GONE
            listView!!.visibility=View.INVISIBLE
            refreshNewsFeed!!.visibility = View.VISIBLE
            refreshNewsFeed!!.setRefreshing(false)
            relNoNetwork!!.visibility=View.VISIBLE
        })

        requestQueue!!.add(jsonArrayRequest)*/
    }

    override fun onClick(view: View?) {
        val position = view?.getTag(R.id.key_position) as Int
        if (view.id == R.id.comments_OnNewsFeed) {
            // Toast.makeText(ctx, "Found View ID", Toast.LENGTH_LONG).show()
            val newsFeedModel = list!![position]

            showComments(newsFeedModel.postId)
        }
    }

    private fun showComments(postid: String) {
        val commentIntent = Intent(ctx, CommentsActivity::class.java)
        commentIntent.putExtra("postid", postid)
        startActivity(commentIntent)

        Bungee.slideLeft(ctx)
    }
}