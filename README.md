# TvPlayer
A plyer that optimized for tv and based on Exo player google
# Implement in gradle
In the module gradle, dependencies insert this line:
<br/>
`implementation 'com.github.hossein-no1:TvPlayer:vx,x,x'`
check the lateast relese version in github.
<br/>
<br/>
# Setup Simple player
1.Create a player view in xml<br/>
2.Pass the player view to SimplePlayer constructor<br/>
`playerHandler = SimplePlayer(context = this,playerView = binding.playerView)`<br/>
3.Create a media item<br/><br/>
`        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(filmWithoutSubtitleLink))
            .setTag(MediaSourceType.Progressive)
            .setSubtitleConfigurations(listOf(subtitle))
            .build()`<br/>
MediaSourceType is for video type that when equal by MediaSourceType.Progressive(default) play .mp4 or .mkv and MediaSourceType.HLS play .m3u8<br/>
4.Add a listener for player and prepare that play
<br/>
<br/>
# Setup advertise player
1.Create a player view with custome controller in xml<br/>
`app:show_timeout="0"
app:hide_on_touch="false"`
2.Pass the advertise player view to AdvertisePlayer constructor<br/>
3.Create advertise media item<br/>
`playerHandler.addMediaAdvertise(media = mediaItem, skippTime = 10)`<br/>
4.Setup advertisePlayerHandler and set to player<br/>
5.Call playAdvertiseAutomatic for player<br/>
