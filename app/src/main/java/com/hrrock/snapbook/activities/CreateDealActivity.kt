package com.hrrock.snapbook.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.networks.VolleyConnect
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.activity_create_deal.*
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee

class CreateDealActivity : AppCompatActivity() {
    private var ctx:Context?=null
    private var category:MaterialSpinner?=null
    private var requestQueue:RequestQueue?=null
    private var stringRequest: StringRequest?=null
    private var categoryList:MutableList<String>?=null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var activity:Activity?=null
        fun destroyActivity(){
            activity?.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_deal)
        ctx=this
        activity=this

        requestQueue=VolleyConnect.getInstance().requestQueue
        categoryList=ArrayList()

        category=findViewById(R.id.categorySpinnerOnCreateDeal)
        getCategories()

        setNavigationStatusBarColor()
        onPressingNext()
    }

    private fun getCategories(){
        categoryList!!.clear()
        val url="http://${getString(R.string.ip)}/Snapbook/index.php/DealsController/getAllCategories"

        stringRequest= StringRequest(url, Response.Listener<String> { response ->
            when(response.toString()){
                "invalid"->{
                    categoryList!!.add("-Select-")
                    category?.setItems(categoryList as ArrayList<String>)
                }
                else->{
                    categoryList!!.add("-Select-")
                    val jsonArray=JSONArray(response)
                    for(i in 0 until jsonArray.length()){
                        val jsonObject=jsonArray.optJSONObject(i)

                        categoryList!!.add(jsonObject.optString("name"))
                    }
                    category?.setItems(categoryList as ArrayList<String>)
                }
            }
        }, Response.ErrorListener { error ->  })

        requestQueue!!.add(stringRequest)
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.light_gray)
        window.navigationBarColor = resources.getColor(R.color.white,null)
    }

    private fun onPressingNext(){
        createDealNext.setOnClickListener({
           // Toast.makeText(ctx,""+category?.selectedIndex,Toast.LENGTH_SHORT).show()
           // Toast.makeText(ctx,""+categoryList?.get(category?.selectedIndex!!),Toast.LENGTH_SHORT).show()
            val intent=Intent(ctx,CreateDealFinalActivity::class.java)

            intent.putExtra("productname",productNameOnCreateDeal.text.toString())
            intent.putExtra("productdesc",descriptionOnCreateDeal.text.toString())
            intent.putExtra("productcat", categoryList?.get(category?.selectedIndex!!))

            startActivity(intent)
            Bungee.slideLeft(ctx)
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(ctx,DealsActivity::class.java))
        Bungee.slideRight(ctx)
        finish()
    }
}
