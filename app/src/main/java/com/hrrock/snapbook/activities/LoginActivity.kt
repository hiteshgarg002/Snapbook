package com.hrrock.snapbook.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.dx.dxloadingbutton.lib.LoadingButton
import com.hrrock.snapbook.R
import com.hrrock.snapbook.activities.signup.SignupActivity
import com.hrrock.snapbook.interfaces.RetrofitApiInterface
import com.hrrock.snapbook.models.UserDetailsModel
import com.hrrock.snapbook.networks.RetrofitApiClient
import com.libizo.CustomEditText
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import spencerstudios.com.bungeelib.Bungee


class LoginActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var activity: Activity? = null
    private var loginButton: LoadingButton? = null
    private var uName: CustomEditText? = null
    private var uPwd: CustomEditText? = null
    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var details: List<UserDetailsModel>? = null
    private var apiInterface: RetrofitApiInterface? = null
    private var jsonCall: Call<List<UserDetailsModel>>? = null

    companion object {
        private const val REQ_CODE = 1
        private const val ACTIVITY = "LoginActivity"
        private const val USER_PREFERENCE = "userinfo"

        @SuppressLint("StaticFieldLeak")
        private var activityRef: Activity? = null

        fun destroy() {
            activityRef!!.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ctx = this
        activity = this
        activityRef = this

        uName = findViewById(R.id.userNameLogin)
        uPwd = findViewById(R.id.passwordLogin)
        loginButton = findViewById(R.id.loginButton)
        details = ArrayList()
        apiInterface = RetrofitApiClient.getRetrofitForCredentialController().create(RetrofitApiInterface::class.java)

        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        loginButton!!.setOnClickListener {
            if (isConnected()) {
                loginButton!!.startLoading()
                doLogin()
            } else {
                Snackbar.make(findViewById(R.id.relLoginActivity), "Check your network settings!", Snackbar.LENGTH_LONG).show()
            }
        }

        signUpfromLogin.setOnClickListener {
            startActivity(Intent(ctx, SignupActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            Bungee.slideRight(ctx)
            // finish()
        }

        requestPermission()
        onLoginAnimEnd()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this@LoginActivity, arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE), REQ_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQ_CODE ->
                if (grantResults.isNotEmpty()) {
                    val CAMERA = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val INTERNET = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    val READ_EXTERNAL = grantResults[2] == PackageManager.PERMISSION_GRANTED
                    val WRITE_EXTERNAL = grantResults[3] == PackageManager.PERMISSION_GRANTED
                    val CALL_PHONE = grantResults[4] == PackageManager.PERMISSION_GRANTED

                    if (CAMERA && INTERNET && READ_EXTERNAL && WRITE_EXTERNAL && CALL_PHONE) {

                        Snackbar.make(findViewById(R.id.relLoginActivity), "Permissions Granted!", Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(findViewById(R.id.relLoginActivity), "Permissions Denied!", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun doLogin() {
        jsonCall = apiInterface!!.doLogin("" + uName!!.text, "" + uPwd!!.text)

        jsonCall!!.enqueue(object : Callback<List<UserDetailsModel>> {
            override fun onResponse(call: Call<List<UserDetailsModel>>, response: retrofit2.Response<List<UserDetailsModel>>) {
                details = response.body()

                if (details!!.isNotEmpty()) {
                    val uDetails = details!![0]
                    editor!!.putString("email", uDetails.email)
                    editor!!.putString("password", uDetails.password)
                    editor!!.putString("name", uDetails.name)
                    editor!!.putString("mobileno", uDetails.mobileno)
                    editor!!.putString("username", uDetails.username)
                    editor!!.putString("profile", uDetails.profile)
                    editor!!.putString("gender", uDetails.gender)
                    editor!!.putString("about", uDetails.about)
                    editor!!.putBoolean("login", true)
                    editor!!.apply()

                    loginButton!!.loadingSuccessful()
                } else {
                    loginButton!!.loadingFailed()
                }
            }

            override fun onFailure(call: Call<List<UserDetailsModel>>, t: Throwable) {
                loginButton!!.loadingFailed()
                // Toast.makeText(ctx, "" + call, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    private fun onLoginAnimEnd() {
        loginButton!!.setAnimationEndListener({ animationType ->
            if (animationType == LoadingButton.AnimationType.SUCCESSFUL) {
                startActivity(Intent(ctx, HomeActivity::class.java))
                Bungee.inAndOut(ctx)
                finish()
            } else if (animationType == LoadingButton.AnimationType.FAILED) {
                Snackbar.make(findViewById(R.id.relLoginActivity), "Invalid Credential(s)!", Snackbar.LENGTH_LONG).show()
            }
        })
    }
}
