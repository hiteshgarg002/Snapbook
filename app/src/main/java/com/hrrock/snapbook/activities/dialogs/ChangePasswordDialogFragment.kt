package com.hrrock.snapbook.activities.dialogs

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.hrrock.snapbook.R
import com.hrrock.snapbook.networks.VolleyConnect
import kotlinx.android.synthetic.main.dialog_fragment_change_password.*

class ChangePasswordDialogFragment : DialogFragment() {
    private var ctx: Context? = null
    private var preferences: SharedPreferences? = null
    private var editor:SharedPreferences.Editor?=null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var update: TextView? = null
    private var close: ImageView? = null

    private companion object {
        private const val USER_PREFERENCES = "userinfo"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_fragment_change_password, container, false)
        ctx = activity

        preferences = ctx!!.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE)
        editor=preferences!!.edit()
        requestQueue = VolleyConnect.getInstance().requestQueue

        update = v.findViewById(R.id.updatePwd)
        close = v.findViewById(R.id.closePwdDialog)

        update!!.setOnClickListener({

            updatePwd.visibility = View.INVISIBLE
            pwdUpdateProgress.visibility = View.VISIBLE
            updatePassword()
        })

        close!!.setOnClickListener({
            dismiss()
        })

        return v
    }

    private fun updatePassword() {
        if (!currentPassword.text.isEmpty() && !newPassword.text.isEmpty() && !confirmPassword.text.isEmpty()) {

            if (newPassword.text.toString() == confirmPassword.text.toString()) {

                val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/CredentialController/updatePassword?" +
                        "username=${preferences!!.getString("username", "")}&cpwd=${currentPassword.text}&npwd=${newPassword.text}"

                stringRequest = StringRequest(url, Response.Listener<String> { response ->
                    when (response) {
                        "success" -> {
                            editor!!.putString("password",newPassword.text.toString())
                            editor!!.apply()
                            editor!!.commit()

                            dismiss()
                            Toast.makeText(ctx, "Password has been changed!", Toast.LENGTH_LONG).show()
                        }
                        "invalid" -> {
                            pwdUpdateProgress.visibility = View.INVISIBLE
                            updatePwd.visibility = View.VISIBLE
                            Toast.makeText(ctx, "Invalid Password!", Toast.LENGTH_LONG).show()
                        }
                        "failed" -> Toast.makeText(ctx, "Failed to update Password!", Toast.LENGTH_LONG).show()
                    }
                    pwdUpdateProgress.visibility = View.INVISIBLE
                    updatePwd.visibility = View.VISIBLE
                }, Response.ErrorListener { error -> })

                requestQueue!!.add(stringRequest)

            } else {
                pwdUpdateProgress.visibility = View.INVISIBLE
                updatePwd.visibility = View.VISIBLE
                Toast.makeText(ctx, "Password do not match!", Toast.LENGTH_LONG).show()
            }
        } else {
            pwdUpdateProgress.visibility = View.INVISIBLE
            updatePwd.visibility = View.VISIBLE
            Toast.makeText(ctx, "Field(s) can not be blank!", Toast.LENGTH_LONG).show()
        }
    }
}