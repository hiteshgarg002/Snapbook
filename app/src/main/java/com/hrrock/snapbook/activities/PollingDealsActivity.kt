package com.hrrock.snapbook.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.PollingDealsAdapter
import com.hrrock.snapbook.models.PollingDealsModel
import com.hrrock.snapbook.networks.VolleyConnect
import kotlinx.android.synthetic.main.activity_polling_deals.*
import kotlinx.android.synthetic.main.snippet_top_polling_deals.*
import org.json.JSONArray

class PollingDealsActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: PollingDealsAdapter? = null
    private var list: MutableList<PollingDealsModel>? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var preferences: SharedPreferences? = null
    private var jsonArrayRequest: JsonArrayRequest? = null

    private companion object {
        private const val USER_PREFERENCES = "userinfo"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polling_deals)
        ctx = this

        requestQueue = VolleyConnect.getInstance().requestQueue
        recyclerView = findViewById(R.id.polling_dealsRecyclerView)
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)
        setUpRecyclerView()

        setNavigationStatusBarColor()

        back.setOnClickListener({
            onBackPressed()
        })
    }

    private fun setUpRecyclerView() {
        list = ArrayList()

        recyclerView!!.layoutManager = LinearLayoutManager(ctx)

        // recyclerView!!.adapter = null
        adapter = PollingDealsAdapter(ctx, list)
        recyclerView!!.adapter = adapter
        getPollingDeals()

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

        window.navigationBarColor = resources.getColor(R.color.appBarColor, null)
    }

    private fun getPollingDeals() {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/PollingController/getPollingDeals?username=${preferences!!.getString("username","")}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            if(response.length()>0){
                (0 until response.length()).forEach { i ->
                    val jsonObject=response.optJSONObject(i)
                    val dealsModel=PollingDealsModel(jsonObject.optString("postid")
                            ,jsonObject.optString("dealid"),
                            jsonObject.optString("dealname"),
                            jsonObject.optString("date"))

                    list!!.add(dealsModel)
                }
                adapter!!.notifyDataSetChanged()
                rel_pollingDeals_progress.visibility= View.GONE
                rel_pollingDeals.visibility=View.VISIBLE
            }else{

            }
        }, Response.ErrorListener { error -> })

        requestQueue!!.add(jsonArrayRequest)
    }
}
