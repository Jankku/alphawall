package com.jankku.alphawall.util

sealed class Event {
    data class SearchGuide(val hideSearchGuide: Boolean) : Event()
}
