package com.example.motivationcalendarapi.ui.exercise.ai

import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Equipment

fun isAiChooseOption(option: String, aiChooseLabel: String): Boolean {
    val normalizedOption = option.trim().lowercase()
    val normalizedLabel = aiChooseLabel.trim().lowercase()
    return normalizedOption == normalizedLabel || normalizedOption == "let ai choose"
}

fun safeEquipmentIcon(option: String, aiChooseLabel: String = "Let AI choose"): Int {
    val value = option.trim()
    if (isAiChooseOption(value, aiChooseLabel) || value.isBlank()) return R.drawable.ic_info

    return Equipment.fromCatalogText(value)?.iconResId
        ?: runCatching { Equipment.fromString(value).iconResId }.getOrDefault(Equipment.Unknown.iconResId)
}
