package com.pixelcrater.Diaro.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.activitytypes.TypeActivity
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager
import com.pixelcrater.Diaro.storage.dropbox.DropboxLocalHelper
import com.pixelcrater.Diaro.storage.dropbox.SyncService
import com.pixelcrater.Diaro.utils.AppLog
import com.pixelcrater.Diaro.utils.Static
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils
import com.pixelcrater.Diaro.utils.storage.StorageUtils
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfilePhotoActivity : TypeActivity() {

    private var profilePhotoView: ImageView? = null
    private var profilePhotoFile: File? = null
    private var CROP_REQUEST = 1001;

    // *** Broadcast Receiver ***
    private val brReceiver: BroadcastReceiver = BrReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(addViewToContentContainer(R.layout.profile_photo))
        activityState.setLayoutBackground()

        // Photo
        profilePhotoView = findViewById<View>(R.id.profile_photo) as ImageView
        profilePhotoFile = File(AppLifetimeStorageUtils.getProfilePhotoFilePath())

        // Register broadcast receiver
        ContextCompat.registerReceiver(this,brReceiver, IntentFilter(Static.BR_IN_PROFILE_PHOTO), ContextCompat.RECEIVER_NOT_EXPORTED)

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState)
    }

    private fun restoreDialogListeners(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val dialog1 = supportFragmentManager.findFragmentByTag(Static.DIALOG_CONFIRM_PHOTO_DELETE) as ConfirmDialog?
            dialog1?.let { setPhotoDeleteConfirmDialogListener(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_profile_photo, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AppLog.d("item: $item")
        return if (activityState.isActivityPaused) true else when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.item_delete -> {
                showDeletePhotoConfirmation()
                true
            }
            R.id.item_edit -> {
                val fileUri = Uri.fromFile(File(AppLifetimeStorageUtils.getProfilePhotoFilePath()))
                startImageCropper(fileUri)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode ==  UCrop.REQUEST_CROP) {
            if (data != null) {
                val uri = UCrop.getOutput(data)
                val bitmap: Bitmap?
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    finishCrop(bitmap)
                } catch (e: IOException) {
                    Static.showToastError(String.format("%s:", e.message))
                }
            }
        }

        if (resultCode == RESULT_OK && requestCode == CROP_REQUEST) {
            if (data != null) {
                val uri = data.data
                val bitmap: Bitmap?
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    finishCrop(bitmap)
                } catch (e: IOException) {
                    Static.showToastError(String.format("%s:", e.message))
                }
            }
        }
    }

    private fun startImageCropper(sourceUri: Uri) {


        val destUri = Uri.fromFile(
                File(externalCacheDir, "profile.png")
        )
        val uCropOptions = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.PNG)
            setFreeStyleCropEnabled(true)
            setMaxBitmapSize(2048)
        }

        UCrop.of(sourceUri, destUri).withOptions(uCropOptions).start(this)

        //  val ignoredAspectRations = Arrays.asList(AspectRatio.ASPECT_INS_STORY, AspectRatio.ASPECT_FACE_POST, AspectRatio.ASPECT_FACE_COVER, AspectRatio.ASPECT_PIN_POST, AspectRatio.ASPECT_YOU_COVER, AspectRatio.ASPECT_TWIT_POST, AspectRatio.ASPECT_TWIT_HEADER)
   //     val cacheCropRequest = CropRequest.Auto(sourceUri = sourceUri, CROP_REQUEST, storageType = StorageType.CACHE)
   //     Croppy.start(this, cacheCropRequest)
    }


    fun finishCrop(bitmap: Bitmap) {
        try {
            val tmpProfileImg = File(externalCacheDir.toString() + "/profileimage.jpg")
            val out = FileOutputStream(tmpProfileImg)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            Static.copyFileOrDirectory(tmpProfileImg, profilePhotoFile)
            StorageUtils.compressPhoto(profilePhotoFile!!.path, 1024, 100)
            updateProfilePhoto()
            Static.sendBroadcast(Static.BR_IN_MAIN, Static.DO_UPDATE_PROFILE_PHOTO, null)
        } catch (e: Exception) {
            // Show error toast
            Static.showToastError(String.format("%s: %s", getString(R.string.error_add_profile_photo), e.message))
        }
    }

    override fun onResume() {
        super.onResume()
        updateProfilePhoto()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(brReceiver)
    }

    private fun showDeletePhotoConfirmation() {
        val dialogTag = Static.DIALOG_CONFIRM_PHOTO_DELETE
        if (supportFragmentManager.findFragmentByTag(dialogTag) == null) {
            // Show dialog
            val dialog = ConfirmDialog()
            dialog.setTitle(getString(R.string.delete))
            dialog.message = getString(R.string.delete_selected_photo)
            dialog.show(supportFragmentManager, dialogTag)

            // Set dialog listener
            setPhotoDeleteConfirmDialogListener(dialog)
        }
    }

    private fun setPhotoDeleteConfirmDialogListener(dialog: ConfirmDialog) {
        dialog.setDialogPositiveClickListener {

            // Delete profile photo file from SD card
            StorageUtils.deleteFileOrDirectory(profilePhotoFile)
            DropboxLocalHelper.setProfilePicChanged(true)
            if (MyApp.getInstance().networkStateMgr.isConnectedToInternet && DropboxAccountManager.isLoggedIn(this@ProfilePhotoActivity)) {
                // Start sync service
                SyncService.startService()
            }

            // Send broadcasts to profile photo
            Static.sendBroadcastsToUpdateProfilePhoto()
            finish()
        }
    }

    private fun updateProfilePhoto() {
        if (DropboxAccountManager.isLoggedIn(this) && profilePhotoFile!!.exists() && profilePhotoFile!!.length() > 0) {
            val dm = resources.displayMetrics

            // Show photo
            Glide.with(this@ProfilePhotoActivity).load(profilePhotoFile).signature(Static.getGlideSignature(profilePhotoFile))
                    .override(dm.widthPixels, dm.heightPixels).fitCenter().error(R.drawable.ic_photo_red_24dp).into(profilePhotoView!!)
        } else {
            finish()
        }
    }

    private inner class BrReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val doWhat = intent.getStringExtra(Static.BROADCAST_DO)

            // - Update profile photo -
            if (doWhat == Static.DO_UPDATE_PROFILE_PHOTO) {
                updateProfilePhoto()
            }
        }
    }
}