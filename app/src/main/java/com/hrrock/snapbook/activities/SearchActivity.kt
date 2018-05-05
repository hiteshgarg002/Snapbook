package com.hrrock.snapbook.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.SearchAdapter
import com.hrrock.snapbook.models.SearchModel
import com.hrrock.snapbook.networks.VolleyConnect
import com.hrrock.snapbook.utils.BottomNavigationViewHelper
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import com.wang.avi.AVLoadingIndicatorView
import iammert.com.view.scalinglib.ScalingLayout
import iammert.com.view.scalinglib.ScalingLayoutListener
import kotlinx.android.synthetic.main.layout_search_bar.*
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee


class SearchActivity : AppCompatActivity(), View.OnClickListener {
    private var ctx: Context? = null
    private var activity: Activity? = null
    private var bottomNavigationViewEx: BottomNavigationViewEx? = null
    private var menuItem: MenuItem? = null
    private var menu: Menu? = null
    private var searchLayout: RelativeLayout? = null
    private var textViewSearch: TextView? = null
    private var scalingLayout: ScalingLayout? = null
    private var searchText: EditText? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var adapter: SearchAdapter? = null
    private var list: MutableList<SearchModel>? = null
    private var listView: ListView? = null
    private var progress: AVLoadingIndicatorView? = null
    private var preferences: SharedPreferences? = null
    private var searchWM: ImageView? = null
    private var notFoundText: TextView? = null
    private var relNoResult: RelativeLayout? = null

    private companion object {
        private const val ACTIVITY_NUM = 1
        private const val USER_PREFERENCE = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        ctx = this
        activity = this
        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx?.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)

        searchLayout = findViewById(R.id.searchLayout)
        textViewSearch = findViewById(R.id.textViewSearch)
        scalingLayout = findViewById(R.id.scalingLayout)
        searchText = findViewById(R.id.searchText)
        progress = findViewById(R.id.searchProgress)
        searchWM = findViewById(R.id.searchWatermark)
        notFoundText = findViewById(R.id.notfoundText)
        relNoResult = findViewById(R.id.rel_no_result)

        setUpBottomNavigationView()
        setNavigationStatusBarColor()
        searchViewAnim()
        prepareListAndAdapter()
        onSearchTextChange()
        onListItemClick()
    }

    private fun setUpBottomNavigationView() {
        bottomNavigationViewEx = findViewById(R.id.bottomNavigationView)
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx)
        BottomNavigationViewHelper.enableBottomNavigationView(ctx, activity, bottomNavigationViewEx)

        menu = bottomNavigationViewEx!!.menu
        menuItem = menu!!.getItem(ACTIVITY_NUM)
        menuItem!!.isChecked = true

        bottomNavigationViewEx!!.setIconTintList(ACTIVITY_NUM, ColorStateList.valueOf(resources.getColor(R.color.white, Resources.getSystem().newTheme())))
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

    private fun searchViewAnim() {
        scalingLayout?.setListener(object : ScalingLayoutListener {
            override fun onCollapsed() {
                ViewCompat.animate(textViewSearch).alpha(1f).setDuration(150).start()
                ViewCompat.animate(searchLayout).alpha(0f).setDuration(150).setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        textViewSearch?.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(view: View) {
                        searchLayout?.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(view: View) {

                    }
                }).start()
            }

            override fun onExpanded() {
                ViewCompat.animate(textViewSearch).alpha(0f).setDuration(200).start()
                ViewCompat.animate(searchLayout).alpha(1f).setDuration(200).setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        searchLayout?.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(view: View) {
                        textViewSearch?.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(view: View) {

                    }
                }).start()
            }

            override fun onProgress(progress: Float) {

            }
        })
        scalingLayout?.setOnClickListener {
            if (scalingLayout?.state == iammert.com.view.scalinglib.State.COLLAPSED) {
                scalingLayout?.expand()
            }
        }

        findViewById<View>(R.id.rootLayout).setOnClickListener {
            if (scalingLayout?.state == iammert.com.view.scalinglib.State.EXPANDED) {
                scalingLayout?.collapse()
            }
        }

        arrowBack.setOnClickListener({
            if (scalingLayout?.state == iammert.com.view.scalinglib.State.EXPANDED) {
                scalingLayout?.collapse()

                val view = this.currentFocus
                if (view != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                searchText!!.text.clear()
            }
        })
    }

    private fun onSearchTextChange() {
        searchText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                progress?.visibility = View.VISIBLE

                if (searchText!!.text.toString() == "") {
                    progress?.visibility = View.INVISIBLE
                    searchWM?.visibility = View.VISIBLE
                    //   listView?.visibility= View.INVISIBLE
                    list!!.clear()
                    adapter?.clear()
                    adapter?.notifyDataSetInvalidated()
                    adapter?.notifyDataSetChanged()
                } else {
                    list!!.clear()
                    adapter?.clear()
                    adapter?.notifyDataSetInvalidated()
                    adapter?.notifyDataSetChanged()
                    searchWM?.visibility = View.INVISIBLE
                    //  listView?.visibility= View.VISIBLE

                    if (searchText!!.text.toString().contains(" ")) {
                        val search = searchText!!.text.toString().replace(" ", "+")
                        getSearchResult(search)
                    } else {
                        getSearchResult(searchText!!.text.toString())
                    }
                }
            }
        })
    }

    private fun getSearchResult(search: String) {
        list?.clear()
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/SearchController/searchUser?search=$search&user=${preferences?.getString("username", "")}"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            if (response == "invalid") {
                progress?.visibility = View.INVISIBLE
                listView?.visibility = View.INVISIBLE
                notFoundText?.text = searchText!!.text
                relNoResult?.visibility = View.VISIBLE
                searchWM?.visibility = View.VISIBLE
            } else {
                searchWM?.visibility = View.INVISIBLE
                relNoResult?.visibility = View.INVISIBLE
                listView?.visibility = View.VISIBLE
                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.optJSONObject(i)

                    //  Toast.makeText(ctx,""+jsonObject.optString("followstatus"),Toast.LENGTH_LONG).show()

                    val searchModel = SearchModel(jsonObject.optString("username")
                            , jsonObject.optString("name")
                            , jsonObject.optString("profile")
                            , jsonObject.optString("followstatus"))

                    list?.add(searchModel)
                }
                adapter!!.notifyDataSetChanged()
                progress?.visibility = View.INVISIBLE
            }
        }, Response.ErrorListener { error ->
            progress?.visibility = View.INVISIBLE
        })

        requestQueue?.add(stringRequest)
    }

    private fun prepareListAndAdapter() {
        listView = findViewById(R.id.searchList)
        list = ArrayList()
        adapter = SearchAdapter(ctx!!, R.layout.layout_search_row, list!!, this)

        listView?.adapter = adapter
    }

    private fun onListItemClick() {
        listView?.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val searchModel = list!![i]

            if (searchModel.userName == preferences?.getString("username", "")) {
                val myIntent = Intent(ctx, ProfileActivity::class.java)
                startActivity(myIntent)
                Bungee.slideLeft(ctx)
            } else {
                val myIntent = Intent(ctx, UserProfileActivity::class.java)
                myIntent.putExtra("username", searchModel.userName)
                startActivity(myIntent)
                Bungee.slideLeft(ctx)
            }
        }
    }

    override fun onClick(view: View?) {
        val position = view?.getTag(R.id.key_position) as Int
        if (view.id == R.id.followStatusOnSearch) {
            //  Toast.makeText(ctx,"Found View ID",Toast.LENGTH_LONG).show()
            when (view.tag) {
                "follow" -> {
                    progress?.visibility = View.VISIBLE
                    view.visibility = View.INVISIBLE
                    doFollow(list!![position].userName, view)
                }
                "following" -> {
                    progress?.visibility = View.VISIBLE
                    view.visibility = View.INVISIBLE
                    doUnFollow(list!![position].userName, view)
                }
                "me" -> {
                }
            }
        }
    }

    private fun doFollow(username: String, view: View) {
        Toast.makeText(ctx, "Follow", Toast.LENGTH_LONG).show()
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/FollowController/doFollow?follower=${preferences?.getString("username", "")}&following=$username"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            // Toast.makeText(ctx,""+response,Toast.LENGTH_LONG).show()
            when (response.toString()) {
                "success" -> {
                    progress?.visibility = View.INVISIBLE
                    view.visibility = View.VISIBLE
                    getSearchResult(searchText?.text.toString())
                }
                "failed" -> {
                    progress?.visibility = View.INVISIBLE
                }
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(ctx, "" + error, Toast.LENGTH_LONG).show()
            progress?.visibility = View.INVISIBLE
        })

        requestQueue?.add(stringRequest)
    }

    private fun doUnFollow(username: String, view: View) {
        Toast.makeText(ctx, "UnFollow", Toast.LENGTH_LONG).show()
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/FollowController/doUnFollow?follower=${preferences?.getString("username", "")}&following=$username"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            when (response.toString()) {
                "success" -> {
                    progress?.visibility = View.INVISIBLE
                    view.visibility = View.VISIBLE
                    getSearchResult(searchText?.text.toString())
                }
                "failed" -> {
                }
            }
        }, Response.ErrorListener { error ->

        })

        requestQueue?.add(stringRequest)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(ctx, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        //overridePendingTransition(0, 0)
        Bungee.fade(ctx)
        activity!!.finish()
    }
}
