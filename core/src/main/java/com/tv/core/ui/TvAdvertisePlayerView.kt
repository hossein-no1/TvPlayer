package com.tv.core.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.tv.core.R
import com.tv.core.base.TvPlayer

@SuppressLint("MissingInflatedId")
class TvAdvertisePlayerView(private val mContext: Context, attrs: AttributeSet?) :
    BaseTvPlayerView(mContext, attrs) {

    private var playerViewBackground: Int? = 0

    var tvSkipAd: AppCompatTextView? = null

    init {
        init(
            layoutResId = R.layout.default_advertise_player_layout
        )
        setupPlayerView(R.id.default_advertise_player_view)
    }

    @SuppressLint("Recycle")
    override fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.TvAdvertisePlayerView)

            playerViewBackground =
                typedArray.getResourceId(
                    R.styleable.TvAdvertisePlayerView_player_view_background,
                    0
                )
        } finally {
            typedArray?.recycle()
        }
    }

    override fun findViews() {
        tvSkipAd = findViewById(R.id.skippAd)
    }

    override fun updateUi() {
        playerViewBackground?.let { safeBackground ->
            if (safeBackground > 0) {
                playerView.setBackgroundColor(ContextCompat.getColor(mContext, safeBackground))
            }
        }
    }

    override fun setupElement(adPlayerHandler: TvPlayer) {
        super.setupElement(adPlayerHandler)
        tvSkipAd?.setOnClickListener {
            adPlayerHandler.startForceVideo()
        }
    }

}