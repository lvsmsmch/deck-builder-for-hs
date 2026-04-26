package com.lvsmsmch.deckbuilder.data.network.mapper

import com.lvsmsmch.deckbuilder.data.network.dto.CardBackDto
import com.lvsmsmch.deckbuilder.domain.entities.CardBack

fun CardBackDto.toDomain(): CardBack = CardBack(
    id = id,
    slug = slug,
    name = name,
    text = text?.takeUnless { it.isBlank() },
    image = image,
    sortCategory = sortCategory?.takeUnless { it.isBlank() },
    enabled = enabled == 1,
)
