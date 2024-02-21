package com.tv.core.util.mediaItems

import android.net.Uri
import java.util.UUID

abstract class MediaItemParent(
    val id: String = UUID.randomUUID().toString(),
    var startPositionMs: Long = 0L,
    val subtitleItems: List<SubtitleItem> = listOf(),
    val dubbedList: List<DubbedItem> = listOf(),
    val links: List<MediaLink> = listOf()
) {

    internal val linkList = mutableListOf<MediaLink>()

    val currentLink: MediaLink?
        get() {
            linkList.forEach {
                if (it.isSelected)
                    return it
            }
            return null
        }

    init {
        linkList.addAll(links)
        if (links.isNotEmpty()) setDefaultQuality(0)
        if (linkList.size > 1) linkList.first().isSelected = true
    }

    fun isThereLink() = linkList.size > 1

    @Throws(IndexOutOfBoundsException::class)
    fun setDefaultQuality(index: Int) {
        if (index < linkList.size && index >= 0) {
            resetQualitySelected()
            linkList[index].isSelected = true
            linkList[0] = linkList[index].also { linkList[index] = linkList[0] }
        } else throw IndexOutOfBoundsException("Not found quality index in the list!")
    }

    @Throws(IndexOutOfBoundsException::class)
    fun setDefaultQuality(quality: String) {
        setDefaultQuality(getQualityIndexWithTitle(quality))
    }

    private fun getQualityIndexWithTitle(quality: String): Int {
        linkList.forEachIndexed { index, qualityItem ->
            if (qualityItem.title == quality) return index
        }
        return -1
    }

    protected fun resetQualitySelected() {
        linkList.forEach { mediaQuality ->
            mediaQuality.isSelected = false
        }
    }

    abstract fun addLink(quality: String, link: String, adTagUri: Uri = Uri.EMPTY): MediaItemParent
    abstract fun changeQualityUriInItem(qualitySelectedPosition: Int): MediaItemParent

}