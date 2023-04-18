package com.tv.core.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.exoplayer2.text.Cue.TEXT_SIZE_TYPE_ABSOLUTE
import com.google.android.exoplayer2.ui.CaptionStyleCompat
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.tv.core.R
import com.tv.core.base.TvPlayer
import com.tv.core.util.TvDispatchKeyEvent
import kotlinx.coroutines.*

@SuppressLint("MissingInflatedId")
class TvPlayerView(private val mContext: Context, attrs: AttributeSet?) :
    BaseTvPlayerView(mContext, attrs) {

    private lateinit var playerHandler: TvPlayer

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

    private lateinit var iranSansTypeFace: Typeface

    private lateinit var tvIncrement: AppCompatTextView
    private lateinit var tvDecrement: AppCompatTextView
    private lateinit var tvDurationKeyControl: AppCompatTextView
    private lateinit var tvPositionKeyControl: AppCompatTextView
    private lateinit var llParentRewindAnimation: LinearLayout
    private lateinit var llParentFastForwardAnimation: LinearLayout
    private lateinit var llParentVideoState: LinearLayout

    private var incrementLongPressJob: Job? = null
    private var decrementLongPressJob: Job? = null
    private var incrementLongPressValidation = false
    private var decrementLongPressValidation = false
    private var incrementCounter = 10
    private var decrementCounter = 10

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

    override fun findViews() {
        ibSubtitle = findViewById(R.id.ib_subtitles)
        ibQuality = findViewById(R.id.ib_qualities)
        ibAudioTack = findViewById(R.id.ib_audioTrack)
        lottieLiveAnimation = findViewById(R.id.lottie_liveAnimation)
        tvIncrement = findViewById(R.id.tv_labelIncrement)
        tvDecrement = findViewById(R.id.tv_labelDecrement)
        tvDurationKeyControl = findViewById(R.id.tv_durationKeyControl)
        tvPositionKeyControl = findViewById(R.id.tv_positionKeyControl)
        llParentRewindAnimation = findViewById(R.id.ll_parentRewindAnimation)
        llParentFastForwardAnimation = findViewById(R.id.ll_parentFastForwardAnimation)
        llParentVideoState = findViewById(R.id.ll_parentVideoState)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun updateUi() {
        iranSansTypeFace = Typeface.createFromAsset(
            mContext.assets,
            "fonts/iran_sans_x_medium.ttf"
        )
        configureSubtitleView(iranSansTypeFace)
        changePlayerTextTypeFace(iranSansTypeFace)
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
        backgroundTextColor: Int = mContext.getColor(R.color.black_600)
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

    /*
    * Duration and position text view*/
    fun changePlayerTextTypeFace(typeface: Typeface) {
        findViewById<AppCompatTextView>(R.id.exo_duration).typeface = typeface
        findViewById<AppCompatTextView>(R.id.exo_position).typeface = typeface
    }

    override fun setupElement(playerHandler: TvPlayer, isLive: Boolean) {
        this.playerHandler = playerHandler
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
            findViewById<StyledPlayerView>(R.id.default_player_view).visibility = View.INVISIBLE
            findViewById<StyledPlayerView>(R.id.default_live_player_view).visibility = View.VISIBLE
        } else {
            findViewById<StyledPlayerView>(R.id.default_player_view).visibility = View.VISIBLE
            findViewById<StyledPlayerView>(R.id.default_live_player_view).visibility =
                View.INVISIBLE
        }
    }

    override fun changeSubtitleState(isThereSubtitle: Boolean) {
        isThereSubtitle.apply {
            ibSubtitle?.isFocusable = this
            ibSubtitle?.isFocusableInTouchMode = this
            ibSubtitle?.isClickable = this
            ibSubtitle?.alpha = if (this) 1F else .3F
        }
    }

    override fun changeQualityState(isThereQualities: Boolean) {
        isThereQualities.apply {
            ibQuality?.isFocusable = this
            ibQuality?.isFocusableInTouchMode = this
            ibQuality?.isClickable = this
            ibQuality?.alpha = if (this) 1F else .3F
        }
    }

    override fun changeAudioTrackState(isThereDubbed: Boolean) {
        super.changeAudioTrackState(isThereDubbed)
        isThereDubbed.apply {
            ibAudioTack?.isFocusable = this
            ibAudioTack?.isFocusableInTouchMode = this
            ibAudioTack?.isClickable = this
            ibAudioTack?.alpha = if (this) 1F else .3F
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

    fun handleDispatchKeyEvent(
        event: KeyEvent,
        activity: Activity,
        tvDispatcherListener: TvDispatchKeyEvent? = null
    ): Boolean {
        if (!isControllerVisible() && !playerHandler.isAdPlaying()) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                tvDurationKeyControl.text = playerHandler.getDurationString()
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        tvDispatcherListener?.onLeftClick()
                        if (decrementLongPressJob == null) {
                            decrementLongPressValidation = true
                            decrementLongPressJob = GlobalScope.launch(Dispatchers.Main) {
                                llParentRewindAnimation.visibility = View.VISIBLE
                                llParentVideoState.visibility = View.VISIBLE

                                setDecrementLabelText(decrementCounter.toString())

                                delay(80)

                                if (decrementLongPressValidation) {
                                    setDecrementLabelText(decrementCounter.toString())

                                    decrementCounter += 10
                                    tvPositionKeyControl.text =
                                        playerHandler.getPositionString(playerHandler.getCurrentPosition() - (decrementCounter * 1_000))
                                }
                                decrementLongPressJob = null
                            }
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        tvDispatcherListener?.onRightClick()
                        if (incrementLongPressJob == null) {
                            incrementLongPressValidation = true
                            incrementLongPressJob = GlobalScope.launch(Dispatchers.Main) {
                                llParentFastForwardAnimation.visibility = View.VISIBLE
                                llParentVideoState.visibility = View.VISIBLE

                                setIncrementLabelText(incrementCounter.toString())

                                delay(80)

                                if (incrementLongPressValidation) {
                                    setIncrementLabelText(incrementCounter.toString())

                                    incrementCounter += 10
                                    tvPositionKeyControl.text =
                                        playerHandler.getPositionString(playerHandler.getCurrentPosition() + (incrementCounter * 1_000))
                                }
                                incrementLongPressJob = null
                            }
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        tvDispatcherListener?.onEnterClick()
                        showController()
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        tvDispatcherListener?.onBackClick()
                        activity.onBackPressed()
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        tvDispatcherListener?.onUpClick()
                        showController()
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        tvDispatcherListener?.onDownClick()
                        showController()
                    }
                }
                return false
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(80)
                    if (incrementLongPressValidation) {
                        playerHandler.fastForwardIncrement(incrementCounter)
                        incrementCounter = 10
                    } else if (decrementLongPressValidation) {
                        playerHandler.fastRewindIncrement(decrementCounter)
                        decrementCounter = 10
                    }
                    delay(2_000)
                    incrementLongPressValidation = false
                    decrementLongPressValidation = false
                    llParentFastForwardAnimation.visibility = View.INVISIBLE
                    llParentRewindAnimation.visibility = View.INVISIBLE
                    llParentVideoState.visibility = View.INVISIBLE
                }
                return false
            }
        } else {
            return if (incrementLongPressJob == null || decrementLongPressJob == null) {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    hideController()
                    false
                } else {
                    super.dispatchKeyEvent(event)
                }
            } else {
                false
            }
        }
    }

    private fun setIncrementLabelText(text: String) {
        tvIncrement.text = String.format(
            mContext.getString(R.string.label_incrementVideo),
            text
        )
    }

    private fun setDecrementLabelText(text: String) {
        tvDecrement.text = String.format(
            mContext.getString(R.string.label_decrementVideo),
            text
        )
    }

}