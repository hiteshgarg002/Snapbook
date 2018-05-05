package com.hrrock.snapbook.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.hrrock.snapbook.R
import com.hrrock.snapbook.networks.CallHttpRequest
import com.hrrock.snapbook.networks.VolleyConnect
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.thekhaeng.pushdownanim.PushDownAnim
import kotlinx.android.synthetic.main.activity_post_uploading.*
import kotlinx.android.synthetic.main.snippet_top_finalizing_photo.*
import spencerstudios.com.bungeelib.Bungee
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PostUploadingActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var photoURL: String? = null
    private var destination: File? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var preferences: SharedPreferences? = null
    private var timeStamp: Long = 0
    private var photoName: String? = null
    private var share: ImageView? = null

    companion object {
        private const val MOMENT_ACTIVITY = "MomentActivity"
        private const val USER_PREFERENCE = "userinfo"
        private const val CAMERA = "camera"
        private const val GALLERY = "gallery"
        private const val TAG_CAMERA = "CAMERA"
        private const val TAG_GALLERY = "GALLERY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_uploading)
        ctx = this

        preferences = ctx!!.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
        requestQueue = VolleyConnect.getInstance().requestQueue
        photoURL = intent.getStringExtra("imgURL")
        share = findViewById(R.id.share)

        if (photoURL != "") {
            setSelectedPhoto(photoURL!!)
        }
        //createApplicationFolder(Intent(Intent.ACTION_PICK, photoURL), GALLERY)
        // val bitmap=BitmapFactory.decodeFile(photoURL)

        when (intent.getStringExtra("from")) {
            TAG_CAMERA -> {
                /* destination = File(photoURL+"")
                 photoName=photoURL!!.substringAfterLast("/")
                 timeStamp=System.currentTimeMillis()/1000L
                 Toast.makeText(ctx,""+photoURL,Toast.LENGTH_SHORT).show()
                 Toast.makeText(ctx,""+ TAG_CAMERA,Toast.LENGTH_SHORT).show()
                 Toast.makeText(ctx,""+ photoName,Toast.LENGTH_SHORT).show()*/
                val uri = Uri.fromFile(File(photoURL))
                //  Toast.makeText(ctx,""+uri,Toast.LENGTH_SHORT).show()
                createApplicationFolderForCamera(uri)
            }
            TAG_GALLERY -> {
                val uri = Uri.fromFile(File(photoURL))
                // Toast.makeText(ctx,""+uri,Toast.LENGTH_SHORT).show()
                createApplicationFolderForGallery(uri)
                //  Toast.makeText(ctx,""+photoURL,Toast.LENGTH_SHORT).show()
                //Toast.makeText(ctx,""+ TAG_GALLERY,Toast.LENGTH_SHORT).show()
            }
        }

        setNavigationStatusBarColor()
        shareOnClickAnim()

        backToPhotoSelection.setOnClickListener({
            onBackPressed()
        })

        Picasso.with(ctx).load(ctx!!.getString(R.string.BASE_URL_PROFILE_PIC) + preferences!!.getString("username", "") + ".jpg")
                .placeholder(R.drawable.no_dp_big)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(dpOnFinalUpload)
    }

    private fun shareOnClickAnim() {
        PushDownAnim.setPushDownAnimTo(share).setOnClickListener({ view ->
            val v = this.currentFocus
            if (v != null) {
                val imm = ctx!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            uploadPhotoDetails()
        })
    }

    private fun setSelectedPhoto(imgURL: String) {
        val uri = Uri.fromFile(File(imgURL))
        Glide.with(ctx!!)
                .load(uri)
                .into(imgOnFinalUpload)
    }

    private fun uploadPhotoDetails() {
        rel_post_uploading.visibility = View.INVISIBLE
        rel_uploadingProgress_post_uploading.visibility = View.VISIBLE
        //val photoId = preferences!!.getString("username", "") + timeStamp.toString()
        val photoDetailsURL = "http://${getString(R.string.ip)}/Snapbook/index.php/PhotoController/uploadPhotoDetails" +
                "?username=${preferences!!.getString("username", "")}&caption=${if (caption.text.toString() != "") {
                    caption.text.toString().replace(" ", "+")
                } else {
                    ""
                }}&timestamp=$timeStamp&photo=$photoName&type=photo&date=${getDateFromTS(timeStamp).replace(" ", "+")}"

        stringRequest = StringRequest(photoDetailsURL, Response.Listener<String> { response ->
            if (response.toString() == "success") {
                uploadPhoto()
            } else {
                jumpToHomeActivity(false)
            }
        }, Response.ErrorListener { error ->
            jumpToHomeActivity(false)
        })

        requestQueue!!.add(stringRequest)
    }

    private fun uploadPhoto() {
        val photoURL = arrayOf("http://" + getString(R.string.ip) + "/Snapbook/index.php/PhotoController/uploadPhoto")
        CallHttpRequest(ctx, destination!!, MOMENT_ACTIVITY).execute(photoURL[0])

        jumpToHomeActivity(true)
    }

    private fun createApplicationFolder(data: Intent, from: String) {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()

        when (from) {
            CAMERA -> saveImageCameraOld(data)
            GALLERY -> saveImageGallery(data.data)
        }
    }

    private fun createApplicationFolderForCameraOld(data: Intent, from: String) {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()

        saveImageCameraOld(data)
    }

    private fun getDateFromTS(timeStamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(timeStamp * 1000L)
    }

    private fun createApplicationFolderForGallery(data: Uri?) {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()

        saveImageGallery(data)
    }

    private fun createApplicationFolderForCamera(data: Uri?) {
        val folder = File(Environment.getExternalStorageDirectory(), File.separator + "Pictures/Snapbook/")
        folder.mkdirs()

        saveImageCamera(data)
    }

    @Throws(IOException::class)
    private fun saveImageGallery(data: Uri?) {
        timeStamp = System.currentTimeMillis() / 1000L
        photoName = preferences!!.getString("username", "") + "_$timeStamp.jpg"

        val thumbnail = MediaStore.Images.Media.getBitmap(this.contentResolver, data)
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 20, bytes)

        destination = File("sdcard/Pictures/Snapbook/", photoName + "")

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
    }

    private fun saveImageCameraOld(data: Intent) {
        timeStamp = System.currentTimeMillis() / 1000L
        photoName = preferences!!.getString("username", "") + "_$timeStamp.jpg"

        val thumbnail = data.extras!!.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 20, bytes)

        destination = File("sdcard/Pictures/Snapbook/", photoName + "")
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
    }

    @Throws(IOException::class)
    private fun saveImageCamera(data: Uri?) {
        timeStamp = System.currentTimeMillis() / 1000L
        photoName = preferences!!.getString("username", "") + "_$timeStamp.jpg"

        val thumbnail = MediaStore.Images.Media.getBitmap(this.contentResolver, data)
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        destination = File("sdcard/Pictures/Snapbook/", photoName + "")
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
    }

    private fun getPath(uri: Uri): String {
        var cursor = contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToFirst()
        var document_id = cursor.getString(0)
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
        cursor.close()

        cursor = contentResolver.query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", arrayOf(document_id), null)
        cursor!!.moveToFirst()
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()

        return path
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.colorGray)
        window.navigationBarColor = resources.getColor(R.color.colorGray)
    }

    private fun jumpToHomeActivity(status: Boolean) {
        val myIntent = Intent(ctx, HomeActivity::class.java)
        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        myIntent.putExtra("status", status)
        startActivity(myIntent)
        Bungee.slideLeft(ctx)
        MomentActivity.destroyActivity()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        when (intent.getStringExtra("from")) {
            TAG_CAMERA -> {
                startActivity(Intent(ctx, HomeActivity::class.java))
                Bungee.slideRight(ctx)
                finish()
            }
            TAG_GALLERY -> {
                startActivity(Intent(ctx, MomentActivity::class.java))
                Bungee.slideRight(ctx)
                finish()
            }
        }
    }
}
