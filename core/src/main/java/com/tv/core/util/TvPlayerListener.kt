package com.tv.core.util

import com.tv.core.util.mediaItems.MediaItemParent

interface TvPlayerListener {
    fun onPlayerError(error: TvPlayBackException){}
    fun onPlaybackStateChanged(playbackState: Int){}
    fun onMediaStartToPlay(mediaItem : MediaItemParent){}
    fun onMediaChange(mediaItem : MediaItemParent){}
    fun onMediaComplete(mediaItem : MediaItemParent){}
    fun onMediaListComplete(mediaItem : MediaItemParent){}
    fun onControllerVisibilityChanged(visibility: Int){}
}