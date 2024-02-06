package com.tv.core.util

import com.tv.core.util.mediaItems.MediaItemParent

interface TvPlayerListener {
    fun onPlayerError(
        error: TvPlayBackException,
        currentMediaItem: MediaItemParent,
        currentMediaItemIndex: Int
    ){}
    fun onPlaybackStateChanged(playbackState: Int){}
    fun onMediaStartToPlay(mediaItem: MediaItemParent, currentMediaItemIndex: Int){}
    fun onMediaChange(mediaItem : MediaItemParent){}
    fun onMediaComplete(mediaItem : MediaItemParent){}
    fun onMediaListComplete(mediaItem : MediaItemParent){}
    fun onControllerVisibilityChanged(visibility: Int){}
    fun onAdRollStarted(adGroupIndex: Int, adIndexInAdGroup: Int) {}
}