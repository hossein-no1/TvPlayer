package com.tv.core.util.episodelistdialog

data class EpisodeModel(
    val id : String,
    val title : String,
    val cover : String,
    val startPosition : Long = 0L
)
