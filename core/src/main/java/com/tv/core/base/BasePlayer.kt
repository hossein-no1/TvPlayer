package com.tv.core.base

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

abstract class BasePlayer {

    abstract fun addListener(listener: Player.Listener)

    abstract fun addMedia(media: MediaItem, index: Int = 0)

    abstract fun addMediaList(media: List<MediaItem>, index: Int = 0)

    open fun showSubtitle(overrideThemeResId: Int) {}

    abstract fun isThereSubtitle(): Boolean

    abstract fun preparePlayer()

    abstract fun play()

    abstract fun release()

}