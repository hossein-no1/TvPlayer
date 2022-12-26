package com.tv.core.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.DefaultTimeBar

class TvDefaultTimeBar : DefaultTimeBar {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        timebarAttrs: AttributeSet?
    ) : super(context, attrs, defStyleAttr, timebarAttrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        timebarAttrs: AttributeSet?,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, timebarAttrs, defStyleRes)
}