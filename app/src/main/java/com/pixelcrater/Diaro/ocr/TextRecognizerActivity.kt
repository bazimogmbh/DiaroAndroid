package com.pixelcrater.Diaro.ocr

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.activitytypes.TypeBindingActivity
import com.pixelcrater.Diaro.databinding.TextRecognizerBinding
import com.pixelcrater.Diaro.utils.MyThemesUtils
import com.pixelcrater.Diaro.utils.Static
import com.stfalcon.imageviewer.StfalconImageViewer
import com.yalantis.ucrop.UCrop
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumFile
import java.io.File
import java.io.IOException


class TextRecognizerActivity : TypeBindingActivity<TextRecognizerBinding>(){

    private lateinit var loadedUri: Uri
    private lateinit var loadingDialog: Dialog
    private val CROP_REQUEST = 1001;
    override fun inflateLayout(layoutInflater: LayoutInflater)  = TextRecognizerBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbar(binding.toolbar)
        activityState!!.setActionBarTitle(supportActionBar, R.string.text_recognition)

        loadingDialog = ProgressDialog(this@TextRecognizerActivity)

        startImagePicker()

        binding.fab.visibility = View.GONE
        binding.fab.backgroundTintList = ColorStateList.valueOf(MyThemesUtils.getAccentColor())
        binding.fab.rippleColor = MyThemesUtils.getDarkColor(MyThemesUtils.getAccentColorCode())

        binding.textResultView.visibility = View.GONE
        binding.imgDetailsView.visibility = View.GONE

        binding.imgDetailsView.setOnClickListener {
            if (this::loadedUri.isInitialized) {
                StfalconImageViewer.Builder(this, listOf(loadedUri)) { view, path ->
                    val photoFile = loadedUri.toFile()
                    Glide.with(this).load(loadedUri).signature(Static.getGlideSignature(photoFile)).fitCenter().into(view);
                }.show()
            }
        }

        // fab
        binding.fab.setOnClickListener {
            val intent = Intent()
            intent.putExtra("text", binding.textResultView.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun startImagePicker() {
        // Registers a photo picker activity launcher in single-select mode.
       /** val pickMedia = registerForActivityResult<PickVisualMediaRequest, Uri>(PickVisualMedia()) { uri: Uri? ->
            if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet) {
                MyApp.getInstance().securityCodeMgr.setUnlocked()
            }
            if (uri != null) {
                startImageCropper(uri)
            } else {

            }
        }
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))**/

        Album.image(this).singleChoice().camera(true).columnCount(3).onResult { result: ArrayList<AlbumFile> ->
            if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet) {
                MyApp.getInstance().securityCodeMgr.setUnlocked()
            }
            val fileUri = Uri.fromFile(File(result[0].path))
            startImageCropper(fileUri)
        }
            .onCancel { result: String? -> }
            .start()
    }

    private fun startImageCropper(sourceUri: Uri) {
        /** val cacheCropRequest = CropRequest.Auto(sourceUri = sourceUri, requestCode = CROP_REQUEST, storageType = StorageType.CACHE)
        Croppy.start(this, cacheCropRequest) **/

        val destUri = Uri.fromFile(
                File(externalCacheDir, "ocr.png")
        )
        val uCropOptions = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.PNG)
            setFreeStyleCropEnabled(true)
            setMaxBitmapSize(2048)
        }

        UCrop.of(sourceUri, destUri).withOptions(uCropOptions).start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.fab.visibility = View.VISIBLE
        binding.imgDetailsView.visibility = View.VISIBLE
        binding.textResultView.visibility = View.VISIBLE

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                val fileUri = UCrop.getOutput(data)
                if (fileUri != null) {
                    val photoFile = fileUri.toFile()
                    Glide.with(this).load(fileUri).signature(Static.getGlideSignature(photoFile)).centerCrop().into(binding.ivImageDetails);
                    loadedUri = fileUri
                    recognizeText(fileUri)
                }
            }
        }

        if (resultCode == RESULT_OK && requestCode == CROP_REQUEST) {
            if (data != null) {
                val fileUri = data.data
                if (fileUri != null) {
                    Glide.with(this).load(fileUri).centerCrop().into(binding.ivImageDetails);
                    loadedUri = fileUri
                    recognizeText(fileUri)
                }
            }
        }
        if (resultCode == RESULT_CANCELED) {
            val intent = Intent()
            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }

    private fun recognizeText(uri: Uri) {
        val image: InputImage
        loadingDialog.show()
        try {
            image = InputImage.fromFilePath(this, uri)
            val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image).addOnSuccessListener {
                loadingDialog.dismiss()
                if (it != null)
                    showResult(it)
            }.addOnFailureListener {
                loadingDialog.dismiss()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun showResult(result: Text) {
        val stringBuilder = StringBuilder()
        for (block in result.textBlocks) {
            for (paragraph in block.lines) {
                stringBuilder.append(paragraph.text.replace("\n", " "))
                stringBuilder.append("\n")
            }
        }
        val finalText = stringBuilder.toString().trim()
        binding.textResultView.setText(finalText)
    }

}