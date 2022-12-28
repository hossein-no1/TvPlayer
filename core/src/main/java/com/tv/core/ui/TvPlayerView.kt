package com.tv.core.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerView
import com.tv.core.R
import com.tv.core.base.BasePlayer

@SuppressLint("MissingInflatedId")
class TvPlayerView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    var playerView: PlayerView

    private var showSubtitleButton: Boolean? = false
    private var showQualityButton: Boolean? = false
    private var playerViewBackground: Int? = 0

    private var ibSubtitle: AppCompatImageButton? = null
    private var ibQuality: AppCompatImageButton? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.default_player_layout, this, true)
        playerView = view.findViewById(R.id.default_player_view)
        findViews()
        obtainAttributes(context, attrs)
        updateUi()
    }

    @SuppressLint("Recycle")
    private fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.TvPlayerView)
            showSubtitleButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_subtitle_button, false)

            showQualityButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_quality_button, true)

            playerViewBackground =
                typedArray.getResourceId(R.styleable.TvPlayerView_player_view_background , 0)
        } finally {
            typedArray?.recycle()
        }
    }

    private fun findViews() {
        ibSubtitle = findViewById(R.id.ib_subtitles)
        ibQuality = findViewById(R.id.ib_qualities)
    }

    private fun updateUi() {
        ibSubtitle?.visibility =
            if (showSubtitleButton == true) View.VISIBLE else View.INVISIBLE

        playerViewBackground?.let { safeBackground ->
            if (safeBackground > 0) {
                playerView.setBackgroundColor(ContextCompat.getColor(context, safeBackground))
            }
        }
    }

    fun setupElement(playerHandler: BasePlayer) {
        ibSubtitle?.setOnClickListener {
            playerHandler.showSubtitle()
        }
        ibQuality?.setOnClickListener {
            playerHandler.showQuality()
        }
    }

    fun changeSubtitleState(isThereSubtitle: Boolean) {
        isThereSubtitle.apply {
            ibSubtitle?.isFocusable = this
            ibSubtitle?.isFocusableInTouchMode = this
            ibSubtitle?.isClickable = this
            ibSubtitle?.alpha = if (this) 1F else .5F
        }
    }

    fun changeQualityState(isThereQualities: Boolean) {
        ibQuality?.visibility =
            if (isThereQualities && (showQualityButton == true)) View.VISIBLE else View.INVISIBLE
    }

}