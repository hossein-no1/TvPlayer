package com.tv.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.tv.core.base.BasePlayer

abstract class BaseTvPlayerView(private val mContext: Context,private val attrs: AttributeSet?) : FrameLayout(mContext, attrs) {

    lateinit var playerView: PlayerView

    fun init(resLayout : Int, playerViewId : Int){
        val view = LayoutInflater.from(context).inflate(resLayout, this, true)
        playerView = view.findViewById(playerViewId)
        findViews()
        obtainAttributes(mContext, attrs)
        updateUi()
    }

    protected abstract fun obtainAttributes(context: Context, attrs: AttributeSet?)
    protected abstract fun findViews()
    protected abstract fun updateUi()
    open fun setupElement(playerHandler: BasePlayer){}
    open fun changeSubtitleState(isThereSubtitle: Boolean){}
    open fun changeQualityState(isThereQualities: Boolean){}

}