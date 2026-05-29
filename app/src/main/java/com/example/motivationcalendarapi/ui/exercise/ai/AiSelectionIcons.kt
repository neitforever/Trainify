package com.example.motivationcalendarapi.ui.exercise.ai

import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.getIconForEquipment

fun isAiChooseOption(option: String, aiChooseLabel: String): Boolean {
    val normalizedOption = option.trim().lowercase()
    val normalizedLabel = aiChooseLabel.trim().lowercase()
    return normalizedOption == normalizedLabel || normalizedOption == "let ai choose"
}

fun safeEquipmentIcon(option: String, aiChooseLabel: String = "Let AI choose"): Int {
    if (isAiChooseOption(option, aiChooseLabel) || option.isBlank()) return R.drawable.ic_info
    return runCatching { getIconForEquipment(option) }.getOrDefault(R.drawable.ic_info)
}
