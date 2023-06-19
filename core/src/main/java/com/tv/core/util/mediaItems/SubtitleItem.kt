package com.tv.core.util.mediaItems

import com.google.android.exoplayer2.C

class SubtitleItem(
    val url : String = "",
    val language : String = "fa",
    val label : String = "Subtitle",
    val mimeType: String = MimeType.APPLICATION_SUBRIP,
    val selectionFlags : Int = SELECTION_FLAG_AUTOSELECT
){

    companion object{
        const val SELECTION_FLAG_DEFAULT = C.SELECTION_FLAG_DEFAULT
        const val SELECTION_FLAG_AUTOSELECT = C.SELECTION_FLAG_AUTOSELECT
        const val SELECTION_FLAG_FORCED = C.SELECTION_FLAG_FORCED
    }

}