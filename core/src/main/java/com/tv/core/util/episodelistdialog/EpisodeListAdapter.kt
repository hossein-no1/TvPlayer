package com.tv.core.util.episodelistdialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.tv.core.databinding.RecyclerEpisodeBinding
import com.tv.core.util.RecyclerItemClick

internal class EpisodeListAdapter :
    ListAdapter<EpisodeModel, EpisodeListAdapter.ViewHolder>
        (
        object : DiffUtil.ItemCallback<EpisodeModel>() {
            override fun areItemsTheSame(oldItem: EpisodeModel, newItem: EpisodeModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EpisodeModel, newItem: EpisodeModel): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    var recyclerItemClick: RecyclerItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerEpisodeBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position),position, recyclerItemClick)
    }

    inner class ViewHolder(private val binding: RecyclerEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root), DpadViewHolder {

        fun onBind(model: EpisodeModel,position: Int, episodeItemClick: RecyclerItemClick?) {
            itemView.setOnClickListener {
                episodeItemClick?.onItemClickListener(model, position)
            }
            binding.tvTitleRecyclerEpisode.text = model.title
            Glide.with(itemView.context).load(model.cover).into(binding.ivCoverRecyclerEpisode)
        }

        override fun onViewHolderDeselected() {
            super.onViewHolderDeselected()
            binding.rlParentCoverRecyclerEpisode.isSelected = false
            binding.tvTitleRecyclerEpisode.visibility = View.INVISIBLE
        }

        override fun onViewHolderSelected() {
            super.onViewHolderSelected()
            binding.rlParentCoverRecyclerEpisode.isSelected = true
            binding.tvTitleRecyclerEpisode.visibility = View.VISIBLE
        }
    }
}