package com.tv.core.util.mediaItems

import android.net.Uri
import com.tv.core.util.episodelistdialog.EpisodeModel
import java.util.UUID

abstract class MediaItemParent(
    val id: String = UUID.randomUUID().toString(),
    var startPositionMs: Long = 0L,
    val subtitleItems: List<SubtitleItem> = listOf(),
    val dubbedList: List<DubbedItem> = listOf(),
    qualities: List<MediaQuality> = listOf()
) {

    internal val qualityList = mutableListOf<MediaQuality>()

    val currentQuality: MediaQuality?
        get() {
            qualityList.forEach {
                if (it.isSelected)
                    return it
            }
            return null
        }

    init {
        qualityList.addAll(qualities)
        if (qualities.isNotEmpty()) setDefaultQuality(0)
        if (qualityList.size > 1) qualityList.first().isSelected = true
    }

    fun isThereQuality() = qualityList.size > 1

    @Throws(IndexOutOfBoundsException::class)
    fun setDefaultQuality(index: Int) {
        if (index < qualityList.size && index >= 0) {
            resetQualitySelected()
            qualityList[index].isSelected = true
            qualityList[0] = qualityList[index].also { qualityList[index] = qualityList[0] }
        } else throw IndexOutOfBoundsException("Not found quality index in the list!")
    }

    @Throws(IndexOutOfBoundsException::class)
    fun setDefaultQuality(quality: String) {
        setDefaultQuality(getQualityIndexWithTitle(quality))
    }

    private fun getQualityIndexWithTitle(quality: String): Int {
        qualityList.forEachIndexed { index, qualityItem ->
            if (qualityItem.title == quality) return index
        }
        return -1
    }

    protected fun resetQualitySelected() {
        qualityList.forEach { mediaQuality ->
            mediaQuality.isSelected = false
        }
    }

    abstract fun addQuality(quality: String, link: String, adTagUri: Uri = Uri.EMPTY): MediaItemParent
    abstract fun changeQualityUriInItem(qualitySelectedPosition: Int): MediaItemParent

}