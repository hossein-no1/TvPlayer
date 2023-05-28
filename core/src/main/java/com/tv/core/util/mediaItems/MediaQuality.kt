package com.tv.core.util.mediaItems

import android.net.Uri

data class MediaQuality(
    val title: String = "Auto",
    val link: String,
    val adTagUri: Uri = Uri.EMPTY,
    internal var isSelected: Boolean = false
)
