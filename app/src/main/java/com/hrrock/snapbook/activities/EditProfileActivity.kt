package com.hrrock.snapbook.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hrrock.snapbook.R
import com.hrrock.snapbook.activities.dialogs.ChangePasswordDialogFragment
import com.hrrock.snapbook.networks.CallHttpRequest
import com.hrrock.snapbook.networks.VolleyConnect
import com.hrrock.snapbook.utils.GlideApp
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.thekhaeng.pushdownanim.PushDownAnim
import com.thekhaeng.pushdownanim.PushDownAnim.MODE_SCALE
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.snippet_top_edit_profile.*
import lib.kingja.switchbutton.SwitchMultiButton
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee
import java.io.*

class EditProfileActivity : AppCompatActivity(),OnMenuItemClickListener<PowerMenuItem> {
    private var ctx: Context? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var stringRequest: StringRequest? = null
    private var preferences: SharedPreferences? = null
    private var gender: SwitchMultiButton? = null
    private var powerMenu:PowerMenu?=null
    private var myIntent:Intent?=null
    private var destination: File? = null

    private companion object {
        private const val USER_PREFERENCE = "userinfo"
        private const val CAM_REQ_CODE = 1
        private const val GAL_REQ_CODE = 2
        private const val CAMERA = "camera"
        private const val GALLERY = "gallery"
        private const val EDIT_PROFILE_ACTIVITY="EditProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        ctx = this

        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
        requestQueue = VolleyConnect.getInstance().requestQueue

        gender = findViewById(R.id.genderOnEditProfile)

        setNavigationStatusBarColor()
        getInfo()
        onSaveClick()
        onDpFabClick()

        back.setOnClickListener({
            onBackPressed()
        })

        onChangePasswordClick()
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.appBarColor)

        window.navigationBarColor = resources.getColor(R.color.colorGray, null)
    }

    private fun onSaveClick(){
        PushDownAnim.setPushDownAnimTo(saveOnEditProfile)
                .setScale(MODE_SCALE, 0.89f )
                .setOnClickListener({
                    saveOnEditProfile.visibility=View.INVISIBLE
                    saveProgressOnEditProfile.visibility=View.VISIBLE
                    updateProfile()
                })
    }

    private fun getInfo() {
        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/CredentialController/getInfoByUsername?username=${preferences!!.getString("username", "")}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            if (response.length() == 1) {
                val jsonObject = response.optJSONObject(0)
                nameOnEditProfile.setText(jsonObject.optString("name"))
                mobileOnEditProfile.setText(jsonObject.optString("mobileno"))
                aboutOnEditProfile.setText(jsonObject.optString("about"))
                userNameOnEditProfile.text = jsonObject.optString("username")
                emailOnEditProfile.text = jsonObject.optString("email")

                when (jsonObject.optString("gender")) {
                    "Male" -> gender!!.selectedTab = 0
                    "Female" -> gender!!.selectedTab = 1
                }

                GlideApp.with(ctx!!.applicationContext)
                        .load("${ctx!!.getString(R.string.BASE_URL_PROFILE_PIC)}/${jsonObject.optString("profile")}")
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                dpUpdateProgressEditProfile!!.visibility=View.GONE
                                dpOnEditProfile!!.visibility=View.VISIBLE
                                fab_editDPOnEditProfile!!.visibility=View.VISIBLE
                                return false
                            }

                            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                                dpUpdateProgressEditProfile!!.visibility=View.GONE
                                dpOnEditProfile!!.visibility=View.VISIBLE
                                fab_editDPOnEditProfile!!.visibility=View.VISIBLE
                                return false
                            }
                        })
                        .placeholder(R.drawable.no_dp_big)
                        .into(dpOnEditProfile)

                relProgressOnEditText.visibility = View.GONE
                scrollEditProfile.visibility = View.VISIBLE
            }
        }, Response.ErrorListener { error -> })

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun updateProfile() {
        val url = "http://${ctx!!.getString(R.string.ip)}/Snapbook/index.php/CredentialController/updateProfile?" +
                "username=${preferences!!.getString("username", "")}&name=${nameOnEditProfile.text}&mobileno=${mobileOnEditProfile.text}&" +
                "about=${aboutOnEditProfile.text}&gender=${if (gender!!.selectedTab == 0) {
                    "Male"
                } else {
                    "Female"
                }}"

        stringRequest = StringRequest(url, Response.Listener<String> { response ->
            if(response=="success"){
                val intent=Intent(ctx,ProfileActivity::class.java)
                intent.putExtra("from","EditProfile")
                startActivity(intent)

                Bungee.inAndOut(ctx)
                ProfileActivity.destroyActivity()
                finish()
            }else{
                saveProgressOnEditProfile.visibility=View.INVISIBLE
                saveOnEditProfile.visibility=View.VISIBLE
            }
        }, Response.ErrorListener { error ->
            saveProgressOnEditProfile.visibility=View.INVISIBLE
            saveOnEditProfile.visibility=View.VISIBLE
        })

        requestQueue!!.add(stringRequest)
    }

    private fun onDpFabClick() {
        dpOnEditProfile!!.setOnClickListener({ view ->
            showPhotoSourceDialog(view)
        })

        fab_editDPOnEditProfile!!.setOnClickListener({ view ->
            showPhotoSourceDialog(view)
        })
    }

    private fun showPhotoSourceDialog(view: View) {
        /*val builder = CFAlertDialog.Builder(ctx)
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
        builder!!.show()*/

        powerMenu = PowerMenu.Builder(ctx)
                //     .setHeaderView(R.layout.layout_dialog_header) // header used for title
                //   .setFooterView(R.layout.layout_dialog_footer) // footer used for yes and no buttons
                .addItem(PowerMenuItem("Gallery", false)) // this is body
                .addItem(PowerMenuItem("Camera", false))
                // .setLifecycleOwner(this)
                .setAnimation(MenuAnimation.ELASTIC_CENTER)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setWidth(600)
                .setSelectedEffect(true)
                .setOnMenuItemClickListener(this)
                .build()

        powerMenu!!.showAsAnchorCenter(rootEditProfileView)
    }

    private fun startUploading() {
        updateDP()
    }

    private fun createApplicationFolder(data: Intent, from: String) {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()
        dpOnEditProfile!!.visibility = View.INVISIBLE
        fab_editDPOnEditProfile!!.visibility = View.INVISIBLE
        dpUpdateProgressEditProfile!!.visibility = View.VISIBLE
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
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 20, bytes)

        destination = File("sdcard/Pictures/Snapbook/", "${preferences!!.getString("username", "")}.jpg")
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

        // photoDestination = "sdcard/Pictures/Snapbook/${preferences!!.getString("username","")}.jpg"
        // loadSelectedPhoto(photoDestination!!)
        startUploading()
    }

    private fun saveImageCamera(data: Intent) {
        val thumbnail = data.extras!!.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 20, bytes)

        destination = File("sdcard/Pictures/Snapbook/", "${preferences!!.getString("username", "")}.jpg")
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

        //  photoDestination = "sdcard/Pictures/Snapbook/${preferences!!.getString("username","")}.jpg"
        //  loadSelectedPhoto(photoDestination!!)
        startUploading()
    }

    private fun updateDP() {
        val dpURL = arrayOf("http://${getString(R.string.ip)}/Snapbook/index.php/PhotoController/uploadDP")
        CallHttpRequest(applicationContext, destination!!, EDIT_PROFILE_ACTIVITY, dpUpdateProgressEditProfile, dpOnEditProfile, fab_editDPOnEditProfile).execute(dpURL[0])
    }

    override fun onItemClick(position: Int, item: PowerMenuItem?) {
        when (item!!.title) {
            "Gallery" -> {
                myIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(myIntent, GAL_REQ_CODE)
                powerMenu!!.dismiss()
            }
            "Camera" -> {
                myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(myIntent, CAM_REQ_CODE)
                powerMenu!!.dismiss()
            }
        }
    }

    private fun onChangePasswordClick(){
        PushDownAnim.setPushDownAnimTo(relChangePassword)
                .setScale(MODE_SCALE, 0.89f )
                .setOnClickListener({
                    val pwdDialog=ChangePasswordDialogFragment()
                    pwdDialog.show(supportFragmentManager,"")
                    pwdDialog.isCancelable=false
                })
    }
}
