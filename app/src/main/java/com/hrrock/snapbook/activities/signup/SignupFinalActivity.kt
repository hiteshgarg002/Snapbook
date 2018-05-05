package com.hrrock.snapbook.activities.signup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import com.crowdfire.cfalertdialog.CFAlertDialog
import com.dx.dxloadingbutton.lib.LoadingButton
import com.hrrock.snapbook.R
import com.hrrock.snapbook.activities.LoginActivity
import com.hrrock.snapbook.interfaces.RetrofitApiInterface
import com.hrrock.snapbook.networks.CallHttpRequest
import com.hrrock.snapbook.networks.RetrofitApiClient
import kotlinx.android.synthetic.main.activity_signup_final.*
import lib.kingja.switchbutton.SwitchMultiButton
import retrofit2.Call
import retrofit2.Callback
import spencerstudios.com.bungeelib.Bungee
import java.io.*

class SignupFinalActivity : AppCompatActivity() {
    private var userName: List<String>? = null
    private var email: String? = null
    private var password: String? = null
    private var ctx: Context? = null
    private var activity: Activity? = null
    private var signup: LoadingButton? = null
    private var myIntent: Intent? = null
    private var destination: File? = null
    private var genderSwitch: SwitchMultiButton? = null
    private var gender: String? = null
    private var apiInterface: RetrofitApiInterface? = null
    private var stringCall: Call<String>? = null

    private companion object {
        private const val SIGNUP_ACTIVITY = "SignupActivity"
        private const val CAM_REQ_CODE = 1
        private const val GAL_REQ_CODE = 2
        private const val CAMERA = "camera"
        private const val GALLERY = "gallery"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_final)
        ctx = this
        activity = this
        apiInterface = RetrofitApiClient.getRetrofitForCredentialController().create(RetrofitApiInterface::class.java)

        userName = ArrayList()
        signup = findViewById(R.id.signupButton)
        genderSwitch = findViewById(R.id.radioGender)

        val bundle = intent.extras
        email = bundle.getString("email")
        password = bundle.getString("password")
        userName = email!!.split("@").toList()

        Toast.makeText(ctx, userName!![0], Toast.LENGTH_LONG).show()

        unameSignup.text = userName!![0]

        setDP()
        onSignupClick()
        onSignupAnimationEnd()
    }

    private fun onSignupClick() {
        signup!!.setOnClickListener({ view ->
            if (isConnected()) {
                if (!(isCredentialNull(nameSignup.text.toString())
                                && isCredentialNull(mobilenoSignup.text.toString()))) {
                    signup!!.startLoading()
                    doSignup()
                } else {
                    Snackbar.make(view, "Credential(s) can not be blank!", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun uploadDP() {
        val dpURL = arrayOf("http://" + getString(R.string.ip) + "/Snapbook/index.php/PhotoController/uploadDP")
        CallHttpRequest(ctx, destination!!, SIGNUP_ACTIVITY).execute(dpURL[0])

        signup!!.loadingSuccessful()
    }

    private fun doSignup() {
        uploadDP()
        when (genderSwitch!!.selectedTab) {
            0 -> gender = "Male"
            1 -> gender = "Female"
        }

        stringCall = apiInterface!!.signupDetails(email, password, nameSignup.text.toString()
                , mobilenoSignup.text.toString(), unameSignup.text.toString(), unameSignup.text.toString() + ".jpg", gender)

        stringCall!!.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                if (response.body() == "success") {

                } else if (response.body() == "failed") {
                    signup!!.loadingFailed()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(ctx, call.toString() + "", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setDP() {
        dpSignup.setOnClickListener {
            showPhotoSourceDialog()
        }

        editPhotoSignup.setOnClickListener {
            showPhotoSourceDialog()
        }
    }

    private fun showPhotoSourceDialog() {
        val builder = CFAlertDialog.Builder(ctx)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                .setTitle("Select a Photo")
                .addButton("Gallery", -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED) { dialog, which ->
                    myIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(myIntent, GAL_REQ_CODE)
                    dialog.dismiss()
                }.addButton("Camera", -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED) { dialog, which ->
                    myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(myIntent, CAM_REQ_CODE)
                    dialog.dismiss()
                }
        builder!!.show()
    }

    private fun loadSelectedPhoto(destination: String) {
        Glide.with(ctx!!)
                .load(destination)
                .into(dpSignup)
    }

    private fun createApplicationFolder(data: Intent, from: String) {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()

        when (from) {
            CAMERA -> saveImageCamera(data)
            GALLERY -> saveImageGallery(data.data)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
                && resultCode != Activity.RESULT_CANCELED
                && requestCode == CAM_REQ_CODE
                && data != null) {

            createApplicationFolder(data, CAMERA)
        }
        if (resultCode == Activity.RESULT_OK
                && resultCode != Activity.RESULT_CANCELED
                && requestCode == GAL_REQ_CODE
                && data != null) {
            try {
                createApplicationFolder(data, GALLERY)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun saveImageGallery(data: Uri?) {
        val thumbnail = MediaStore.Images.Media.getBitmap(this.contentResolver, data)
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        destination = File("sdcard/Pictures/Snapbook/", unameSignup.text.toString() + ".jpg")
        val fo: FileOutputStream
        try {
            destination!!.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val photoDestination = "sdcard/Pictures/Snapbook/" + unameSignup.text.toString() + ".jpg"
        loadSelectedPhoto(photoDestination)
    }

    private fun saveImageCamera(data: Intent) {
        val thumbnail = data.extras!!.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        destination = File("sdcard/Pictures/Snapbook/", unameSignup.text.toString() + ".jpg")
        val fo: FileOutputStream
        try {
            destination!!.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val photoDestination = "sdcard/Pictures/Snapbook/" + unameSignup.text.toString() + ".jpg"
        loadSelectedPhoto(photoDestination)
    }

    private fun isCredentialNull(credential: String?): Boolean {
        return credential == ""
    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    private fun onSignupAnimationEnd() {
        signup!!.setAnimationEndListener({ animationType ->
            when (animationType) {
                LoadingButton.AnimationType.SUCCESSFUL -> toLoginScreen()
                LoadingButton.AnimationType.FAILED -> Snackbar.make(findViewById(R.id.relFinalSignup), "Something went wrong!", Snackbar.LENGTH_LONG).show()
                else -> {
                }
            }
        })
    }

    private fun toLoginScreen() {
        LoginActivity.destroy()

        startActivity(Intent(ctx, LoginActivity::class.java))
        Bungee.slideLeft(ctx)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(ctx, SignupActivity::class.java))
        Bungee.slideRight(ctx)
        finish()
    }
}
