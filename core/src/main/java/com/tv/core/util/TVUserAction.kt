package com.tv.core.util

enum class TVUserAction(val value: String) {
    SHOW_SOURCE("ShowSource"),
    SHOW_QUALITY("ShowQuality"),
    SHOW_AUDIO("ShowAudio"),
    SHOW_SUBTITLE("ShowSubtitle"),
    SHOW_EPISODE("ShowEpisode"),
    SELECT_SOURCE("SelectSource"),
    SELECT_QUALITY("SelectQuality"),
    SELECT_AUDIO("SelectAudio"),
    SELECT_SUBTITLE("SelectSubtitle"),
    SELECT_EPISODE("SelectEpisode"),
    INCREASE_SUBTITLE("IncreaseSubtitle"),
    REDUCE_SUBTITLE("ReduceSubtitle"),
    FAST_INCREMENT_COUNTER("FastIncrementCounter"),
    FAST_DECREMENT_COUNTER("FastDecrementCounter"),
    NEXT_MEDIA("NextMedia"),
    PREV_MEDIA("PrevMedia"),
    PLAY_MEDIA("PlayMedia"),
    PAUSE_MEDIA("PauseMedia"),
    FORWARD_MEDIA("ForwardMedia"),
    REWIND_MEDIA("RewindMedia"),
    SCRUB_MEDIA("ScrubMedia")
}