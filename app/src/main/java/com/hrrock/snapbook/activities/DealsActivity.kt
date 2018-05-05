package com.hrrock.snapbook.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.github.ksoichiro.android.observablescrollview.ObservableListView
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks
import com.github.ksoichiro.android.observablescrollview.ScrollState
import com.github.ksoichiro.android.observablescrollview.ScrollUtils
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.DealsAdapter
import com.hrrock.snapbook.models.DealsModel
import com.hrrock.snapbook.networks.VolleyConnect
import com.nineoldandroids.view.ViewHelper
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import de.mateware.snacky.Snacky
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.activity_deals.*
import libs.mjn.prettydialog.PrettyDialog
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee
import java.text.SimpleDateFormat
import java.util.*


class DealsActivity : AppCompatActivity(), ObservableScrollViewCallbacks, View.OnClickListener {

    private var ctx: Context? = null
    private var mHeaderView: View? = null
    private var mToolbarView: View? = null
    private var dealsList: ObservableListView? = null
    private var adapter: DealsAdapter? = null
    private var list: MutableList<DealsModel>? = null
    private var mBaseTranslationY: Int = 0
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var stringRequest: StringRequest? = null
    private var shimmerLayout: ShimmerLayout? = null
    private var searchListView: ListView? = null
    private var searchList: MutableList<String>? = null
    private var searchAdapter: ArrayAdapter<String>? = null
    private var preferences: SharedPreferences? = null
    private var back: ImageView? = null
    private var powerMenu: PowerMenu? = null

    private companion object {
        private const val USER_PREFERENCES = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deals)
        ctx = this

        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        searchListView = findViewById(R.id.searchListOnDeals)
        back = findViewById(R.id.back)
        shimmerLayout = findViewById(R.id.shimmer_view_deals)
        shimmerLayout?.startShimmerAnimation()

        list = ArrayList()
        searchList = ArrayList()
        list?.clear()
        // getDeals()
        getDealsInitially()

        mHeaderView = findViewById(R.id.header)
        ViewCompat.setElevation(mHeaderView, resources.getDimension(R.dimen.toolbar_elevation))
        mToolbarView = findViewById(R.id.toolbar)

        dealsList = findViewById(R.id.list)
        dealsList?.setScrollViewCallbacks(this)

        val inflater = LayoutInflater.from(this)
        dealsList?.addHeaderView(inflater.inflate(R.layout.padding, dealsList, false)) // toolbar
        dealsList?.addHeaderView(inflater.inflate(R.layout.padding, dealsList, false)) // sticky view

        // ObservableListView uses setOnScrollListener, but it still works.
        dealsList?.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
        })

        adapter = DealsAdapter(ctx!!, R.layout.layout_deals_row, list!!, this)
        dealsList!!.adapter = adapter
        adapter?.setNotifyOnChange(true)
        adapter?.notifyDataSetChanged()

        setNavigationStatusBarColor()
        checkIfDealPosted()
        checkManageDealsIntent()
        onDealSelected()
        onFabClick()
        onOptionsClick()
        setSearchAdapter()
        onSearchTextChanged()
        onSearchListViewItemClick()
    }

    override fun onUpOrCancelMotionEvent(scrollState: ScrollState?) {
        mBaseTranslationY = 0

        if (scrollState === ScrollState.DOWN) {
            showToolbar()
        } else if (scrollState === ScrollState.UP) {
            val toolbarHeight = mToolbarView?.height
            val scrollY = dealsList?.currentScrollY
            if (toolbarHeight!! <= scrollY!!) {
                hideToolbar()
            } else {
                showToolbar()
            }
        } else {
            // Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
            if (!toolbarIsShown() && !toolbarIsHidden()) {
                // Toolbar is moving but doesn't know which to move:
                // you can change this to hideToolbar()
                showToolbar()
            }
        }
    }

    override fun onScrollChanged(scrollY: Int, firstScroll: Boolean, dragging: Boolean) {
        if (dragging) {
            val toolbarHeight = mToolbarView?.height
            if (firstScroll) {
                val currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView)
                if (-toolbarHeight!! < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY
                }
            }
            val headerTranslationY = ScrollUtils.getFloat((-(scrollY - mBaseTranslationY)).toFloat(), (-toolbarHeight!!).toFloat(), 0f)
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).cancel()
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY)
        }
    }

    override fun onDownMotionEvent() {}

    private fun toolbarIsShown(): Boolean {
        return ViewHelper.getTranslationY(mHeaderView) == 0f
    }

    private fun toolbarIsHidden(): Boolean {
        return ViewHelper.getTranslationY(mHeaderView) == (-mToolbarView?.height!!).toFloat()
    }

    private fun showToolbar() {
        val headerTranslationY = ViewHelper.getTranslationY(mHeaderView)
        if (headerTranslationY != 0f) {
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).cancel()
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).translationY(0F).setDuration(200).start()
        }
    }

    private fun hideToolbar() {
        val headerTranslationY = ViewHelper.getTranslationY(mHeaderView)
        val toolbarHeight = mToolbarView?.height
        if (headerTranslationY != (-toolbarHeight!!).toFloat()) {
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).cancel()
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderView).translationY((-toolbarHeight).toFloat()).setDuration(200).start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //method invoke when we click back button in toolbar
        onBackPressed()
        return true
    }

    private fun getDeals() {
        list!!.clear()
        adapter?.clear()
        adapter?.notifyDataSetChanged()
        // adapter?.notifyDataSetInvalidated()

        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/DealsController/getDeals"
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

                //Toast.makeText(ctx, "" + jsonObject.optString("dealid"), Toast.LENGTH_SHORT).show()

                list!!.add(dealsModel)
            }
            adapter?.notifyDataSetChanged()
            shimmerLayout?.stopShimmerAnimation()
            shimmerLayout?.visibility = View.INVISIBLE

            dealsList?.visibility = View.VISIBLE
        }, Response.ErrorListener { error ->

        })

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
        window.navigationBarColor = resources.getColor(R.color.colorLightGray)
    }

    private fun checkIfDealPosted() {
        if (intent.flags == Intent.FLAG_ACTIVITY_FORWARD_RESULT) {
            getDeals()
            successSnack()
        }
    }

    private fun checkManageDealsIntent() {
        if (intent.flags == Intent.FLAG_ACTIVITY_NEW_DOCUMENT) {
            getDeals()
        }
    }

    private fun getDealsInitially() {
        if (intent.flags != Intent.FLAG_ACTIVITY_FORWARD_RESULT) {
            getDeals()
        }
    }

    private fun successSnack() {
        Snacky.builder()
                .setActivity(this)
                .setText("Deal has been Posted!")
                .setDuration(Snacky.LENGTH_LONG)
                .success()
                .show()
    }

    private fun onDealSelected() {
        dealsList?.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val model: DealsModel = list?.get(i - 2)!!
            //Toast.makeText(ctx, "" + model.dealId, Toast.LENGTH_SHORT).show()

            val intent = Intent(ctx, ViewProductActivity::class.java)
            intent.putExtra("dealid", model.dealId)

            startActivity(intent)
            Bungee.slideLeft(ctx)
        }
    }

    private fun onFabClick() {
        deals_fab?.setOnClickListener({ view ->
            when (view.id) {
                R.id.fab_create_deal -> {
                    // Toast.makeText(ctx,"edit",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(ctx, CreateDealActivity::class.java))
                    Bungee.inAndOut(ctx)
                    finish()
                }
                R.id.fab_sort_by -> {
                    Toast.makeText(ctx, "saved", Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        list?.clear()
        adapter?.clear()
        adapter?.notifyDataSetInvalidated()
        adapter?.notifyDataSetChanged()
    }

    private fun onOptionsClick() {
        optionsOnDeals.setOnClickListener({ view ->
            showOptions(view)
        })
    }

    private fun showOptions(view: View) {
        /*val popupMenu = popupMenu {
            style = R.style.Widget_MPM_Menu_Dark
            section {
                item {
                    label = "Manage Deals"
                    iconDrawable = ContextCompat.getDrawable(this@DealsActivity, R.drawable.ic_action_manage)
                    callback = {
                        //optional
                        // Toast.makeText(this@DealsActivity, "Copied!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(ctx, ManageDealsActivity::class.java))
                        Bungee.slideLeft(ctx)
                    }
                }
                item {
                    label = "Polling"
                    iconDrawable = ContextCompat.getDrawable(this@DealsActivity, R.drawable.ic_action_polling)
                    callback = {
                        //optional
                        //   Toast.makeText(this@DealsActivity, "Text pasted!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(ctx, PollingDealsActivity::class.java))
                        Bungee.slideLeft(ctx)
                    }
                }
            }
        }
        popupMenu.show(this@DealsActivity, view)*/

        powerMenu = PowerMenu.Builder(ctx)
                .addItem(PowerMenuItem("Manage Deals", false))
                .addItem(PowerMenuItem("Polling Deals", false))
                .setAnimation(MenuAnimation.DROP_DOWN)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setTextColor(ctx!!.resources.getColor(R.color.black, null))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ctx!!.resources.getColor(R.color.colorPrimary, null))
                .setOnMenuItemClickListener({ position, item ->
                    when (position) {
                        0 -> {
                            powerMenu!!.dismiss()
                            startActivity(Intent(ctx, ManageDealsActivity::class.java))
                            Bungee.slideLeft(ctx)

                        }
                        1 -> {
                            powerMenu!!.dismiss()
                            startActivity(Intent(ctx, PollingDealsActivity::class.java))
                            Bungee.slideLeft(ctx)
                        }
                    }
                })
                .build()

        powerMenu!!.showAsDropDown(view)
    }

    private fun getSearchResult(search: String) {
        searchList!!.clear()
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/DealsController/getSearchedDealName?search=$search"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            for (i in 0 until response.length()) {
                val jsonObject = response.optJSONObject(i)
                searchList!!.add("" + jsonObject.optString("name"))
                // Toast.makeText(ctx,""+jsonObject.optString("name"),Toast.LENGTH_SHORT).show()
            }

            searchAdapter!!.notifyDataSetChanged()

            if (searchList!!.size > 0) {
                searchListView!!.visibility = View.VISIBLE
            } else {
                searchListView!!.visibility = View.GONE
            }
        }, Response.ErrorListener { error -> })

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun onSearchTextChanged() {
        search_deal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (search_deal.text.toString() != "") {
                    getSearchResult(search_deal.text.toString())
                } else {
                    searchListView!!.visibility = View.GONE
                }
            }
        })
    }

    private fun setSearchAdapter() {
        searchAdapter = ArrayAdapter(ctx, R.layout.layout_search_deal_list_black_text, R.id.list_content, searchList)
        searchListView!!.adapter = searchAdapter
        searchAdapter!!.notifyDataSetChanged()
    }

    private fun onSearchListViewItemClick() {
        searchListView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val productName = searchList!![i]
            val sIntent = Intent(ctx, SearchedDealActivity::class.java)
            sIntent.putExtra("productname", productName)
            startActivity(sIntent)

            Bungee.inAndOut(ctx)
        }
    }

    override fun onRestart() {
        super.onRestart()
        getDeals()

        Log.d("DealsActivity", "OnRestart running")
    }

    private fun getCurrentDate(): String {
        val timeStamp = System.currentTimeMillis() / 1000L
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(timeStamp * 1000L)
    }

    override fun onClick(view: View?) {
        val position = view?.getTag(R.id.key_position) as Int
        if (view.id == R.id.productShareOnDeals) {
            // Toast.makeText(ctx,"Found View ID",Toast.LENGTH_LONG).show()
            // val dealsModel = list!![position]
            // Toast.makeText(ctx,""+dealsModel.productName,Toast.LENGTH_SHORT).show()
            confirmPollingDialog(position)
        }
    }

    private fun confirmPollingDialog(position: Int) {
        val pollingDialog = PrettyDialog(this)
        pollingDialog.setTitle("Poll")
                .setMessage("Start Polling fo this deal?")
                .addButton("Yes", R.color.black, R.color.light_gray) {
                    startPolling(position)
                    pollingDialog.dismiss()
                }
                .addButton("Cancel", R.color.white, R.color.red) {
                    pollingDialog.dismiss()
                }
                .setIcon(R.drawable.ic_action_post_on_feed)
                .setIconTint(R.color.dark_green)
                .setAnimationEnabled(true)
                .setGravity(Gravity.CENTER)
                .show()
    }

    private fun startPolling(position: Int) {
        val dealsModel = list!![position]
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/PollingController/createPollingDeal?" +
                "dealid=${dealsModel.dealId}&username=${preferences!!.getString("username", "")}&dealname=${dealsModel.productName.replace(" ", "+")}" +
                "&dealdescription=${dealsModel.productDescription.replace(" ", "+")}&dealcategory=${dealsModel.productCategory.replace(" ", "+")}&dealcondition=${dealsModel.productCondition}" +
                "&dealcost=${dealsModel.productCost}&dealstatus=${dealsModel.productStatus.replace(" ", "+")}&date=${getCurrentDate()}&type=deal"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            if (response == "success") {
                pollingSuccessSnack()
            } else {
                pollingFailureSnack()
            }
        }, Response.ErrorListener { error ->
            pollingFailureSnack()
        })

        requestQueue!!.add(stringRequest)
    }

    private fun pollingSuccessSnack() {
        Snacky.builder()
                .setActivity(this@DealsActivity)
                .setText("Polling has been started!")
                .setDuration(Snacky.LENGTH_LONG)
                .success()
                .show()
    }

    private fun pollingFailureSnack() {
        Snacky.builder()
                .setActivity(this@DealsActivity)
                .setText("Error! try again")
                .setDuration(Snacky.LENGTH_LONG)
                .error()
                .show()
    }
}