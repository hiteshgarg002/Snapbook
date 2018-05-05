package com.hrrock.snapbook.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gigamole.navigationtabstrip.NavigationTabStrip
import com.hrrock.snapbook.R
import com.hrrock.snapbook.fragments.ProfileGridViewFragment
import com.hrrock.snapbook.fragments.ProfileListViewFragment
import com.hrrock.snapbook.networks.CallHttpRequest
import com.hrrock.snapbook.networks.VolleyConnect
import com.hrrock.snapbook.utils.BottomNavigationViewHelper
import com.hrrock.snapbook.utils.GlideApp
import com.hrrock.snapbook.utils.ScrollableViewPager
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import com.joaquimley.faboptions.FabOptions
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.wang.avi.AVLoadingIndicatorView
import de.hdodenhof.circleimageview.CircleImageView
import iammert.com.view.scalinglib.ScalingLayout
import iammert.com.view.scalinglib.ScalingLayoutListener
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONArray
import spencerstudios.com.bungeelib.Bungee
import java.io.*

class ProfileActivity : AppCompatActivity(), OnMenuItemClickListener<PowerMenuItem> {

    private var ctx: Context? = null
    private var activity: Activity? = null
    private var bottomNavigationViewEx: BottomNavigationViewEx? = null
    private var menuItem: MenuItem? = null
    private var menu: Menu? = null
    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var requestQueue: RequestQueue? = null
    private var jsonArrayRequest: JsonArrayRequest? = null
    private var scalingLayout: ScalingLayout? = null
    private var toolbarText: TextView? = null
    private var fab: FabOptions? = null
    private var myIntent: Intent? = null
    private var destination: File? = null
    private var dpFab: FloatingActionButton? = null
    private var dp: CircleImageView? = null
    private var dpUpdateProgress: AVLoadingIndicatorView? = null
    private var options: ImageView? = null
    private var powerMenu: PowerMenu? = null

    companion object {
        private const val ACTIVITY_NUM = 4
        private const val USER_PREFERENCE = "userinfo"
        private const val CAM_REQ_CODE = 1
        private const val GAL_REQ_CODE = 2
        private const val CAMERA = "camera"
        private const val GALLERY = "gallery"
        private const val PROFILE_ACTIVITY = "ProfileActivity"
        @SuppressLint("StaticFieldLeak")
        private var profileActivity: Activity? = null

        fun destroyActivity() {
            if (profileActivity != null) {
                profileActivity!!.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_profile_old)
        setContentView(R.layout.activity_profile)
        ctx = this
        activity = this
        profileActivity = this

        requestQueue = VolleyConnect.getInstance().requestQueue
        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
        scalingLayout = findViewById(R.id.scalingLayoutProfile)
        toolbarText = findViewById(R.id.profile_toolbar_text)
        fab = findViewById(R.id.profile_fab)
        dpFab = findViewById(R.id.fab_editDPOnProfile)
        dp = findViewById(R.id.dpOnProfile)
        dpUpdateProgress = findViewById(R.id.dpUpdateProgressProfile)
        options = findViewById(R.id.options)

        editor = preferences!!.edit()

        setUpTabLayout()
        setUpBottomNavigationView()
        loadProfilePic()
        setNavigationStatusBarColor()
        setOnScrollListener()
        onFabClick()
        viewFollowersFollowingList()
        getProfile()
        onDpFabClick()
        onOptionsClick()

        if (this.intent.getStringExtra("from")=="EditProfile") {

        }
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

    private fun loadProfilePic() {
        val dpURL = "http://${ctx!!.getString(R.string.ip)}/Snapbook/images/profile/${preferences!!.getString("profile", "")}"
        GlideApp.with(ctx!!.applicationContext)
                .load(dpURL)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        dpUpdateProgress!!.visibility=View.GONE
                        dp!!.visibility=View.VISIBLE
                        dpFab!!.visibility=View.VISIBLE
                        return false
                    }
                })
                .placeholder(R.drawable.no_dp_big)
                .into(dp!!)
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.colorGray)

        window.navigationBarColor = resources.getColor(R.color.colorGray, null)
    }

    private fun setUpTabLayout() {
        val viewPager = findViewById<ScrollableViewPager>(R.id.profileViewPager)
        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment? {
                var F: Fragment? = null
                if (position == 0) {
                    F = ProfileGridViewFragment()
                }
                if (position == 1) {
                    F = ProfileListViewFragment()
                }
                return F
            }

            override fun getCount(): Int {
                return 2
            }
        }

        val navigationTabStrip = findViewById<View>(R.id.profileTabs) as NavigationTabStrip
        navigationTabStrip.setTitles("Snap Grid", "Snap List")
        navigationTabStrip.titleSize = 30f
        navigationTabStrip.stripColor = Color.GREEN
        navigationTabStrip.setStripWeight(6f)
        navigationTabStrip.stripFactor = 2f
        navigationTabStrip.stripType = NavigationTabStrip.StripType.POINT
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        navigationTabStrip.cornersRadius = 3f
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY
        navigationTabStrip.activeColor = Color.WHITE

        navigationTabStrip.setViewPager(viewPager, 0)
        navigationTabStrip.setTabIndex(0, true)

        viewPager.setCanScroll(false)
    }

    private fun setOnScrollListener() {
        scalingLayout!!.setListener(object : ScalingLayoutListener {
            override fun onProgress(p0: Float) {
            }

            override fun onExpanded() {
                toolbarText?.visibility = View.VISIBLE
                fab?.visibility = View.VISIBLE
            }

            override fun onCollapsed() {
                toolbarText?.visibility = View.INVISIBLE
                fab?.visibility = View.INVISIBLE
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(ctx, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        //overridePendingTransition(0, 0)
        Bungee.fade(ctx)
        activity!!.finish()
    }

    private fun onFabClick() {
        fab?.setOnClickListener({ view ->
            when (view.id) {
                R.id.fab_edit -> {
                    startActivity(Intent(ctx, EditProfileActivity::class.java))
                    Bungee.inAndOut(ctx)
                }
                R.id.fab_saved -> {
                    Toast.makeText(ctx, "saved", Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        })
    }

    private fun viewFollowersFollowingList() {
        linearFollowersOnProfile.setOnClickListener({
            startActivity(Intent(ctx, FollowersActivity::class.java))
            Bungee.inAndOut(ctx)
        })

        linearFollowingOnProfile.setOnClickListener({
            startActivity(Intent(ctx, FollowingActivity::class.java))
            Bungee.inAndOut(ctx)
        })
    }

    private fun getProfile() {
        val url = "http://${getString(R.string.ip)}/Snapbook/index.php/CredentialController/getProfile?username=${preferences!!.getString("username", "")}"

        jsonArrayRequest = JsonArrayRequest(url, Response.Listener<JSONArray> { response ->
            if (response.length() > 0) {
                val jsonObject = response.optJSONObject(0)
                nameOnProfile.text = jsonObject.optString("name")
                followersOnProfile.text = jsonObject.optString("followerscount")
                followingOnProfile.text = jsonObject.optString("followingcount")
                postsOnProfile.text = jsonObject.optString("postscount")

                if(!jsonObject.optString("about").isEmpty()) {
                    aboutOnProfile.text = jsonObject.optString("about")
                    aboutOnProfile.visibility=View.VISIBLE
                }else{
                    aboutOnProfile.visibility=View.GONE
                }

                profileProgress.visibility = View.GONE
                rel_no_network.visibility = View.GONE
                profileContent.visibility = View.VISIBLE
            }
        }, Response.ErrorListener { error ->
            profileProgress.visibility = View.GONE
            rel_no_network.visibility = View.VISIBLE
        })

        requestQueue!!.add(jsonArrayRequest)
    }

    private fun onDpFabClick() {
        dp!!.setOnClickListener({ view ->
            showPhotoSourceDialog(view)
        })

        dpFab!!.setOnClickListener({ view ->
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

        powerMenu!!.showAsAnchorCenter(rootProfileView)
    }

    private fun startUploading() {
        updateDP()
    }

    private fun createApplicationFolder(data: Intent, from: String) {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()
        dp!!.visibility = View.INVISIBLE
        dpFab!!.visibility = View.INVISIBLE
        dpUpdateProgress!!.visibility = View.VISIBLE
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
        bottomNavigationViewEx!!.visibility = View.GONE
        val dpURL = arrayOf("http://" + getString(R.string.ip) + "/Snapbook/index.php/PhotoController/uploadDP")
        CallHttpRequest(applicationContext, destination!!, PROFILE_ACTIVITY, dpUpdateProgress, dp, dpFab, bottomNavigationViewEx).execute(dpURL[0])
    }

    private fun onOptionsClick() {
        options!!.setOnClickListener({ view ->
            //Toast.makeText(ctx,"Pressed",Toast.LENGTH_SHORT).show()
            powerMenu = PowerMenu.Builder(ctx)
                    .addItem(PowerMenuItem("Edit Profile", false))
                    .addItem(PowerMenuItem("LogOut", false))
                    .setAnimation(MenuAnimation.DROP_DOWN)
                    .setMenuRadius(10f)
                    .setMenuShadow(10f)
                    .setTextColor(ctx!!.resources.getColor(R.color.black, null))
                    .setSelectedTextColor(Color.WHITE)
                    .setMenuColor(Color.WHITE)
                    .setSelectedMenuColor(ctx!!.resources.getColor(R.color.colorPrimary, null))
                    .setOnMenuItemClickListener(this)
                    .build()

            powerMenu!!.showAsDropDown(view)
        })
    }

    override fun onItemClick(position: Int, item: PowerMenuItem?) {
        when (item!!.title) {
            "Edit Profile" -> {
                startActivity(Intent(ctx, EditProfileActivity::class.java))
                Bungee.inAndOut(ctx)
                powerMenu!!.dismiss()
            }
            "LogOut" -> {
                editor!!.clear()
                editor!!.apply()
                editor!!.commit()

                startActivity(Intent(ctx, LoginActivity::class.java))
                Bungee.inAndOut(ctx)
                powerMenu!!.dismiss()
                finish()
            }
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
}
