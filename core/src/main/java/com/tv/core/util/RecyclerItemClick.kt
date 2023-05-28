package com.tv.core.util

import com.tv.core.util.episodelistdialog.EpisodeModel

interface RecyclerItemClick {
    fun onItemClickListener(episodeModel : EpisodeModel, position: Int)
}