package com.tv.core.util

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters

fun DefaultTrackSelector.generateQualityList(): ArrayList<Pair<String, TrackSelectionParameters.Builder>> {
    val trackOverrideList = ArrayList<Pair<String, TrackSelectionParameters.Builder>>()

    val renderTrack = this.currentMappedTrackInfo
    val renderCount = renderTrack?.rendererCount ?: 0
    for (rendererIndex in 0 until renderCount) {
        if (isSupportedFormat(renderTrack, rendererIndex)) {
            val trackGroupType = renderTrack?.getRendererType(rendererIndex)
            val trackGroups = renderTrack?.getTrackGroups(rendererIndex)
            val trackGroupsCount = trackGroups?.length!!
            if (trackGroupType == C.TRACK_TYPE_VIDEO) {
                for (groupIndex in 0 until trackGroupsCount) {
                    val videoQualityTrackCount = trackGroups[groupIndex].length
                    for (trackIndex in 0 until videoQualityTrackCount) {
                        val isTrackSupported = renderTrack.getTrackSupport(
                            rendererIndex,
                            groupIndex,
                            trackIndex
                        ) == C.FORMAT_HANDLED
                        if (isTrackSupported) {
                            val track = trackGroups[groupIndex]
                            val trackName =
                                "${track.getFormat(trackIndex)} x ${track.getFormat(trackIndex).height}"
                            if (track.getFormat(trackIndex).selectionFlags == C.SELECTION_FLAG_AUTOSELECT) {
                                trackName.plus(" (Default)")
                            }
                            val trackBuilder =
                                TrackSelectionParameters.Builder(requireNotNull(context))
                                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                    .addOverride(
                                        TrackSelectionOverride(
                                            track,
                                            listOf(trackIndex)
                                        )
                                    )
                            trackOverrideList.add(Pair(trackName, trackBuilder))
                        }
                    }
                }
            }
        }
    }
    return trackOverrideList
}

fun isSupportedFormat(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo?,
    rendererIndex: Int
): Boolean {
    val trackGroupArray = mappedTrackInfo?.getTrackGroups(rendererIndex)
    return if (trackGroupArray?.length == 0) {
        false
    } else mappedTrackInfo?.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO || mappedTrackInfo?.getRendererType(
        rendererIndex
    ) == C.TRACK_TYPE_AUDIO || mappedTrackInfo?.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT
}