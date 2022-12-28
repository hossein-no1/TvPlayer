package com.tv.core.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.tv.core.R
import com.tv.core.base.BasePlayer

@SuppressLint("MissingInflatedId")
class TvPlayerView(private val mContext: Context, attrs: AttributeSet?) : BaseTvPlayerView(mContext, attrs) {

    private var showSubtitleButton: Boolean? = false
    private var showQualityButton: Boolean? = false
    private var playerViewBackground: Int? = 0

    private var ibSubtitle: AppCompatImageButton? = null
    private var ibQuality: AppCompatImageButton? = null

    init {
        init(
            resLayout = R.layout.default_player_layout,
            playerViewId = R.id.default_player_view
        )
    }

    @SuppressLint("Recycle")
    override fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.TvPlayerView)
            showSubtitleButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_subtitle_button, false)

            showQualityButton =
                typedArray.getBoolean(R.styleable.TvPlayerView_show_quality_button, true)

            playerViewBackground =
                typedArray.getResourceId(R.styleable.TvPlayerView_player_view_background, 0)
        } finally {
            typedArray?.recycle()
        }
    }

    override fun findViews() {
        ibSubtitle = findViewById(R.id.ib_subtitles)
        ibQuality = findViewById(R.id.ib_qualities)
    }

    override fun updateUi() {
        ibSubtitle?.visibility =
            if (showSubtitleButton == true) View.VISIBLE else View.INVISIBLE

        playerViewBackground?.let { safeBackground ->
            if (safeBackground > 0) {
                playerView.setBackgroundColor(ContextCompat.getColor(mContext, safeBackground))
            }
        }
    }

    override fun setupElement(playerHandler: BasePlayer) {
        ibSubtitle?.setOnClickListener {
            playerHandler.showSubtitle()
        }
        ibQuality?.setOnClickListener {
            playerHandler.showQuality()
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
        ibQuality?.visibility =
            if (isThereQualities && (showQualityButton == true)) View.VISIBLE else View.INVISIBLE
    }

}