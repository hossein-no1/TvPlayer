package com.tv.core.util

import com.google.android.exoplayer2.PlaybackException

interface AdvertisePlayerHandler {
    fun playBackStateChange(playbackState : Int)
    fun onPlayerError(error: PlaybackException)
    fun onSkipTimeChange(currentTime : Int, skippTime : Int)
}