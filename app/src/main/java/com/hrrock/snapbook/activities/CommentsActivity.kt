package com.hrrock.snapbook.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.adapters.CommentsAdapter
import com.hrrock.snapbook.models.CommentsModel
import com.hrrock.snapbook.networks.VolleyConnect
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.thekhaeng.pushdownanim.PushDownAnim
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.snippet_top_comments.*
import org.json.JSONArray
import java.util.*

class CommentsActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var commentsListView: ListView? = null
    private var list: MutableList<CommentsModel>? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var commentsAdapter: CommentsAdapter? = null
    private var writeComment: EditText? = null
    private var postComment: TextView? = null
    private var preferences: SharedPreferences? = null
    private var relProgress: RelativeLayout? = null
    private var relContent: RelativeLayout? = null
    private var relNoComment: RelativeLayout? = null
    private var userToken: String? = null
    private var userName: String? = null
    private var type: String? = null

    private companion object {
        private const val USER_PREFERENCES = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        ctx = this

        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)
        commentsListView = findViewById(R.id.commentsListView)
        writeComment = findViewById(R.id.writeComment)
        postComment = findViewById(R.id.postComment)
        relContent = findViewById(R.id.rel_comment_content)
        relProgress = findViewById(R.id.rel_comment_progress)
        relNoComment = findViewById(R.id.rel_no_comment)
        list = ArrayList()
        getComments()

        commentsAdapter = CommentsAdapter(ctx!!, R.layout.layout_comments_row, list!!)
        commentsListView!!.adapter = commentsAdapter
        commentsAdapter!!.notifyDataSetChanged()

        onCommentTextChange()

        Picasso.with(ctx).load(ctx!!.getString(R.string.BASE_URL_PROFILE_PIC) + preferences!!.getString("username", "") + ".jpg")
                .placeholder(R.drawable.no_dp_big)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(userDpOnComments)

        setNavigationStatusBarColor()

        back.setOnClickListener({
            onBackPressed()
        })

        //  commentsListView!!.smoothScrollToPosition(commentsAdapter!!.count-1)
    }

    private fun getComments() {
        list!!.clear()
        val url = "http://" + ctx!!.getString(R.string.ip) + "/Snapbook/index.php/PollingController/getCommentsByPostId?postid=" + getPostId()

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            if (response.length() > 0) {
                (0 until response.length()).forEach { i ->
                    val jsonObject = response.optJSONObject(i)

                    val commentsModel = CommentsModel(jsonObject.optString("commentid"),
                            jsonObject.optString("postid"),
                            jsonObject.optString("username"),
                            jsonObject.optString("comment"))

                    userToken = jsonObject.optString("usertoken")
                    userName = jsonObject.optString("owneruname")
                    type = jsonObject.optString("posttype")

                    list!!.add(commentsModel)
                }
                commentsAdapter!!.notifyDataSetChanged()
                relProgress!!.visibility = View.INVISIBLE
                relNoComment!!.visibility = View.INVISIBLE
                relContent!!.visibility = View.VISIBLE
                commentsListView!!.smoothScrollToPosition(commentsAdapter!!.count - 1)

                commentsPostProgress.visibility = View.INVISIBLE
                postComment!!.visibility = View.VISIBLE
            } else {
                commentTagLine.text = getString(R.string.post_a_comment)
                relProgress!!.visibility = View.INVISIBLE
                relNoComment!!.visibility = View.VISIBLE

                commentsPostProgress.visibility = View.INVISIBLE
                postComment!!.visibility = View.VISIBLE
            }
        }, Response.ErrorListener { error ->
            commentTagLine.text = getString(R.string.network_error)
            relProgress!!.visibility = View.INVISIBLE
            relNoComment!!.visibility = View.VISIBLE
        })

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun getPostId(): String? {
        return intent.getStringExtra("postid")
    }

    private fun postComment() {
        val url = "http:" + ctx!!.getString(R.string.ip) + "/Snapbook/index.php/PollingController/postCommentByPostId?" +
                "postid=" + getPostId() + "&username=" + preferences!!.getString("username", "") + "&comment=" + writeComment!!.text.toString()

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            if (response == "success") {

                if (preferences!!.getString("username", "") != userName) {
                    //Toast.makeText(ctx,userToken+" - "+writeComment!!.text,Toast.LENGTH_LONG).show()
                    sendCommentNotification(userToken!!, writeComment!!.text.toString())
                    makeNotification(userName!!, getPostId()!!, writeComment!!.text.toString(), type!!)
                }
                getComments()

            } else {
                commentsPostProgress.visibility = View.INVISIBLE
                postComment!!.visibility = View.VISIBLE
            }
        }, Response.ErrorListener { error -> })
        requestQueue!!.add(stringRequest)
    }

    private fun onCommentTextChange() {
        writeComment!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty()) {
                    postComment!!.isClickable = true
                    postComment!!.setTextColor(ctx!!.resources.getColor(R.color.link_blue, null))
                    PushDownAnim.setPushDownAnimTo(postComment)
                            .setScale(PushDownAnim.MODE_SCALE, 0.89f)
                            .setOnClickListener { view ->
                                postComment()
                                postComment!!.visibility = View.INVISIBLE
                                commentsPostProgress.visibility = View.VISIBLE
                            }
                } else {
                    postComment!!.isClickable = false
                    postComment!!.setTextColor(ctx!!.resources.getColor(R.color.sky_blue, null))
                }
            }
        })
    }

    private fun sendCommentNotification(token: String, comment: String) {
        // Toast.makeText(ctx, "$comment - $token",Toast.LENGTH_LONG).show()
        val url = "http://${ctx!!.getString(R.string.ip)}/Firebase/notification.php?regId=$token&title=Snapbook&message=${preferences!!.getString("username", "")} commented on your post! : $comment&push_type=individual"

        stringRequest = StringRequest(url, { response -> }) { error -> }
        requestQueue!!.add(stringRequest)
    }

    private fun makeNotification(username: String, postId: String, comment: String, type: String) {
        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/NotificationController/makeYouNotification?uname=${preferences!!.getString("username", "")}&username=$username&notification=${preferences!!.getString("username", "")} commented on your post! : $comment&postid=$postId&type=$type"

        stringRequest = StringRequest(url, { response ->
            writeComment!!.text.clear()
        }) { error -> }
        requestQueue!!.add(stringRequest)
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.appBarColor)
        window.navigationBarColor = resources.getColor(R.color.white, null)
    }
}
