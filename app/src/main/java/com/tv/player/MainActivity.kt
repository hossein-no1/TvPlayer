package com.tv.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.tv.core.base.SimplePlayer
import com.tv.core.util.AdvertisePlayerHandler
import com.tv.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val filmWithoutSubtitleLink =
        "http://dl.gemescape.com/film/2022/Blanda/SoftSub/B.l.o.n.d.e.2022.1080p.10bit.WEBRip.6CH.x265.HEVC.PSA.SoftSub.PK.mkv"

    private val filmLive =
        "http://92.42.50.29/PLTV/88888888/224/3221226140/index.m3u8?rrsip=92.42.50.29&zoneoffset=0&devkbps=462-3000&servicetype=1&icpid=&accounttype=1&limitflux=-1&limitdur=-1&tenantId=9802&accountinfo=Xrt3KWcoCI5FlXhu5Zv8JxQnVoci7noamwPvHTQ6SQVrjR7xZ7nipIwU5pQG2f%2Bq%2BLz9EbXMj63mpUXJp%2FZIaRvnmpBPEQUyO0R0FdMse4cviE4toyc9SSc00HhDS3lJhZ9K4b4ymiVCSHr0%2BPpFtYRcSWuBLLH0De9WBPjgyJ8%3D%3A20211207150037%3AUTC%2C10001007206428%2C77.81.151.194%2C20211207150037%2Curn%3AHuawei%3AliveTV%3AXTV100000148%2C10001007206428%2C-1%2C0%2C1%2C%2C%2C2%2C%2C%2C%2C2%2C10000506368856%2C0%2C10500006361944%2C384af8f5386e59cb%2C%2C%2C1%2C1%2CEND&GuardEncType=2&it=H4sIAAAAAAAAADWOwQ6CMBBE_6bHplDB7aEnjYmJQRPRq1nLUomFaosm_r2AeNjL7LzJ6wMa2q61WiqQBCBB5ourkpAta2VyQlQKcpWwSM_C65QZdK7pbOGrETsfV5dE8DRNeCrGY-U4uHFotZi6xau9UtDZHzxSeDeGdBVr_sbI0dpAFvvGd_zg8HMKbq4wKme5JJcAMIhkQkjWj2mJ8T582A3jyrcPDFTtvJ0AXaOLxB5o7mipwJZ093Lux-1DNdh8AQYNhMH2AAAA"

    private val ad1 =
        "https://uptv-test.s3.ir-thr-at1.arvanstorage.com/ads/7467080130868809932.MP4"

    private val ad2 =
        "https://hajifirouz6.asset.aparat.com/aparat-video/f911642b62f9155be11dccc19cd43c0248239895-1080p.mp4?wmsAuthSign=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IjI1NTU2YzJmNDQwZjBjODY2ZTRhODRlZTBiZTBiYzNhIiwiZXhwIjoxNjY1OTIzNDYyLCJpc3MiOiJTYWJhIElkZWEgR1NJRyJ9.y0ut_r2dHXt-6RJIR3wnIFhVjK_nt4cWQTL1gObz4c4"

    private lateinit var playerHandler: SimplePlayer

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerHandler = SimplePlayer(
            context = this,
            playerView = binding.playerView
        )

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(filmWithoutSubtitleLink))
            .build()

        playerHandler.addListener(playerListener)
        playerHandler.addMedia(mediaItem)
        playerHandler.preparePlayer()
        playerHandler.play()

        findViewById<AppCompatImageButton>(R.id.ib_subtitles).setOnClickListener {
            playerHandler.showSubtitle(R.style.alertDialog)
        }

    }

    private val playerListener = object : Player.Listener {

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            binding.isLoading = playbackState == ExoPlayer.STATE_BUFFERING
            if (playbackState == ExoPlayer.STATE_READY)
                findViewById<RelativeLayout>(R.id.parent_pauseAndPlay).requestFocus()
            if (playbackState == ExoPlayer.STATE_BUFFERING) {
                //Start video loading
                binding.loading.visibility = View.VISIBLE
            } else {
                //End video loading
                binding.loading.visibility = View.GONE
            }
        }

    }

    private val advertisePlayerHandler = object : AdvertisePlayerHandler {
        override fun playBackStateChange(playbackState: Int) {
        }

        override fun onPlayerError(error: PlaybackException) {
        }

        override fun onSkipTimeChange(currentTime: Int, skippTime: Int) {
            findViewById<AppCompatTextView>(R.id.skippAd).apply {
                this.visibility = View.VISIBLE
                this.text = " تا رد کردن آگهی$currentTime"
                if (currentTime <= 0)
                    this.text = "ردکردن آگهی"
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (playerHandler.player.isPlaying && !binding.playerView.isControllerVisible) {
            if (event.action == KeyEvent.ACTION_UP) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        playerHandler.fastRewindIncrement()

                        binding.lottieFastRewind.visibility = View.VISIBLE
                        Handler(mainLooper).postDelayed({
                            binding.lottieFastRewind.visibility = View.INVISIBLE
                        }, 2_000)
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        playerHandler.fastForwardIncrement()

                        binding.lottieFastForward.visibility = View.VISIBLE
                        Handler(mainLooper).postDelayed({
                            binding.lottieFastForward.visibility = View.INVISIBLE
                        }, 2_000)
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER -> {
                        playerHandler.pause()
                    }
                }

                return false
            } else {
                return false
            }

        } else {
            return super.dispatchKeyEvent(event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHandler.release()
    }
}