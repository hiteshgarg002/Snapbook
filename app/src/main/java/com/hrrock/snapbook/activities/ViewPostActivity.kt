package com.hrrock.snapbook.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.hrrock.snapbook.R
import com.hrrock.snapbook.networks.VolleyConnect
import kotlinx.android.synthetic.main.activity_view_post.*
import kotlinx.android.synthetic.main.snippet_top_view_post.*
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee

class ViewPostActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var preferences: SharedPreferences? = null
    private var stringRequest: StringRequest? = null
    private var pollUp: ImageView? = null
    private var pollDown: ImageView? = null
    private var userToken: String? = null

    private companion object {
        private const val USER_PREFERENCES = "userinfo"
        private const val POLL_UP = "up"
        private const val POLL_DOWN = "down"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_post)
        ctx = this

        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)
        pollUp = findViewById(R.id.thumb_upOnViewPost)
        pollDown = findViewById(R.id.thumb_downOnViewPost)

        back.setOnClickListener({
            onBackPressed()
        })

        comments_OnViewPost.setOnClickListener({
            showComments(getPostId())
        })

        setNavigationStatusBarColor()
        getPost()
        onVoteSelected()
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.black)

        window.navigationBarColor = resources.getColor(R.color.white, null)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        Bungee.slideRight(ctx)
    }

    private fun getPostId(): String {
        return intent.getStringExtra("postid")
    }

    private fun onVoteSelected() {
        pollUp!!.setOnClickListener({
            voteProgress_onViewPost.visibility = View.VISIBLE
            setPollOnAction(POLL_UP, pollUp!!, pollDown!!)
        })

        pollDown!!.setOnClickListener({
            voteProgress_onViewPost.visibility = View.VISIBLE
            setPollOnAction(POLL_DOWN, pollDown!!, pollUp!!)
        })
    }

    @SuppressLint("SetTextI18n")
    private fun getPost() {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/PostsController/getPostById?postid=${getPostId()}&username=${preferences!!.getString("username", "")}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            val jsonObject = response.optJSONObject(0)

            Glide.with(applicationContext)
                    .load(ctx!!.getString(R.string.BASE_URL_PHOTOS_PROFILE) + jsonObject.optString("photo"))
                    .into(postOnViewPost)

            Glide.with(applicationContext)
                    .load(ctx!!.getString(R.string.BASE_URL_PROFILE_PIC) + "${jsonObject.optString("username")}.jpg")
                    .into(dpOnViewPost)

            userNameOnViewPost.text = jsonObject.optString("username")
            dateOnViewPost.text = jsonObject.optString("date")
            userToken = jsonObject.optString("firebasetoken")

            if (jsonObject.optString("caption") != "") {
                captionOnViewPost.text = jsonObject.optString("caption")
                linearCaptionOnViewPost.visibility = View.VISIBLE
            } else {
                linearCaptionOnViewPost.visibility = View.GONE
            }

            if (jsonObject.optString("voteup") != "0" || jsonObject.optString("votedown") != "0"
                    || jsonObject.optString("voteup") != "0" && jsonObject.optString("votedown") != "0") {
                upVotesOnViewPost.text = "${jsonObject.optString("voteup")} ${ctx!!.getString(R.string.vote_up)}"
                downVotesOnViewPost.text = "${jsonObject.optString("votedown")} ${ctx!!.getString(R.string.vote_down)}"
                linearVotes.visibility = View.VISIBLE
            } else {
                linearVotes.visibility = View.GONE
            }

            when {
                jsonObject.optString("pollstatus") == "up" -> {
                    thumb_downOnViewPost.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null))
                    thumb_upOnViewPost.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.link_blue, null))
                }
                jsonObject.optString("pollstatus") == "down" -> {
                    thumb_upOnViewPost.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null))
                    thumb_downOnViewPost.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.link_blue, null))
                }
                else -> {
                    thumb_upOnViewPost.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null))
                    thumb_downOnViewPost.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null))
                }
            }

            rel_progress_view_post.visibility = View.GONE
            scroll_view_post.visibility = View.VISIBLE
        }, Response.ErrorListener { error -> })

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun setPollOnAction(polltype: String, poll: ImageView, otherPoll: ImageView) {
        pollUp!!.isClickable = false
        pollDown!!.isClickable = false

        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/PollingController/setPoll?postid=${getPostId()}&username=${preferences!!.getString("username", "")}&polltype=$polltype"

        stringRequest = StringRequest(url, { response ->
            updateVoteCount()

            if (response == "up" || response == "down") {
                poll.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.link_blue, null))
                otherPoll.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null))

                if (userNameOnViewPost.text != preferences!!.getString("username", "")) {
                    sendVoteNotification(userToken!!)
                    makeNotification(userNameOnViewPost.text.toString(), getPostId())
                }
            } else {
                poll.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null))
                otherPoll.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null))

                if (userNameOnViewPost.text != preferences!!.getString("username", "")) {
                    removeNotification(userNameOnViewPost.text.toString(), getPostId())
                }
            }
        }) { error -> poll.imageTintList = ColorStateList.valueOf(ctx!!.resources.getColor(R.color.holo_light, null)) }

        requestQueue!!.add(stringRequest)
    }

    @SuppressLint("SetTextI18n")
    private fun updateVoteCount() {
        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/PollingController/updateVoteCount?postid=${getPostId()}"
        //Toast.makeText(ctx,newsFeedModel.getPostId()+"",Toast.LENGTH_SHORT).show();

        jsonArrayRequest = JsonArrayRequest(url, { response ->
            val jsonObject = response.optJSONObject(0)

            if (jsonObject.optString("voteup") != "0" || jsonObject.optString("votedown") != "0"
                    || jsonObject.optString("voteup") != "0" && jsonObject.optString("votedown") != "0") {

                upVotesOnViewPost.text = "${jsonObject.optString("voteup")} ${ctx!!.getString(R.string.vote_up)}"
                downVotesOnViewPost.text = "${jsonObject.optString("votedown")} ${ctx!!.getString(R.string.vote_down)}"

                linearVotes.visibility = View.VISIBLE
            } else {
                linearVotes.visibility = View.GONE
            }

            voteProgress_onViewPost.visibility = View.INVISIBLE
            pollUp!!.isClickable = true
            pollDown!!.isClickable = true
            // Toast.makeText(ctx,jsonObject.optString("voteup")+"-"+jsonObject.optString("votedown"),Toast.LENGTH_SHORT).show();
        }) { error -> }

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun showComments(postid: String) {
        val commentIntent = Intent(ctx, CommentsActivity::class.java)
        commentIntent.putExtra("postid", postid)
        startActivity(commentIntent)

        Bungee.slideLeft(ctx)
    }

    private fun sendVoteNotification(token: String) {
        val url = "http://${ctx!!.getString(R.string.ip)}/Firebase/notification.php?regId=$token&title=Snapbook&message=${preferences!!.getString("username", "")} voted on your post!&push_type=individual"

        stringRequest = StringRequest(url, { response -> }) { error -> }
        requestQueue!!.add(stringRequest)
    }

    private fun makeNotification(username: String, postId: String) {
        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/NotificationController/makeYouNotification?uname=${preferences!!.getString("username", "")}&username=$username&notification=${preferences!!.getString("username", "")} voted on your post!&postid=$postId&type=vote"

        stringRequest = StringRequest(url, { response -> }) { error -> }
        requestQueue!!.add(stringRequest)
    }

    private fun removeNotification(username: String, postId: String) {
        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/NotificationController/removeYouNotification?uname=${preferences!!.getString("username", "")}&username=$username&notification=${preferences!!.getString("username", "")} voted on your post!&postid=$postId&type=vote"

        stringRequest = StringRequest(url, { response -> }) { error -> }
        requestQueue!!.add(stringRequest)
    }
}