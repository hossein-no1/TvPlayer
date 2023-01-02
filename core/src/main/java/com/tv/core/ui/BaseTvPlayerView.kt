package com.tv.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.tv.core.base.BasePlayer

abstract class BaseTvPlayerView(private val mContext: Context, private val attrs: AttributeSet?) :
    FrameLayout(mContext, attrs) {

    lateinit var playerView: PlayerView
    private lateinit var view : View

    fun init(layoutResId: Int) {
        view = LayoutInflater.from(context).inflate(layoutResId, this, true)
        obtainAttributes(mContext, attrs)
    }

    fun setupPlayerView(playerViewId: Int){
        playerView = view.findViewById(playerViewId)
        findViews()
        updateUi()
    }

    protected abstract fun obtainAttributes(context: Context, attrs: AttributeSet?)
    protected abstract fun findViews()
    protected abstract fun updateUi()
    open fun setupElement(playerHandler: BasePlayer, isLive: Boolean) {}
    open fun setupElement(adPlayerHandler: BasePlayer) {}
    open fun changeSubtitleState(isThereSubtitle: Boolean) {}
    open fun changeQualityState(isThereQualities: Boolean) {}

}