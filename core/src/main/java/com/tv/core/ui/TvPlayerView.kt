package com.tv.core.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.exoplayer2.text.Cue.TEXT_SIZE_TYPE_ABSOLUTE
import com.google.android.exoplayer2.ui.CaptionStyleCompat
import com.google.android.exoplayer2.ui.PlayerView
import com.tv.core.R
import com.tv.core.base.TvPlayer

@SuppressLint("MissingInflatedId")
class TvPlayerView(private val mContext: Context, attrs: AttributeSet?) :
    BaseTvPlayerView(mContext, attrs) {

    private var showSubtitleButton: Boolean? = true
    private var showQualityButton: Boolean? = true
    private var showAudioTrackButton: Boolean? = false
    private var playerViewBackground: Int? = 0
    private var liveAnimationColor: Int? = 0
    private var showLiveAnimation: Boolean? = true

    private var lottieLiveAnimation: LottieAnimationView? = null
    private var ibSubtitle: AppCompatImageButton? = null
    private var ibQuality: AppCompatImageButton? = null
    private var ibAudioTack: AppCompatImageButton? = null

    private var subtitleDialogTitle = "Select subtitle"
    private var subtitleDialogButtonText = "Off subtitle"
    private var qualityDialogTitle = "Select quality"
    private var qualityDialogButtonText = "Close"
    private var audioTrackDialogTitle = "Select dubbed"
    private var audioTrackDialogButtonText = "Close"
    private var subtitleDialogResIdStyle = R.style.defaultAlertDialogStyle
    private var qualityDialogResIdStyle = R.style.defaultAlertDialogStyle
    private var audioTrackDialogResIdStyle = R.style.defaultAlertDialogStyle

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
                typedArray.getBoolean(R.styleable.TvPlayerView_show_quality_button, true)

            showAudioTrackButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_dubbed_button, false)

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

    fun audioTrackButtonVisibility(isVisible: Boolean) {
        ibAudioTack?.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    fun liveAnimationVisibility(isVisible: Boolean) {
        lottieLiveAnimation?.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun findViews() {
        configureSubtitleView(
            Typeface.createFromAsset(
                mContext.assets,
                "fonts/iran_sans_x_medium.ttf"
            )
        )
        ibSubtitle = findViewById(R.id.ib_subtitles)
        ibQuality = findViewById(R.id.ib_qualities)
        ibAudioTack = findViewById(R.id.ib_audioTrack)
        lottieLiveAnimation = findViewById(R.id.lottie_liveAnimation)
    }

    override fun updateUi() {
        subtitleButtonVisibility(showSubtitleButton ?: true)
        qualityButtonVisibility(showQualityButton ?: true)
        audioTrackButtonVisibility(showAudioTrackButton ?: false)

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

    @RequiresApi(Build.VERSION_CODES.M)
    fun configureSubtitleView(
        typeface: Typeface,
        textSize: Float = 18F,
        textColor: Int = Color.WHITE,
        backgroundTextColor: Int = mContext.getColor(R.color.black_200)
    ) {
        val subtitleView = playerView.subtitleView
        subtitleView?.setApplyEmbeddedFontSizes(false)
        subtitleView?.setApplyEmbeddedStyles(false)
        subtitleView?.setFixedTextSize(TEXT_SIZE_TYPE_ABSOLUTE, textSize)
        subtitleView?.setBottomPaddingFraction(.1F)

        val style = CaptionStyleCompat(
            textColor, backgroundTextColor, Color.TRANSPARENT,
            CaptionStyleCompat.EDGE_TYPE_NONE, Color.TRANSPARENT, typeface
        )
        subtitleView?.setStyle(style)
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

        ibAudioTack?.setOnClickListener {
            playerHandler.showAudioTrack(
                dialogTitle = audioTrackDialogTitle,
                dialogButtonText = audioTrackDialogButtonText,
                resIdStyle = audioTrackDialogResIdStyle
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
        isThereQualities.apply {
            ibQuality?.isFocusable = this
            ibQuality?.isFocusableInTouchMode = this
            ibQuality?.isClickable = this
            ibQuality?.alpha = if (this) 1F else .5F
        }
    }

    override fun changeAudioTrackState(isThereDubbed: Boolean) {
        super.changeAudioTrackState(isThereDubbed)
        isThereDubbed.apply {
            ibAudioTack?.isFocusable = this
            ibAudioTack?.isFocusableInTouchMode = this
            ibAudioTack?.isClickable = this
            ibAudioTack?.alpha = if (this) 1F else .5F
        }
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

    fun changeDubbedDialogTexts(
        title: String = "Select dubbed",
        buttonText: String = "Close"
    ) {
        this.audioTrackDialogTitle = title
        this.audioTrackDialogButtonText = buttonText
    }

    fun setSubtitleDialogStyle(resId: Int) {
        this.subtitleDialogResIdStyle = resId
    }

    fun setQualityDialogStyle(resId: Int) {
        this.qualityDialogResIdStyle = resId
    }

    fun setDubbedDialogStyle(resId: Int) {
        this.audioTrackDialogResIdStyle = resId
    }

}