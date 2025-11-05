package com.pixelcrater.Diaro.templates

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Template(var uid: String, var name: String, var title: String, var text: String) : Parcelable