package com.tv.core.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.exoplayer2.text.Cue.TEXT_SIZE_TYPE_ABSOLUTE
import com.google.android.exoplayer2.ui.CaptionStyleCompat
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.ui.TimeBar
import com.tv.core.R
import com.tv.core.base.TvPlayer
import com.tv.core.util.RecyclerItemClick
import com.tv.core.util.TVUserAction
import com.tv.core.util.TvDispatchKeyEvent
import com.tv.core.util.episodelistdialog.EpisodeListDialogHelper
import com.tv.core.util.episodelistdialog.EpisodeModel
import kotlinx.coroutines.*

class TvPlayerView(private val mContext: Context, attrs: AttributeSet?) :
    BaseTvPlayerView(mContext, attrs) {

    private lateinit var playerHandler: TvPlayer

    private var showSubtitleButton: Boolean? = true
    private var showQualityButton: Boolean? = true
    private var showLinkButton: Boolean? = true
    private var showSourceButton: Boolean? = true
    private var showAudioTrackButton: Boolean? = true
    private var showEpisodeButton: Boolean? = false
    private var showIncreaseSubtitleButton: Boolean? = false
    private var showReduceButton: Boolean? = false
    private var playerViewBackground: Int? = 0
    private var liveAnimationColor: Int? = 0
    private var showLiveAnimation: Boolean? = true

    private var lottieLiveAnimation: LottieAnimationView? = null
    private var ibSource: AppCompatImageButton? = null
    private var ibLink: AppCompatImageButton? = null
    private var ibQuality: AppCompatImageButton? = null
    private var ibAudioTack: AppCompatImageButton? = null
    private var ibSubtitle: AppCompatImageButton? = null
    private var ibEpisodeList: AppCompatImageButton? = null
    private var ibIncreaseSubtitle: AppCompatImageButton? = null
    private var ibReduceSubtitle: AppCompatImageButton? = null
    private var exoNext: AppCompatImageButton? = null
    private var exoPrev: AppCompatImageButton? = null
    private var exoPlayPause: AppCompatImageButton? = null
    private var exoForward: AppCompatImageButton? = null
    private var exoRewind: AppCompatImageButton? = null
    private var exoProgress: TvDefaultTimeBar? = null

    private var sourceDialogTitle = "Select source"
    private var sourceDialogButtonText = "Close"
    private var subtitleDialogTitle = "Select subtitle"
    private var subtitleDialogButtonText = "Off subtitle"
    private var linkDialogTitle = "Select quality"
    private var linkDialogButtonText = "Close"
    private var qualityDialogTitle = "Select quality"
    private var qualityDialogButtonText = "Close"
    private var qualityDialogItemDefault = "Auto"
    private var audioTrackDialogTitle = "Select dubbed"
    private var audioTrackDialogButtonText = "Close"
    private var subtitleDialogResIdStyle = R.style.defaultAlertDialogStyle
    private var qualityDialogResIdStyle = R.style.defaultAlertDialogStyle
    private var audioTrackDialogResIdStyle = R.style.defaultAlertDialogStyle
    private var sourceDialogResIdStyle = R.style.defaultAlertDialogStyle
    private var linkDialogResIdStyle = R.style.defaultAlertDialogStyle

    private var subtitleLanguageDictionary =
        mapOf("fa" to "فارسی", "en" to "انگلیسی", "eng" to "انگلیسی", null to "زیرنویس")
    private var audioLanguageDictionary = subtitleLanguageDictionary
    private var defaultSubtitle = "زیرنویس"
    private var defaultAudio = "صدا"

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

    private val episodeListDialog by lazy {
        EpisodeListDialogHelper(mContext)
    }

    private var subtitleTextSize: Float = 18F

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

            showSourceButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_source_button, true)

            showLinkButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_link_button, true)

            showQualityButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_quality_button, true)

            showAudioTrackButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_dubbed_button, true)

            showSubtitleButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_subtitle_button, true)

            showEpisodeButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_episode_button, false)

            showIncreaseSubtitleButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_increase_subtitle_button, true)

            showReduceButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_reduce_subtitle_button, true)

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

    fun sourceButtonVisibility(isVisible: Boolean) {
        ibSource?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun linkButtonVisibility(isVisible: Boolean) {
        ibLink?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun qualityButtonVisibility(isVisible: Boolean) {
        ibQuality?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun audioTrackButtonVisibility(isVisible: Boolean) {
        ibAudioTack?.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    fun subtitleButtonVisibility(isVisible: Boolean) {
        ibSubtitle?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun episodeButtonVisibility(isVisible: Boolean) {
        ibEpisodeList?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun increaseSubtitleButtonVisibility(isVisible: Boolean) {
        ibIncreaseSubtitle?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun reduceSubtitleButtonVisibility(isVisible: Boolean) {
        ibReduceSubtitle?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun liveAnimationVisibility(isVisible: Boolean) {
        lottieLiveAnimation?.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    override fun findViews() {
        ibSource = findViewById(R.id.ib_source)
        ibLink = findViewById(R.id.ib_links)
        ibQuality = findViewById(R.id.ib_quality)
        ibAudioTack = findViewById(R.id.ib_audioTrack)
        ibSubtitle = findViewById(R.id.ib_subtitles)
        ibEpisodeList = findViewById(R.id.ib_episodeList)
        ibIncreaseSubtitle = findViewById(R.id.ib_increaseSubtitle)
        ibReduceSubtitle = findViewById(R.id.ib_reduceSubtitle)
        lottieLiveAnimation = findViewById(R.id.lottie_liveAnimation)
        tvIncrement = findViewById(R.id.tv_labelIncrement)
        tvDecrement = findViewById(R.id.tv_labelDecrement)
        tvDurationKeyControl = findViewById(R.id.tv_durationKeyControl)
        tvPositionKeyControl = findViewById(R.id.tv_positionKeyControl)
        llParentRewindAnimation = findViewById(R.id.ll_parentRewindAnimation)
        llParentFastForwardAnimation = findViewById(R.id.ll_parentFastForwardAnimation)
        llParentVideoState = findViewById(R.id.ll_parentVideoState)
        exoNext = findViewById(R.id.exo_next)
        exoPrev = findViewById(R.id.exo_prev)
        exoPlayPause = findViewById(R.id.exo_play_pause)
        exoForward = findViewById(R.id.exo_ffwd)
        exoRewind = findViewById(R.id.exo_rew)
        exoProgress = findViewById(R.id.exo_progress)
    }

    override fun updateUi() {
        iranSansTypeFace = Typeface.createFromAsset(
            mContext.assets,
            "fonts/iran_sans_x_medium.ttf"
        )
        configureSubtitleView(iranSansTypeFace)
        changePlayerTextTypeFace(iranSansTypeFace)
        subtitleButtonVisibility(showSubtitleButton ?: true)
        qualityButtonVisibility(showQualityButton ?: true)
        linkButtonVisibility(showLinkButton ?: true)
        sourceButtonVisibility(showSourceButton ?: true)
        audioTrackButtonVisibility(showAudioTrackButton ?: true)
        episodeButtonVisibility(showEpisodeButton ?: false)
        increaseSubtitleButtonVisibility(showIncreaseSubtitleButton ?: true)
        reduceSubtitleButtonVisibility(showReduceButton ?: true)

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

    fun configureSubtitleView(
        typeface: Typeface,
        textSize: Float = subtitleTextSize,
        textColor: Int = Color.WHITE,
        backgroundTextColor: Int = mContext.getColor(R.color.black_600)
    ) {

        subtitleTextSize = textSize

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

    fun increaseSubtitle() {
        if (subtitleTextSize < 34F) {
            subtitleTextSize += 2F
            configureSubtitleView(iranSansTypeFace)
        }
    }

    fun reduceSubtitle() {
        if (subtitleTextSize > 14F) {
            subtitleTextSize -= 2F
            configureSubtitleView(iranSansTypeFace)
        }
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
        ibSource?.setOnClickListener {
            playerHandler.showSource(
                dialogTitle = sourceDialogTitle,
                dialogButtonText = sourceDialogButtonText,
                resIdStyle = sourceDialogResIdStyle
            )
        }
        ibLink?.setOnClickListener {
            playerHandler.showLink(
                dialogTitle = linkDialogTitle,
                dialogButtonText = linkDialogButtonText,
                resIdStyle = linkDialogResIdStyle
            )
        }
        ibQuality?.setOnClickListener {
            playerHandler.showQuality(
                dialogTitle = qualityDialogTitle,
                dialogButtonText = qualityDialogButtonText,
                resIdStyle = qualityDialogResIdStyle,
                qualityDialogItemDefault = qualityDialogItemDefault,
            )
        }
        ibAudioTack?.setOnClickListener {
            playerHandler.showAudioTrack(
                dialogTitle = audioTrackDialogTitle,
                dialogButtonText = audioTrackDialogButtonText,
                resIdStyle = audioTrackDialogResIdStyle,
                audioLanguageDictionary = audioLanguageDictionary,
                defaultAudio = defaultAudio
            )
        }
        ibSubtitle?.setOnClickListener {
            playerHandler.showSubtitle(
                dialogTitle = subtitleDialogTitle,
                dialogButtonText = subtitleDialogButtonText,
                resIdStyle = subtitleDialogResIdStyle,
                subtitleLanguageDictionary = subtitleLanguageDictionary,
                defaultSubtitle = defaultSubtitle
            )
        }
        ibEpisodeList?.setOnClickListener {
            episodeListDialog.setEpisodeList(playerHandler.mediaItems).show()
            playerHandler.submitUserInteractionLogs(TVUserAction.SHOW_EPISODE)
        }
        ibIncreaseSubtitle?.setOnClickListener {
            increaseSubtitle()
            playerHandler.submitUserInteractionLogs(TVUserAction.INCREASE_SUBTITLE)
        }
        ibReduceSubtitle?.setOnClickListener {
            reduceSubtitle()
            playerHandler.submitUserInteractionLogs(TVUserAction.REDUCE_SUBTITLE)
        }
        episodeListDialog.setOnEpisodeClickListener(object : RecyclerItemClick {
            override fun onItemClickListener(episodeModel: EpisodeModel, position: Int) {
                if (playerHandler.player.currentMediaItemIndex != position)
                    playerHandler.changeMedia(position, episodeModel.startPosition)
                playerHandler.submitUserInteractionLogs(TVUserAction.SELECT_EPISODE)
                episodeListDialog.dismiss()
            }
        })
        exoNext?.setOnClickListener {
            playerHandler.submitUserInteractionLogs(TVUserAction.NEXT_MEDIA)
            playerHandler.player.seekToNext()
        }
        exoPrev?.setOnClickListener {
            playerHandler.submitUserInteractionLogs(TVUserAction.PREV_MEDIA)
            playerHandler.player.seekToPrevious()
        }
        exoPlayPause?.setOnClickListener {
            if (playerHandler.player.playWhenReady) {
                playerHandler.player.pause()
                playerHandler.submitUserInteractionLogs(TVUserAction.PLAY_MEDIA)
            } else {
                playerHandler.player.play()
                playerHandler.submitUserInteractionLogs(TVUserAction.PAUSE_MEDIA)
            }
        }
        exoForward?.setOnClickListener {
            playerHandler.player.seekForward()
            playerHandler.submitUserInteractionLogs(TVUserAction.FORWARD_MEDIA)
        }
        exoRewind?.setOnClickListener {
            playerHandler.player.seekBack()
            playerHandler.submitUserInteractionLogs(TVUserAction.REWIND_MEDIA)
        }
        exoProgress?.addListener(onScrubListener)

        if (isLive) {
            findViewById<StyledPlayerView>(R.id.default_player_view).visibility = View.INVISIBLE
            findViewById<StyledPlayerView>(R.id.default_live_player_view).visibility = View.VISIBLE
            findViewById<StyledPlayerView>(R.id.default_live_player_view).showController()
        } else {
            findViewById<StyledPlayerView>(R.id.default_player_view).visibility = View.VISIBLE
            findViewById<StyledPlayerView>(R.id.default_live_player_view).visibility =
                View.INVISIBLE
        }
    }

    private val onScrubListener = object : TimeBar.OnScrubListener {
        override fun onScrubStart(timeBar: TimeBar, position: Long) {
        }

        override fun onScrubMove(timeBar: TimeBar, position: Long) {
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            playerHandler.player.seekTo(position)
            playerHandler.submitUserInteractionLogs(TVUserAction.SCRUB_MEDIA)
        }
    }

    override fun changeSourceState(isThereSource: Boolean) {
        isThereSource.apply {
            ibSource?.isFocusable = this
            ibSource?.isFocusableInTouchMode = this
            ibSource?.isClickable = this
            ibSource?.alpha = if (this) 1F else .3F
            ibSource?.visibility = if (this) View.VISIBLE else View.GONE
        }
    }

    override fun changeLinkState(isThereLinks: Boolean) {
        isThereLinks.apply {
            ibLink?.isFocusable = this
            ibLink?.isFocusableInTouchMode = this
            ibLink?.isClickable = this
            ibLink?.alpha = if (this) 1F else .3F
            ibLink?.visibility = if (this) View.VISIBLE else View.GONE
        }
    }

    override fun changeQualityState(isThereQualities: Boolean) {
        isThereQualities.apply {
            ibQuality?.isFocusable = this
            ibQuality?.isFocusableInTouchMode = this
            ibQuality?.isClickable = this
            ibQuality?.alpha = if (this) 1F else .3F
            ibQuality?.visibility = if (this) View.VISIBLE else View.GONE
        }
    }

    override fun changeAudioTrackState(isThereDubbed: Boolean) {
        isThereDubbed.apply {
            ibAudioTack?.isFocusable = this
            ibAudioTack?.isFocusableInTouchMode = this
            ibAudioTack?.isClickable = this
            ibAudioTack?.alpha = if (this) 1F else .3F
            ibAudioTack?.visibility = if (this) View.VISIBLE else View.GONE
        }
    }

    override fun changeSubtitleState(isThereSubtitle: Boolean) {
        isThereSubtitle.apply {
            ibSubtitle?.isFocusable = this
            ibSubtitle?.isFocusableInTouchMode = this
            ibSubtitle?.isClickable = this
            ibSubtitle?.alpha = if (this) 1F else .3F
            ibSubtitle?.visibility = if (this) View.VISIBLE else View.GONE

            ibIncreaseSubtitle?.isFocusable = this
            ibIncreaseSubtitle?.isFocusableInTouchMode = this
            ibIncreaseSubtitle?.isClickable = this
            ibIncreaseSubtitle?.alpha = if (this) 1F else .3F
            ibIncreaseSubtitle?.visibility = if (this) View.VISIBLE else View.GONE

            ibReduceSubtitle?.isFocusable = this
            ibReduceSubtitle?.isFocusableInTouchMode = this
            ibReduceSubtitle?.isClickable = this
            ibReduceSubtitle?.alpha = if (this) 1F else .3F
            ibReduceSubtitle?.visibility = if (this) View.VISIBLE else View.GONE
        }
    }

    override fun changeEpisodeListState(isThereEpisodeMediaItems: Boolean) {
        super.changeEpisodeListState(isThereEpisodeMediaItems)
        episodeButtonVisibility((showEpisodeButton == true) && isThereEpisodeMediaItems)
    }

    fun changeSourceDialogTexts(title: String = "Select Source", buttonText: String = "Close") {
        this.sourceDialogTitle = title
        this.sourceDialogButtonText = buttonText
    }

    fun changeSubtitleDialogTexts(
        title: String = "Select quality",
        buttonText: String = "Close",
        subtitleLanguageDictionary: Map<String?, String>? = null
    ) {
        this.subtitleDialogTitle = title
        this.subtitleDialogButtonText = buttonText
        subtitleLanguageDictionary?.let { this.subtitleLanguageDictionary = it }
    }

    fun changeQualityDialogTexts(
        title: String = "Select subtitle",
        buttonText: String = "Off subtitle",
        qualityDialogItemDefault: String = "Auto"
    ) {
        this.qualityDialogTitle = title
        this.qualityDialogButtonText = buttonText
        this.linkDialogTitle = title
        this.linkDialogButtonText = buttonText
        this.qualityDialogItemDefault = qualityDialogItemDefault
    }

    fun changeDubbedDialogTexts(
        title: String = "Select dubbed",
        buttonText: String = "Close",
        audioLanguageDictionary: Map<String?, String>? = null
    ) {
        this.audioTrackDialogTitle = title
        this.audioTrackDialogButtonText = buttonText
        audioLanguageDictionary?.let { this.audioLanguageDictionary = it }
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
        activity: AppCompatActivity,
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
                            decrementLongPressJob =
                                activity.lifecycleScope.launch(Dispatchers.Main) {
                                    llParentRewindAnimation.visibility = View.VISIBLE
                                    llParentVideoState.visibility = View.VISIBLE

                                    setDecrementLabelText(decrementCounter.toString())

                                    delay(80)

                                    if (decrementLongPressValidation) {
                                        setDecrementLabelText(decrementCounter.toString())

                                        decrementCounter += 10
                                        tvPositionKeyControl.text =
                                            playerHandler.getPositionString(playerHandler.getCurrentPosition() - (decrementCounter * 1_000))
                                        playerHandler.submitUserInteractionLogs(TVUserAction.FAST_DECREMENT_COUNTER)
                                    }
                                    decrementLongPressJob = null
                                }
                        }
                    }

                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        tvDispatcherListener?.onRightClick()
                        if (incrementLongPressJob == null) {
                            incrementLongPressValidation = true
                            incrementLongPressJob =
                                activity.lifecycleScope.launch(Dispatchers.Main) {
                                    llParentFastForwardAnimation.visibility = View.VISIBLE
                                    llParentVideoState.visibility = View.VISIBLE

                                    setIncrementLabelText(incrementCounter.toString())

                                    delay(80)

                                    if (incrementLongPressValidation) {
                                        setIncrementLabelText(incrementCounter.toString())

                                        incrementCounter += 10
                                        tvPositionKeyControl.text =
                                            playerHandler.getPositionString(playerHandler.getCurrentPosition() + (incrementCounter * 1_000))
                                        playerHandler.submitUserInteractionLogs(TVUserAction.FAST_INCREMENT_COUNTER)
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
                        activity.onBackPressedDispatcher.onBackPressed()
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
                activity.lifecycleScope.launch(Dispatchers.Main) {
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
            mContext.getString(R.string.tv_label_incrementVideo),
            text
        )
    }

    private fun setDecrementLabelText(text: String) {
        tvDecrement.text = String.format(
            mContext.getString(R.string.tv_label_decrementVideo),
            text
        )
    }

}