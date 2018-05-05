package com.hrrock.snapbook.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hrrock.snapbook.R
import com.hrrock.snapbook.activities.PostUploadingActivity
import com.hrrock.snapbook.activities.HomeActivity
import com.hrrock.snapbook.adapters.GridViewAdapter
import com.hrrock.snapbook.utils.FilePaths
import com.hrrock.snapbook.utils.FileSearch
import com.jaredrummler.materialspinner.MaterialSpinner
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter
import spencerstudios.com.bungeelib.Bungee
import java.io.*

/**
 * Created by hp-u on 2/18/2018.
 */
class GalleryViewFragment : Fragment() {
    private var ctx: Context? = null
    private var gridView: GridView? = null
    private var destination: File? = null
    private var galImageView: ImageView? = null
    private var close: ImageView? = null
    private var mobno: String? = null
    private var directories: MutableList<String>? = null
    private var selectedImg: String? = null
    private var spinner: MaterialSpinner? = null
    private var finalPicDestination: String? = null
    private var myIntent: Intent? = null
    private var next: TextView? = null

    private companion object {
        private const val NUM_GRID_COL = 4
        private const val TAG="GALLERY"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gallery_view, container, false)
        ctx = activity

        gridView = v.findViewById(R.id.gridViewGallery)
        galImageView = v.findViewById(R.id.galleryImageView)
        spinner = v.findViewById(R.id.photoDirectory)
        close = v.findViewById(R.id.ivCloseMoment)
        next = v.findViewById(R.id.tvNext)
        directories = ArrayList()

        close!!.setOnClickListener { view ->
            startActivity(Intent(ctx, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            Bungee.fade(ctx)
            activity!!.finish()
        }

        next!!.setOnClickListener { view ->
            myIntent = Intent(ctx, PostUploadingActivity::class.java)
            myIntent!!.putExtra("imgURL", finalPicDestination + "")
            myIntent!!.putExtra("from",TAG)
            startActivity(myIntent)

            Bungee.slideLeft(ctx)
        }

        init()
        return v
    }

    private fun init() {
        val filePaths = FilePaths()

        if (File(filePaths.SDCARD_CAMERA).exists() && FileSearch.getDirectoryPaths(filePaths.SDCARD_CAMERA) != null) {
            directories!!.add(filePaths.SDCARD_CAMERA)
        }

        if (File(filePaths.CAMERA).exists() && FileSearch.getDirectoryPaths(filePaths.CAMERA) != null) {
            directories!!.add(filePaths.CAMERA)
        }

        if (File(filePaths.SDCARD_Retrica).exists() && FileSearch.getDirectoryPaths(filePaths.SDCARD_Retrica) != null) {
            directories!!.add(filePaths.SDCARD_Retrica)
        }

        if (File(filePaths.INSTAGRAM).exists() && FileSearch.getDirectoryPaths(filePaths.INSTAGRAM) != null) {
            directories!!.add(filePaths.INSTAGRAM)
        }

        if (File(filePaths.RETRICA).exists() && FileSearch.getDirectoryPaths(filePaths.RETRICA) != null) {
            directories!!.add(filePaths.RETRICA)
        }

        if (File(filePaths.SCREENSHOTS).exists() && FileSearch.getDirectoryPaths(filePaths.SCREENSHOTS) != null) {
            directories!!.add(filePaths.SCREENSHOTS)
        }

        if (File(filePaths.WHATSAPP).exists() && FileSearch.getDirectoryPaths(filePaths.WHATSAPP) != null) {
            directories!!.add(filePaths.WHATSAPP)
        }

        val dirNames = ArrayList<String>()
        for (i in directories!!.indices) {
            val indexOfLastSlash = directories!![i].lastIndexOf("/")
            val index = indexOfLastSlash + 1
            val subDirName = directories!![i].substring(index)

            dirNames.add(subDirName)
        }

        val materialSpinnerAdapter = MaterialSpinnerAdapter<String>(ctx, dirNames)
        spinner!!.setAdapter(materialSpinnerAdapter)

        spinner!!.setOnItemSelectedListener { view, position, id, item ->
            setUpGridView(directories!![position])
        }
    }

    private fun setUpGridView(directory: String) {
        val imgURLs = FileSearch.getFilePaths(directory)
        val gridWidth = resources.displayMetrics.widthPixels
        val imageWidth = gridWidth / NUM_GRID_COL

        gridView!!.columnWidth = imageWidth
        val gridVAda = GridViewAdapter(ctx!!, R.layout.layout_photo_profile_grid, imgURLs)
        gridView!!.adapter = gridVAda

        setSelectedImage(imgURLs[0])
        selectedImg = imgURLs[0]

        gridView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            setSelectedImage(imgURLs[i])
            selectedImg = imgURLs[i]
        }
    }

    private fun setSelectedImage(imgURL: String) {
        finalPicDestination = imgURL
        val uri = Uri.fromFile(File(imgURL))
        Glide.with(activity!!).load(uri).into(galImageView!!)
    }

    @Throws(IOException::class)
    private fun saveImageGallery(data: Uri) {
        val thumbnail = BitmapFactory.decodeFile(data.encodedPath.toString())
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        destination = File("sdcard/Pictures/Snapbook", "$mobno.jpg")


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
}
