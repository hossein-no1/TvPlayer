package com.tv.core.util

import java.util.UUID

data class AdvertiseItem(
    val id : String = UUID.randomUUID().toString(),
    var url: String,
    val subtitleItem: SubtitleItem? = null
)