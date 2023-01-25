package com.tv.core.util

class MediaItem(
    var url: String,
    val subtitleItems: List<SubtitleItem> = listOf(),
    val isLive: Boolean = false,
    defaultQualityTitle: String = "Item",
) {

    private val qualityList: MutableList<MediaQuality> =
        mutableListOf(MediaQuality(defaultQualityTitle, url))

    fun addQuality(quality: String, url: String): MediaItem {
        qualityList.add(MediaQuality(quality, url))
        if (qualityList.size > 1) qualityList.first().isSelected = true
        return this
    }

    fun getQualityList(): List<MediaQuality> = qualityList

    fun isThereQuality() = qualityList.size > 1

    @Throws(IndexOutOfBoundsException::class)
    fun setDefaultQuality(index: Int) {
        if (index < getQualityList().size && index >= 0) {
            resetQualitySelected()
            qualityList[index].isSelected = true
            url = qualityList[index].link
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

    internal fun changeQualityUriInItem(qualitySelectedPosition: Int): MediaItem {
        resetQualitySelected()
        url = qualityList[qualitySelectedPosition].link
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
    val title: String, val link: String, var isSelected: Boolean = false
)