package com.pixelcrater.Diaro.ktxextensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

/**
 * Shows the toast message
 */
fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}


fun Context.copyTextToClipboard(textToCopy: String, textCopiedMessage: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Copied Text", textToCopy)
    clipboardManager.setPrimaryClip(clipData)
    toast(textCopiedMessage)
}
