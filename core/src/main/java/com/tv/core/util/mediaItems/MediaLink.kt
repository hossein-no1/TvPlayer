package com.tv.core.util.mediaItems

import android.net.Uri

data class MediaLink(
    val title: String = "Auto",
    val link: String,
    val adTagUri: Uri = Uri.EMPTY,
    val source: String = "Source",
    internal var isSelected: Boolean = false
)
