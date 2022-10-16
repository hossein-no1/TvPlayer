package com.tv.player

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.google.android.exoplayer2.ui.PlayerView

class OptimizedPlayerView :
    PlayerView {

    var playerKeyEventListener : PlayerKeyEventListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        playerKeyEventListener?.keyListener(event)
        return super.dispatchKeyEvent(event)
    }

}