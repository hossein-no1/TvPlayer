package com.tv.core.util

interface TvPlayerListener {
    fun onPlayerError(error: Exception){}
    fun onPlaybackStateChanged(playbackState: Int){}
    fun onMediaPlay(mediaItem : MediaItem){}
    fun onMediaChange(mediaItem : MediaItem){}
    fun onMediaComplete(mediaItem : MediaItem){}
    fun onMediaListComplete(mediaItem : MediaItem){}
}