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

class FollowingNotificationFragment : Fragment() {
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
        val v=inflater.inflate(R.layout.fragment_following_notification,container,false)
        ctx=activity

        requestQueue=VolleyConnect.getInstance().requestQueue
        preferences=ctx!!.getSharedPreferences(USER_PREFERENCES,Context.MODE_PRIVATE)

        return v
    }
}