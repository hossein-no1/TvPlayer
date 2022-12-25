package com.tv.core.util

interface TvPlayerListener {
    fun onPlayerError(error: Exception){}
    fun onPlaybackStateChanged(playbackState: Int){}
}