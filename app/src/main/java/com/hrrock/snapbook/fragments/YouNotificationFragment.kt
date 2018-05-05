package com.hrrock.snapbook.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.YouNotificationAdapter
import com.hrrock.snapbook.models.YouNotificationModel
import com.hrrock.snapbook.networks.VolleyConnect
import org.json.JSONArray

class YouNotificationFragment : Fragment() {
    private var ctx: Context?=null
    private var preferences:SharedPreferences?=null
    private var jsonArrayRequest:JsonArrayRequest?=null
    private var requestQueue:RequestQueue?=null
    private var adapter:YouNotificationAdapter?=null
    private var notificationList:RecyclerView?=null
    private var list:MutableList<YouNotificationModel>?=null
    private var relProgress:RelativeLayout?=null
    private var relNotifications:RelativeLayout?=null
    private var relNoNotification:RelativeLayout?=null
    private var relNoNetwork:RelativeLayout?=null

    private companion object {
        private const val USER_PREFERENCES="userinfo"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v=inflater.inflate(R.layout.fragment_you_notification,container,false)
        ctx=activity

        requestQueue=VolleyConnect.getInstance().requestQueue
        preferences=ctx!!.getSharedPreferences(USER_PREFERENCES,Context.MODE_PRIVATE)

        notificationList=v.findViewById(R.id.youNotificationList)
        relProgress=v.findViewById(R.id.relProgress_onYouNotification)
        relNotifications=v.findViewById(R.id.relNotificationsOnYouNotification)
        relNoNotification=v.findViewById(R.id.relNoNotificationOnYouNotification)
        relNoNetwork=v.findViewById(R.id.relNoNetworkOnYouNotification)

        list = ArrayList()

        getNotifications()

        notificationList!!.layoutManager = LinearLayoutManager(ctx)
        adapter = YouNotificationAdapter(ctx, list)
        notificationList!!.adapter = adapter
        adapter!!.notifyDataSetChanged()

        return v
    }

    private fun getNotifications(){
        list!!.clear()
        val url="http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/NotificationController/getYouNotification?username=${preferences!!.getString("username","")}"

        jsonArrayRequest=JsonArrayRequest(url,Response.Listener<JSONArray> { response ->
            if(response.length()>0){
                (0 until response.length()).forEach { i ->
                    val jsonObject=response.optJSONObject(i)

                    val notification=YouNotificationModel(jsonObject.optString("notificationid"),
                            jsonObject.optString("uname"),
                            jsonObject.optString("username"),
                            jsonObject.optString("notification"),
                            jsonObject.optString("postid"),
                            jsonObject.optString("postphoto"),
                            jsonObject.optString("type"))

                 //   Toast.makeText(ctx, jsonObject.optString("notification")+"",Toast.LENGTH_SHORT).show()

                    list!!.add(notification)
                }
                adapter!!.notifyDataSetChanged()
                relProgress!!.visibility=View.GONE
                relNoNotification!!.visibility=View.GONE
                relNoNetwork!!.visibility=View.GONE
                relNotifications!!.visibility=View.VISIBLE
            }else{
                relProgress!!.visibility=View.GONE
                relNotifications!!.visibility=View.GONE
                relNoNetwork!!.visibility=View.GONE
                relNoNotification!!.visibility=View.VISIBLE
            }
        }, Response.ErrorListener { error ->
            relProgress!!.visibility=View.GONE
            relNotifications!!.visibility=View.GONE
            relNoNotification!!.visibility=View.GONE
            relNoNetwork!!.visibility=View.VISIBLE
        })

        requestQueue!!.add(jsonArrayRequest)
    }
}