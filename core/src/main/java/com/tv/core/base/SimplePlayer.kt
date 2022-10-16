package com.tv.core.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class SimplePlayer(
    private val context: Context,
    private val playerView: PlayerView,
    playWhenReady: Boolean = true
) : BasePlayer() {

    private var trackSelector: DefaultTrackSelector
    private var player: ExoPlayer

    init {
        val videoTrackSelectionFactory =
            AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(context.applicationContext, videoTrackSelectionFactory)
        player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
        playerView.player = player
        player.playWhenReady = playWhenReady
    }

    override fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    override fun addMedia(media: MediaItem, index: Int) {
        player.addMediaItem(index, media)
    }

    override fun addMediaList(media: List<MediaItem>, index: Int) {
        player.addMediaItems(index, media)
    }

    override fun isThereSubtitle(): Boolean {
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                return true
            }
        }
        return false
    }

    override fun preparePlayer() {
        player.prepare()
    }

    override fun play() {
        player.play()
    }

    override fun release() {
        playerView.player = null
        player.release()
    }

    override fun showSubtitle(overrideThemeResId: Int) {
        val subtitleLanguageList = ArrayList<String>()
        val subtitlesList = ArrayList<String>()
        for (group in player.currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                val groupInfo = group.mediaTrackGroup
                for (i in 0 until groupInfo.length) {
                    subtitleLanguageList.add(groupInfo.getFormat(i).language.toString())
                    subtitlesList.add(
                        "${subtitlesList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage
                                + " (${
                            if (groupInfo.getFormat(i).label == null) "Subtitle" else groupInfo.getFormat(
                                i
                            ).label
                        })"
                    )
                }
            }
        }

        val tempTracks = subtitlesList.toArray(arrayOfNulls<CharSequence>(subtitlesList.size))
        val subtitleDialog =
            MaterialAlertDialogBuilder(context, overrideThemeResId)
                .setTitle("Select Subtitles")
                .setOnCancelListener { }
                .setPositiveButton("Off Subtitles") { self, _ ->
                    trackSelector.setParameters(
                        trackSelector.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_VIDEO, true
                        )
                    )
                    self.dismiss()
                }
                .setItems(tempTracks) { _, position ->
                    trackSelector.setParameters(
                        trackSelector.buildUponParameters()
                            .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                            .setPreferredTextLanguage(subtitleLanguageList[position])
                    )
                }
                .create()
        subtitleDialog.show()
        subtitleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        subtitleDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
    }

}