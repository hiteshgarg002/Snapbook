package com.hrrock.snapbook.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hrrock.snapbook.R
import com.hrrock.snapbook.networks.VolleyConnect
import com.hrrock.snapbook.utils.SquareImageView
import de.mateware.snacky.Snacky
import iammert.com.view.scalinglib.ScalingLayout
import iammert.com.view.scalinglib.ScalingLayoutListener
import iammert.com.view.scalinglib.State
import kotlinx.android.synthetic.main.activity_view_product.*
import kotlinx.android.synthetic.main.snippet_fab_view_product.*
import kotlinx.android.synthetic.main.snippet_top_view_product.*
import libs.mjn.prettydialog.PrettyDialog
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*


class ViewProductActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var stringRequest: StringRequest? = null
    private var preferences: SharedPreferences? = null
    private var fabIcon: ImageView? = null
    private var filterLayout: LinearLayout? = null
    private var scalingLayout: ScalingLayout? = null
    private var scalingRoot: RelativeLayout? = null
    private var status: TextView? = null
    private var condition: TextView? = null
    private var category: TextView? = null
    private var pname: TextView? = null
    private var cost: TextView? = null
    private var description: TextView? = null
    private var username: TextView? = null
    private var uname: TextView? = null
    private var email: TextView? = null
    private var mobileno: TextView? = null
    private var productPhoto: SquareImageView? = null

    private companion object {
        private const val USER_PREFERENCES = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_product)
        ctx = this
        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)

        fabIcon = findViewById(R.id.fabIcon)
        filterLayout = findViewById(R.id.filterLayout)
        scalingLayout = findViewById(R.id.scalingLayout)
        scalingRoot = findViewById(R.id.rootLayoutScaling)

        status = findViewById(R.id.productStatusOnViewProduct)
        condition = findViewById(R.id.productConditionOnViewProduct)
        category = findViewById(R.id.categoryOnViewProduct)
        pname = findViewById(R.id.productNameOnViewProduct)
        cost = findViewById(R.id.productCostOnViewProduct)
        description = findViewById(R.id.productDescriptionOnViewProduct)
        username = findViewById(R.id.usernameOnViewProduct)
        uname = findViewById(R.id.nameOnViewProduct)
        email = findViewById(R.id.emailOnViewProduct)
        productPhoto = findViewById(R.id.productPhotoOnViewProduct)
        mobileno = findViewById(R.id.mobilenoOnViewProduct)

        setNavigationStatusBarColor()
        setUpFab()
        getProductDetail()

        back.setOnClickListener({
            onBackPressed()
        })
    }

    private fun getDealId(): String {
        return intent.getStringExtra("dealid")
    }

    private fun setProductPhoto(photo: String) {
        val url = "http://${getString(R.string.ip)}/Snapbook/images/product/$photo"
        Glide.with(ctx!!)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        relProductPhotoProgressOnViewProduct.visibility = View.INVISIBLE
                        relProductPhotoOnViewProduct.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(productPhoto!!)
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.appBarColor)
        window.navigationBarColor = resources.getColor(R.color.colorGray)
    }

    private fun setUpFab() {
        scalingLayout!!.setListener(object : ScalingLayoutListener {
            override fun onCollapsed() {
                ViewCompat.animate(fabIcon).alpha(1f).setDuration(150).start()
                ViewCompat.animate(filterLayout).alpha(0f).setDuration(150).setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        fabIcon!!.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(view: View) {
                        filterLayout!!.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(view: View) {

                    }
                }).start()
            }

            override fun onExpanded() {
                ViewCompat.animate(fabIcon).alpha(0f).setDuration(200).start()
                ViewCompat.animate(filterLayout).alpha(1f).setDuration(200).setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        filterLayout!!.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(view: View) {
                        fabIcon!!.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(view: View) {

                    }
                }).start()
            }

            override fun onProgress(progress: Float) {
                if (progress > 0) {
                    fabIcon!!.visibility = View.INVISIBLE
                }

                if (progress < 1) {
                    filterLayout!!.visibility = View.INVISIBLE
                }
            }
        })

        scalingLayout!!.setOnClickListener {
            if (scalingLayout!!.state == State.COLLAPSED) {
                scalingLayout!!.expand()
            }
        }
    }

    private fun getProductDetail() {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/DealsController/getProductDetailByDealId?dealid=${getDealId()}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response: JSONArray? ->
            if (response!!.length() > 0) {
                val jsonObject = response.optJSONObject(0)
                condition?.text = jsonObject.optString("condition").toUpperCase()
                category?.text = jsonObject.optString("category")
                pname?.text = jsonObject.optString("name")
                cost?.text = jsonObject.optString("cost")
                status?.text = jsonObject.optString("status")
                description?.text = jsonObject.optString("description")
                uname?.text = jsonObject.optString("uname")
                username?.text = jsonObject.optString("username")
                email?.text = jsonObject.optString("email")
                mobileno?.text = jsonObject.optString("mobileno")

                if (jsonObject.optString("condition").toUpperCase() == "OLD") {
                    discount_text.text = ""
                }

                if (jsonObject.optString("condition").toUpperCase() == "NEW") {
                    discount_text.text = resources.getString(R.string.text_discount)
                }

                // Toast.makeText(ctx,""+jsonObject.optString("photo"),Toast.LENGTH_SHORT).show()
                if (jsonObject.optString("photo") != "") {
                    setProductPhoto(jsonObject.optString("photo"))
                }

                makeCall("" + jsonObject.optString("mobileno"))

                viewProductProgressRel.visibility = View.GONE
                viewProductSV.visibility = View.VISIBLE
                rel_fab.visibility = View.VISIBLE

                initPolling()
            }
        }, Response.ErrorListener { error -> })
        requestQueue?.add(jsonArrayRequest)
    }

    private fun getCurrentDate(): String {
        val timeStamp = System.currentTimeMillis() / 1000L
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(timeStamp * 1000L)
    }

    @SuppressLint("MissingPermission")
    private fun makeCall(mobileno: String) {
        rel_call.setOnClickListener({
            scalingLayout!!.collapse()

            val handler = Handler()
            handler.postDelayed({
                val call = Intent(Intent.ACTION_CALL, Uri.parse("tel:$mobileno"))
                call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(call)
            }, 400)
        })
    }

    private fun initPolling() {
        rel_polling.setOnClickListener({
            scalingLayout!!.collapse()

            val handler = Handler()
            handler.postDelayed({
                confirmPollingDialog()
            }, 400)
        })
    }

    private fun confirmPollingDialog() {
        val pollingDialog = PrettyDialog(this)
        pollingDialog.setTitle("Poll")
                .setMessage("Start Polling for this deal?")
                .addButton("Yes", R.color.black, R.color.light_gray) {
                    startPolling()
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

    private fun startPolling() {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/PollingController/createPollingDeal?" +
                "dealid=${getDealId()}&username=${preferences!!.getString("username", "")}&dealname=${pname!!.text.toString().replace(" ", "+")}" +
                "&dealdescription=${description!!.text.toString().replace(" ", "+")}&dealcategory=${category!!.text.toString().replace(" ", "+")}&dealcondition=${condition!!.text}" +
                "&dealcost=${cost!!.text}&dealstatus=${status!!.text.toString().replace(" ", "+")}&date=${getCurrentDate()}&type=deal"

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
                .setActivity(this@ViewProductActivity)
                .setText("Polling has been started!")
                .setDuration(Snacky.LENGTH_LONG)
                .success()
                .show()
    }

    private fun pollingFailureSnack() {
        Snacky.builder()
                .setActivity(this@ViewProductActivity)
                .setText("Error! try again")
                .setDuration(Snacky.LENGTH_LONG)
                .error()
                .show()
    }
}
