package com.tv.core.util

import android.net.Uri
import java.util.*

class MediaItem(
    val id: String = UUID.randomUUID().toString(),
    var startPositionMs: Long = 0L,
    val subtitleItems: List<SubtitleItem> = listOf(),
    val dubbedList: List<String> = listOf(),
    qualities: List<MediaQuality> = listOf()
) {

    private val qualityList = mutableListOf<MediaQuality>()

    init {
        qualityList.addAll(qualities)
        if (qualities.isNotEmpty()) setDefaultQuality(0)
        if (qualityList.size > 1) qualityList.first().isSelected = true
    }

    fun addQuality(quality: String, link: String, adTagUri: Uri = Uri.EMPTY): MediaItem {
        qualityList.add(MediaQuality(quality, link, adTagUri))
        if (qualityList.size > 1) qualityList.first().isSelected = true
        return this
    }

    internal fun getQualityList(): List<MediaQuality> = qualityList

    fun isThereQuality() = qualityList.size > 1

    @Throws(IndexOutOfBoundsException::class)
    fun setDefaultQuality(index: Int) {
        if (index < getQualityList().size && index >= 0) {
            resetQualitySelected()
            qualityList[index].isSelected = true
            qualityList[0] = qualityList[index].also { qualityList[index] = qualityList[0] }
        } else throw IndexOutOfBoundsException("Not found quality index in the list!")
    }

    @Throws(IndexOutOfBoundsException::class)
    fun setDefaultQuality(quality: String) {
        setDefaultQuality(getQualityIndexWithTitle(quality))
    }

    internal fun getCurrentQuality(): MediaQuality? {
        getQualityList().forEach {
            if (it.isSelected)
                return it
        }
        return null
    }

    private fun getQualityIndexWithTitle(quality: String): Int {
        qualityList.forEachIndexed { index, qualityItem ->
            if (qualityItem.title == quality) return index
        }
        return -1
    }

    internal fun changeQualityUriInItem(qualitySelectedPosition: Int): MediaItem {
        resetQualitySelected()
        qualityList[qualitySelectedPosition].isSelected = true
        return this
    }

    private fun resetQualitySelected() {
        qualityList.forEach { mediaQuality ->
            mediaQuality.isSelected = false
        }
    }

}

class MediaQuality(
    val title: String = "Auto",
    val link: String,
    val adTagUri: Uri = Uri.EMPTY,
    internal var isSelected: Boolean = false
)