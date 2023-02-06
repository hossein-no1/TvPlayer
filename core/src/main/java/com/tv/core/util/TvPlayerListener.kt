package com.tv.core.util

interface TvPlayerListener {
    fun onPlayerError(error: TvPlayBackException){}
    fun onPlaybackStateChanged(playbackState: Int){}
    fun onMediaStartToPlay(mediaItem : MediaItem){}
    fun onMediaChange(mediaItem : MediaItem){}
    fun onMediaComplete(mediaItem : MediaItem){}
    fun onMediaListComplete(mediaItem : MediaItem){}
}