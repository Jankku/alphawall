package com.jankku.alphawall.database.model

enum class SortStatus(val value: String) {
    NEWEST("newest"),
    RATING("rating"),
    VIEWS("views"),
    FAVORITES("favorites");

    companion object {
        fun toArray(): Array<String> = values().map { it ->
            it.value.replaceFirstChar { it.uppercase() }
        }.toTypedArray()
    }
}
