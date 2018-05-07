package com.hrrock.snapbook.activities.signup

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.hrrock.snapbook.R
import kotlinx.android.synthetic.main.activity_signup.*
import spencerstudios.com.bungeelib.Bungee

class SignupActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var myIntent: Intent? = null
    private var myBundle: Bundle? = null
    private var next: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        ctx = this

        next = findViewById(R.id.nextSignupBtn)

        next!!.setOnClickListener { view ->
            if (isConnected()) {
                if (!(isCredentialNull(emailSignup.text.toString())
                                && isCredentialNull(passwordSignup.text.toString())
                                && isCredentialNull(cPasswordSignup.text.toString()))) {
                    if (passwordSignup.text.toString() == cPasswordSignup.text.toString()) {
                        jumpToFinalSignup()
                    } else {
                        Snackbar.make(view, "Passwords do not match!", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    Snackbar.make(view, "Credential(s) can not be blank!", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun isCredentialNull(credential: String?): Boolean {
        return credential == ""
    }

    private fun jumpToFinalSignup() {
        myBundle = Bundle()
        myBundle!!.putString("email", emailSignup.text.toString())
        myBundle!!.putString("password", passwordSignup.text.toString())

        myIntent = Intent(ctx, SignupFinalActivity::class.java)
        myIntent!!.putExtras(myBundle)
        myIntent!!.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(myIntent)

        Bungee.slideLeft(ctx)

        finish()
    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Bungee.slideLeft(ctx)
    }
}
