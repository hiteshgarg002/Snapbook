package com.hrrock.snapbook.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.crowdfire.cfalertdialog.CFAlertDialog
import com.hrrock.snapbook.R
import com.hrrock.snapbook.networks.CallHttpRequest
import com.hrrock.snapbook.networks.VolleyConnect
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.activity_create_deal_final.*
import kotlinx.android.synthetic.main.snippet_top_create_deal_final.*
import lib.kingja.switchbutton.SwitchMultiButton
import spencerstudios.com.bungeelib.Bungee
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class CreateDealFinalActivity : AppCompatActivity() {
    private var ctx: Context? = null
    private var requestQueue: RequestQueue? = null
    private var stringRequest: StringRequest? = null
    private var myIntent: Intent? = null
    private var destination: File? = null
    private var preferences: SharedPreferences? = null
    private var timeStamp: Long? = null
    private var description: String? = null
    private var productName: String? = null
    private var category: String? = null
    private var condition: SwitchMultiButton? = null
    private var status: SwitchMultiButton? = null
    private var photoName: String? = null

    private companion object {
        private val CREATE_DEAL_ACTIVITY = "CreateDealActivity"
        private const val CAM_REQ_CODE = 1
        private const val GAL_REQ_CODE = 2
        private const val CAMERA = "camera"
        private const val GALLERY = "gallery"
        private const val USER_PREFERENCE = "userinfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_deal_final)
        ctx = this
        preferences = ctx?.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE)
        requestQueue = VolleyConnect.getInstance().requestQueue

        condition = findViewById(R.id.conditionOnCreateDeal)
        status = findViewById(R.id.statusOnCreateDeal)

        setNavigationStatusBarColor()
        setPhoto()
        getIntentData()
        postDeal()
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return dateFormat.format(calendar.time).toString()
    }

    private fun uploadPhoto() {
        val photoURL = arrayOf("http://" + getString(R.string.ip) + "/Snapbook/index.php/PhotoController/uploadProductPhoto")
        CallHttpRequest(ctx, destination!!, CREATE_DEAL_ACTIVITY).execute(photoURL[0])
    }

    private fun postDeal() {
        postDeal.setOnClickListener({
            val url = "http://${getString(R.string.ip)}/Snapbook/index.php/DealsController/createDeal?username=${preferences!!.getString("username", "")}" +
                    "&email=${preferences!!.getString("email", "")}&mobileno=${preferences!!.getString("mobileno", "")}&name=$productName" +
                    "&description=$description&category=$category&condition=${if (condition?.selectedTab == 0) {
                        "New"
                    } else {
                        "Old"
                    }}&status=${if (status?.selectedTab == 0) {
                        "Available"
                    } else {
                        "Out of Stock"
                    }}&cost=${costOnCreateDeal.text}&date=${getCurrentDate()}&photo=$photoName"

            val dealURL = url.replace(" ", "+")
            stringRequest = StringRequest(dealURL, Response.Listener<String> { response ->
                if (response == "success") {
                    // DealsActivity.destroyActivity()
                    uploadPhoto()
                    myIntent = Intent(ctx, DealsActivity::class.java)
                    myIntent!!.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT

                    startActivity(myIntent)
                    Bungee.inAndOut(ctx)

                    CreateDealActivity.destroyActivity()
                    finish()
                } else {
                    failureSnack()
                }
            }, Response.ErrorListener { error ->
                failureSnack()
            })

            requestQueue?.add(stringRequest)
        })
    }

    private fun failureSnack() {
        Snacky.builder()
                .setActivity(this)
                .setText("Something went wrong! Try again :(")
                .setDuration(Snacky.LENGTH_LONG)
                .error()
                .show()
    }

    private fun setNavigationStatusBarColor() {
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(ctx!!, R.color.light_gray)
        window.navigationBarColor = resources.getColor(R.color.white, null)
    }

    private fun showPhotoSourceDialog() {
        val builder = CFAlertDialog.Builder(ctx)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                .setTitle("Select a Photo")
                .addButton("Gallery", -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED) { dialog, which ->
                    myIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(myIntent, GAL_REQ_CODE)
                    dialog.dismiss()
                }.addButton("Camera", -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED) { dialog, which ->
                    myIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(myIntent, CAM_REQ_CODE)
                    dialog.dismiss()
                }
        builder!!.show()
    }

    private fun setPhoto() {
        productPhotoOnCreateDeal.setOnClickListener({
            showPhotoSourceDialog()
        })

        tagProductPhotoOnCreateDeal.setOnClickListener({
            showPhotoSourceDialog()
        })
    }

    private fun loadSelectedPhoto(destination: String) {
        Glide.with(ctx!!)
                .load(destination)
                .into(productPhotoOnCreateDeal)
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
        timeStamp = System.currentTimeMillis() / 1000L
        val thumbnail = MediaStore.Images.Media.getBitmap(this.contentResolver, data)
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        photoName = "${preferences?.getString("username", "")}_${timeStamp}_deal.jpg"
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

        val photoDestination = "sdcard/Pictures/Snapbook/$photoName"
        loadSelectedPhoto(photoDestination)
    }

    private fun saveImageCamera(data: Intent) {
        timeStamp = System.currentTimeMillis() / 1000L
        val thumbnail = data.extras!!.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        photoName = "${preferences?.getString("username", "")}_${timeStamp}_deal.jpg"
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

        val photoDestination = "sdcard/Pictures/Snapbook/$photoName"
        loadSelectedPhoto(photoDestination)
    }

    private fun getIntentData() {
        productName = intent.getStringExtra("productname")
        description = intent.getStringExtra("productdesc")
        category = intent.getStringExtra("productcat")
    }

    @Throws(IOException::class)
    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei = ExifInterface(image_absolute_path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)

            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)

            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)

            else -> bitmap
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) (-1).toFloat() else 1F, if (vertical) (-1).toFloat() else 1F)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}