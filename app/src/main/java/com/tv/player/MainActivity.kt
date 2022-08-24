package com.tv.player

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.tv.player.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "hossein"

    private val videoUrl720 =
        "http://dl2.gemescape.com/FILM/2022/Minions/Minions.The.Rise.of.Gru.2022.720p.WEB-HD.x264.Pahe.SoftSub.PK.mkv"

    private lateinit var binding: ActivityMainBinding
    private lateinit var exo: ExoPlayer
    private lateinit var player: ForwardingPlayer
    private lateinit var trackSelector: DefaultTrackSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl720))
            .setMimeType(MimeTypes.TEXT_VTT)
            .build()

        trackSelector = DefaultTrackSelector(this.applicationContext)
        exo = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()

        val player: ForwardingPlayer = object : ForwardingPlayer(exo) {
            override fun getSeekBackIncrement(): Long {
                return 10 * 1000 // 10 sec
            }

            override fun getSeekForwardIncrement(): Long {
                return 10 * 1000 // 10 sec
            }
        }

        binding.playerView.player = player

        player.addListener(playerListener)

        player.addMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        player.play()

        findViewById<LinearLayout>(R.id.parent_subtitles).setOnClickListener {
            showSubtitle()
        }

    }

    private fun showSubtitle() {

        val subtitles = ArrayList<String>()
        val subtitlesList = ArrayList<String>()
        for (group in binding.playerView.player?.currentTracks!!.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                val groupInfo = group.mediaTrackGroup
                for (i in 0 until groupInfo.length) {
                    subtitles.add(groupInfo.getFormat(i).language.toString())
                    subtitlesList.add(
                        "${subtitlesList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage
                                + " (${groupInfo.getFormat(i).label})"
                    )
                }
            }
        }

        val tempTracks = subtitlesList.toArray(arrayOfNulls<CharSequence>(subtitlesList.size))
        val sDialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
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
                Snackbar.make(binding.root, subtitlesList[position] + " Selected", 3000).show()
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setPreferredTextLanguages("en", "fa", "ar")
                        .setPreferredVideoMimeTypes(
                            MimeTypes.APPLICATION_SUBRIP,
                            MimeTypes.TEXT_VTT,
                            MimeTypes.TEXT_SSA,
                            MimeTypes.TEXT_UNKNOWN,
                            MimeTypes.TEXT_EXOPLAYER_CUES
                        )
                        .setAllowVideoMixedMimeTypeAdaptiveness(true)
                        .setRendererDisabled(C.TRACK_TYPE_TEXT, false)
                )
            }
            .create()
        sDialog.show()
        sDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        sDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

    }

    private val playerListener = object : Player.Listener {

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.i(TAG, "onPlayerError: ${error.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}