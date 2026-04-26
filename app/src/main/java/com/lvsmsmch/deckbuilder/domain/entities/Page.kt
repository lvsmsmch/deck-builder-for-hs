package com.lvsmsmch.deckbuilder.domain.entities

data class Page<T>(
    val items: List<T>,
    val pageNumber: Int,
    val pageCount: Int,
    val totalCount: Int,
) {
    val hasNext: Boolean get() = pageNumber < pageCount
}
