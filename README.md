# TvPlayer
A plyer that optimized for tv and based on Exo player google
# Implement in gradle
In the module gradle, dependencies insert this line:
<br/>
`implementation 'com.github.hossein-no1:TvPlayer:vx,x,x'`
check the lateast relese version in github.
<br/>
<br/>

<h1>Version 2.*.*</h1>

What's happend in this version?
-for use library, only implement library(without exoplayer library)
-implement internal ui(for simplePlayer, advertisePlayer and livePlayer), but can edit them with everride that xml file or ovverride that styles
-define some attribute for small chagne of player views
-improve some class, for easer use it!

<h2>Declaration code</h2>
There are 3 defferent player that all them extended of BasePlayer<br/>
1. SimplePlayer<br/>
2. AdvertisePlayer<br/>
3. LivePlayer<br/>
<br/>
For easier use, please take a look at the sample project

<h1>Version 1.*.*</h1>
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
