package com.tv.core.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.tv.core.R

class TvLivePlayerView(private val mContext: Context, attrs: AttributeSet?) :
    BaseTvPlayerView(mContext, attrs) {

    private var playerViewBackground: Int? = 0
    private var liveAnimationColor: Int? = 0
    private var showLiveAnimation: Boolean? = true

    private var lottieLiveAnimation: LottieAnimationView? = null

    init {
        init(
            resLayout = R.layout.default_live_player_layout,
            playerViewId = R.id.default_live_player_view
        )
    }

    override fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.TvLivePlayerView)
            playerViewBackground =
                typedArray.getResourceId(R.styleable.TvLivePlayerView_player_view_background, 0)

            liveAnimationColor =
                typedArray.getResourceId(R.styleable.TvLivePlayerView_live_animation_color, 0)

            showLiveAnimation =
                typedArray.getBoolean(R.styleable.TvLivePlayerView_show_live_animation, true)
        } finally {
            typedArray?.recycle()
        }
    }

    override fun findViews() {
        lottieLiveAnimation = findViewById(R.id.lottie_liveAnimation)
    }

    override fun updateUi() {
        playerViewBackground?.let { safeBackground ->
            if (safeBackground > 0) {
                playerView.setBackgroundColor(ContextCompat.getColor(mContext, safeBackground))
            }
        }

        lottieLiveAnimation?.visibility =
            if (showLiveAnimation == true) View.VISIBLE else View.INVISIBLE
        liveAnimationColor?.let { safeColor ->
            if (safeColor > 0) {
                lottieLiveAnimation?.addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER
                ) {
                    return@addValueCallback PorterDuffColorFilter(ContextCompat.getColor(mContext,safeColor), PorterDuff.Mode.SRC_ATOP)
                }
            }
        }

    }

}