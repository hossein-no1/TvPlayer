fun mapToResolutionList(inputList: List<Int>): Map<Int, String> {
    val resolutions = listOf("144p", "240p", "360p", "480p", "720p", "1080p", "1440p", "2160p")
    val sortedList = inputList.sorted()

    val middleIndex = (resolutions.size - inputList.size) / 2
    val middleResolutions = resolutions.slice(middleIndex until middleIndex + inputList.size)

    return sortedList
        .mapIndexed { index, item -> item to (if (index < middleResolutions.size) middleResolutions[index] else "") }
        .filter { it.second.isNotBlank() }
        .toMap()
}
