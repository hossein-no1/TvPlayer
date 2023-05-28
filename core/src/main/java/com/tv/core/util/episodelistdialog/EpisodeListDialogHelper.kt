package com.tv.core.util.episodelistdialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.tv.core.R
import com.tv.core.databinding.LayoutEpisodeListBinding
import com.tv.core.util.RecyclerItemClick
import com.tv.core.util.mediaItems.EpisodeMediaItem
import com.tv.core.util.mediaItems.MediaItemParent

internal class EpisodeListDialogHelper(context: Context) {

    private var binding: LayoutEpisodeListBinding
    private val dialog by lazy {
        Dialog(context, R.style.CustomDialogStyle)
    }


    private val episodeListAdapter = EpisodeListAdapter()

    init {
        binding = LayoutEpisodeListBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setCancelable(true)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

        binding.rvEpisodeLayoutEpisodeList.adapter = episodeListAdapter

    }

    fun setEpisodeList(episodeList: List<MediaItemParent>): EpisodeListDialogHelper {
        val episodeItemList = mutableListOf<EpisodeModel>()
        episodeList.forEach { mediaItemParent ->
            if (mediaItemParent is EpisodeMediaItem)
                episodeItemList.add(mediaItemParent.convertToEpisodeModel())
        }
        episodeListAdapter.submitList(episodeItemList)
        checkEmptyList()
        return this
    }

    private fun checkEmptyList(){
        if (episodeListAdapter.itemCount <= 0) {
            binding.tvMessageLayoutEpisodeList.visibility = View.VISIBLE
            binding.rvEpisodeLayoutEpisodeList.visibility = View.INVISIBLE
        }else{
            binding.tvMessageLayoutEpisodeList.visibility = View.INVISIBLE
            binding.rvEpisodeLayoutEpisodeList.visibility = View.VISIBLE
        }
    }

    fun setOnEpisodeClickListener(listener: RecyclerItemClick) {
        episodeListAdapter.recyclerItemClick = listener
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}