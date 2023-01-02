package com.tv.core.util

import androidx.appcompat.widget.AppCompatTextView

interface AdvertisePlayerListener : TvPlayerListener{
    fun onSkipTimeChange(currentTime : Int, skippTime : Int,textViewSkip : AppCompatTextView?)
}