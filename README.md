# TvPlayer
A player that optimized for tv and based on Exo player google
# Implement in gradle
In the module gradle, dependencies insert this line:
> `implementation 'com.github.hossein-no1:TvPlayer:vx,x,x'`

check the latest release version in github.
and

>`implementation 'com.google.ads.interactivemedia.v3:interactivemedia:x.x.x'`

for show VAST ads

# version 3.4.0
##### What's happened in this version?
1. Change Media item structor
> `MediaItem(qualities = listOf(MediaQuality(title = "Title",link = "MediaLink",adTagUri = "AdUri"))))`

# version 3.3.0
##### What's happened in this version?
1. Handle remote key for rewind and fast forward
2. Add start position in mediaItem

https://user-images.githubusercontent.com/53558241/228231422-fceeaa91-3c4e-43bf-82df-dbe471411627.mp4

# Version 3.*.*
##### What's happened in this version?
- Implement ImaPlayer for show VAST ads
- Add some feature(for read more, take a look to release note)
- Fix some bugs
- Implement dubbed
- Configure custom subtitle text

For use VAST tags:
Create an instance of ImaPlayer and pass ImaAdsLoader object to them
Then create media item and pass a VAST tag uri to them
**Note: please don't forget implementation interactive library for use ImaPlayer**

# Version 2.*.*

##### What's happened in this version?
- For use library, only implement library(without exoplayer library)
- Implement internal ui(for simplePlayer, advertisePlayer and livePlayer), but can edit them with override that xml file or override that styles
- Define some attribute for small change of player views
- Improve some class, for easier use it!

## Declaration code
There are 3 different player that all them extended of BasePlayer
1. SimplePlayer
2. AdvertisePlayer
3. LivePlayer

For easier use, please take a look at the sample project

## Version 1.*.*
##### What's happened in this version?
Implement in gradle
In the module gradle, dependencies insert this line:

> `implementation 'com.github.hossein-no1:TvPlayer:vx,x,x'`

check the latest release version in github.

# Setup Simple player
1. Create a player view in xml
2. Pass the player view to SimplePlayer constructor
> `playerHandler = SimplePlayer(context = this,playerView = binding.playerView)`

3. Create a media item
> `val mediaItem = MediaItem.Builder().setUri(Uri.parse(filmWithoutSubtitleLink))
.setTag(MediaSourceType.Progressive)
.setSubtitleConfigurations(listOf(subtitle))
.build()`

MediaSourceType is for video type that when equal by MediaSourceType.Progressive(default) play .mp4 or .mkv and MediaSourceType.HLS play .m3u8

4. Add a listener for player and prepare that play


# Setup advertise player
1. Create a player view with custom controller in xml
> `app:show_timeout="0"
app:hide_on_touch="false"`
2. Pass the advertise player view to AdvertisePlayer constructor
3. Create advertise media item
> `playerHandler.addMediaAdvertise(media = mediaItem, skippTime = 10)`
4. Setup advertisePlayerHandler and set to player<br/>
5. Call playAdvertiseAutomatic for player<br/>
