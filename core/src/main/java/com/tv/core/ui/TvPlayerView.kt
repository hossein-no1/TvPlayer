package com.tv.core.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.exoplayer2.ui.PlayerView
import com.tv.core.R
import com.tv.core.base.TvPlayer

@SuppressLint("MissingInflatedId")
class TvPlayerView(private val mContext: Context, attrs: AttributeSet?) :
    BaseTvPlayerView(mContext, attrs) {

    private var showSubtitleButton: Boolean? = true
    private var showQualityButton: Boolean? = false
    private var playerViewBackground: Int? = 0
    private var liveAnimationColor: Int? = 0
    private var showLiveAnimation: Boolean? = true

    private var lottieLiveAnimation: LottieAnimationView? = null
    private var ibSubtitle: AppCompatImageButton? = null
    private var ibQuality: AppCompatImageButton? = null

    private var subtitleDialogTitle = "Select subtitle"
    private var subtitleDialogButtonText = "Off subtitle"
    private var qualityDialogTitle = "Select quality"
    private var qualityDialogButtonText = "Close"
    private var subtitleDialogResIdStyle = R.style.defaultAlertDialogStyle
    private var qualityDialogResIdStyle = R.style.defaultAlertDialogStyle

    init {
        init(
            layoutResId = R.layout.default_player_layout
        )
    }

    @SuppressLint("Recycle")
    override fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.TvPlayerView)
            showSubtitleButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_subtitle_button, true)

            showQualityButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_quality_button, false)

            playerViewBackground =
                typedArray.getResourceId(R.styleable.TvPlayerView_player_view_background, 0)

            liveAnimationColor =
                typedArray.getResourceId(R.styleable.TvPlayerView_live_animation_color, 0)

            showLiveAnimation =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_live_animation, true)

        } finally {
            typedArray?.recycle()
        }
    }

    fun qualityButtonVisibility(isVisible: Boolean) {
        ibQuality?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun subtitleButtonVisibility(isVisible: Boolean) {
        ibSubtitle?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun liveAnimationVisibility(isVisible: Boolean){
        lottieLiveAnimation?.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    override fun findViews() {
        ibSubtitle = findViewById(R.id.ib_subtitles)
        ibQuality = findViewById(R.id.ib_qualities)
        lottieLiveAnimation = findViewById(R.id.lottie_liveAnimation)
    }

    override fun updateUi() {
        subtitleButtonVisibility(showSubtitleButton ?: true)

        playerViewBackground?.let { safeBackground ->
            if (safeBackground > 0) {
                playerView.setBackgroundColor(ContextCompat.getColor(mContext, safeBackground))
            }
        }

        liveAnimationVisibility(showLiveAnimation ?: false)
        liveAnimationColor?.let { safeColor ->
            if (safeColor > 0) {
                lottieLiveAnimation?.addValueCallback(
                    KeyPath("**"), LottieProperty.COLOR_FILTER
                ) {
                    return@addValueCallback PorterDuffColorFilter(
                        ContextCompat.getColor(
                            mContext,
                            safeColor
                        ), PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
        }
    }

    override fun setupElement(playerHandler: TvPlayer, isLive: Boolean) {
        setupPlayerView(if (isLive) R.id.default_live_player_view else R.id.default_player_view)

        ibSubtitle?.setOnClickListener {
            playerHandler.showSubtitle(
                dialogTitle = subtitleDialogTitle,
                dialogButtonText = subtitleDialogButtonText,
                resIdStyle = subtitleDialogResIdStyle
            )
        }
        ibQuality?.setOnClickListener {
            playerHandler.showQuality(
                dialogTitle = qualityDialogTitle,
                dialogButtonText = qualityDialogButtonText,
                resIdStyle = qualityDialogResIdStyle
            )
        }

        if (isLive) {
            findViewById<PlayerView>(R.id.default_player_view).visibility = View.INVISIBLE
            findViewById<PlayerView>(R.id.default_live_player_view).visibility = View.VISIBLE
        } else {
            findViewById<PlayerView>(R.id.default_player_view).visibility = View.VISIBLE
            findViewById<PlayerView>(R.id.default_live_player_view).visibility = View.INVISIBLE
        }
    }

    override fun changeSubtitleState(isThereSubtitle: Boolean) {
        isThereSubtitle.apply {
            ibSubtitle?.isFocusable = this
            ibSubtitle?.isFocusableInTouchMode = this
            ibSubtitle?.isClickable = this
            ibSubtitle?.alpha = if (this) 1F else .5F
        }
    }

    override fun changeQualityState(isThereQualities: Boolean) {
        qualityButtonVisibility(isThereQualities && (showQualityButton == true))
    }

    fun changeSubtitleDialogTexts(title: String = "Select quality", buttonText: String = "Close") {
        this.subtitleDialogTitle = title
        this.subtitleDialogButtonText = buttonText
    }

    fun changeQualityDialogTexts(
        title: String = "Select subtitle",
        buttonText: String = "Off subtitle"
    ) {
        this.qualityDialogTitle = title
        this.qualityDialogButtonText = buttonText
    }

    fun setSubtitleDialogStyle(resId: Int) {
        this.subtitleDialogResIdStyle = resId
    }

    fun setQualityDialogStyle(resId: Int) {
        this.qualityDialogResIdStyle = resId
    }

}