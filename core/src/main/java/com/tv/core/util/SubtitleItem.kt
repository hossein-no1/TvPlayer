package com.tv.core.util

import android.net.Uri

class SubtitleItem(
    val uri : Uri,
    val language : String = "fa",
    val label : String = "Subtitle",
    val mimeType: String = MimeType.APPLICATION_SUBRIP
)