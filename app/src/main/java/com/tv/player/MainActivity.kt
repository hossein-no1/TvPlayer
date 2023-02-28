package com.tv.player

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tv.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btSimplePlayerActivityMain.setOnClickListener {
            startActivity(Intent(this, SimplePlayerActivity::class.java))
        }
        binding.btAdvertisePlayerActivityMain.setOnClickListener {
            startActivity(Intent(this, AdvertisePlayerActivity::class.java))
        }
        binding.btImaPlayerActivityMain.setOnClickListener {
            startActivity(Intent(this, ImaPlayerActivity::class.java))
        }
        binding.btLivePlayerActivityMain.setOnClickListener {
            startActivity(Intent(this, LivePlayerActivity::class.java))
        }

    }

}