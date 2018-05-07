package com.hrrock.snapbook.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.baoyz.widget.PullRefreshLayout
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.ManageDealsAdapter
import com.hrrock.snapbook.models.DealsModel
import com.hrrock.snapbook.networks.VolleyConnect
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.activity_manage_deals.*
import libs.mjn.prettydialog.PrettyDialog
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee
import java.util.*


class ManageDealsActivity : AppCompatActivity(), View.OnClickListener {
    private var ctx: Context? = null
    private var dealsList: ListView? = null
    private var adapter: ManageDealsAdapter? = null
    private var list: MutableList<DealsModel>? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var preferences: SharedPreferences? = null
    private var stringRequest: StringRequest? = null

    private companion object {
        private const val USER_PREFERENCE = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_deals)
        ctx = this

        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
        requestQueue = VolleyConnect.getInstance().requestQueue
        dealsList = findViewById(R.id.dealsList)

        list = ArrayList()
        getDeals()

        adapter = ManageDealsAdapter(ctx!!, R.layout.layout_manage_deals_row, list!!, this)
        dealsList!!.adapter = adapter
        adapter?.setNotifyOnChange(true)
        adapter?.notifyDataSetChanged()

        refreshMD!!.setOnRefreshListener(PullRefreshLayout.OnRefreshListener(this@ManageDealsActivity::getDeals))

        setNavigationStatusBarColor()
        onDealsListItemClick()
    }

    private fun getDeals() {
        list!!.clear()
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/DealsController/getDealsByUserName" +
                "?username=${preferences!!.getString("username", "")}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            for (i in 0 until response.length()) {
                val jsonObject = response.optJSONObject(i)
                val dealsModel = DealsModel(jsonObject.optString("dealid")
                        , jsonObject.optString("username")
                        , jsonObject.optString("name")
                        , jsonObject.optString("description")
                        , jsonObject.optString("category")
                        , jsonObject.optString("condition")
                        , jsonObject.optString("cost")
                        , jsonObject.optString("photo")
                        , jsonObject.optString("mobileno")
                        , jsonObject.optString("email")
                        , jsonObject.optString("state")
                        , jsonObject.optString("city")
                        , jsonObject.optString("date")
                        , jsonObject.optString("status"))

                //  Toast.makeText(ctx, "" + jsonObject.optString("name"), Toast.LENGTH_SHORT).show()

                list!!.add(dealsModel)
            }
            adapter?.notifyDataSetChanged()

            rel_progress.visibility = View.GONE
            refreshMD.setRefreshing(false)
            refreshMD.visibility = View.VISIBLE
        }, Response.ErrorListener { error -> })

        requestQueue?.add(jsonArrayRequest)
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.appBarColor)
        window.navigationBarColor = resources.getColor(R.color.appBarColor)
    }

    override fun onClick(view: View?) {
        val position = view?.getTag(R.id.key_position) as Int
        if (view.id == R.id.deleteOnMD) {
            //  Toast.makeText(ctx,"Found View ID",Toast.LENGTH_LONG).show()
            val dealsModel = list!![position]
            // Toast.makeText(ctx,""+dealsModel.productName,Toast.LENGTH_SHORT).show()
            confirmDeleteDialog(dealsModel.dealId, position)
        }
    }

    private fun deleteDeal(dealId: String, position: Int) {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/DealsController/deleteDealByDealId?dealid=$dealId"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            if (response == "success") {
                deleteDealfromListView(position)
                successSnack()
            } else {
                failureSnack()
            }
        }, Response.ErrorListener { error ->
            failureSnack()
        })

        requestQueue!!.add(stringRequest)
    }

    private fun confirmDeleteDialog(dealId: String, position: Int) {
        val deleteDialog = PrettyDialog(this)
        deleteDialog.setTitle("Delete Deal")
                .setMessage("Are you sure want to delete this deal?")
                .addButton("Delete", R.color.white, R.color.red) {
                    deleteDeal(dealId, position)
                    deleteDialog.dismiss()
                }
                .addButton("Cancel", R.color.black, R.color.light_gray) {
                    deleteDialog.dismiss()
                }
                .setIconTint(R.color.red)
                .setAnimationEnabled(true)
                .setGravity(Gravity.CENTER)
                .show()
    }

    private fun successSnack() {
        Snacky.builder()
                .setActivity(this@ManageDealsActivity)
                .setText("Deal has been deleted!")
                .setDuration(Snacky.LENGTH_LONG)
                .success()
                .show()
    }

    private fun failureSnack() {
        Snacky.builder()
                .setActivity(this@ManageDealsActivity)
                .setText("Error! try again")
                .setDuration(Snacky.LENGTH_LONG)
                .error()
                .show()
    }

    private fun deleteDealfromListView(position: Int) {
        val remove = adapter!!.getItem(position)
        adapter!!.remove(remove)
        adapter!!.notifyDataSetChanged()
    }

    private fun onDealsListItemClick() {
        dealsList!!.setOnItemClickListener { adapterView, view, i, l ->
            val deals = list!![i]
            val intent = Intent(ctx, ViewProductActivity::class.java)
            intent.putExtra("dealid", deals.dealId)

            startActivity(intent)
            Bungee.slideLeft(ctx)
        }
    }
}