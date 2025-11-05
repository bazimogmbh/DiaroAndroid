package com.pixelcrater.Diaro.gallery

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GalleryItem (var filename: String, var entryDate: String, var entryUid: String): Parcelable