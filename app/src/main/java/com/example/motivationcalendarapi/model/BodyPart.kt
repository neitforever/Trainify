package com.example.motivationcalendarapi.model

import com.example.motivationcalendarapi.R

sealed class BodyPart(
    val iconResId: Int,
    private val enName: String,
    private val ruName: String,
    private val beName: String
) {
    object Waist : BodyPart(R.drawable.ic_body_waist, "waist", "талия", "талія")
    object Back : BodyPart(R.drawable.ic_body_back, "back", "спина", "спина")
    object Chest : BodyPart(R.drawable.ic_body_chest, "chest", "груди", "грудзі")
    object UpperLegs : BodyPart(R.drawable.ic_body_upper_legs, "upper legs", "верхние части ног", "верхнія часткі ног")
    object UpperArms : BodyPart(R.drawable.ic_body_upper_arms, "upper arms", "верхние части рук", "верхнія часткі рук")
    object Shoulders : BodyPart(R.drawable.ic_body_shoulders, "shoulders", "плечи", "плячы")
    object LowerArms : BodyPart(R.drawable.ic_body_lower_arms, "lower arms", "нижние части рук", "ніжнія часткі рук")
    object LowerLegs : BodyPart(R.drawable.ic_body_lower_legs, "lower legs", "нижние части ног", "ніжнія часткі ног")
    object Cardio : BodyPart(R.drawable.ic_body_cardio, "cardio", "кардио", "кардыё")
    object Neck : BodyPart(R.drawable.ic_body_neck, "neck", "шея", "шыя")
    object Unknown : BodyPart(R.drawable.ic_dumbbell, "unknown", "неизвестно", "невядома")

    companion object {
        fun fromString(value: String): BodyPart {
            return when (value.trim().lowercase()) {
                "waist", "талия", "талія" -> Waist
                "back", "спина" -> Back
                "chest", "груди", "грудзі" -> Chest
                "upper legs", "верхние части ног", "верхнія часткі ног" -> UpperLegs
                "upper arms", "верхние части рук", "верхнія часткі рук" -> UpperArms
                "shoulders", "плечи", "плячы" -> Shoulders
                "lower arms", "нижние части рук", "ніжнія часткі рук" -> LowerArms
                "lower legs", "нижние части ног", "ніжнія часткі ног" -> LowerLegs
                "cardio", "кардио", "кардыё" -> Cardio
                "neck", "шея", "шыя" -> Neck
                else -> Unknown
            }
        }
    }
}
fun getIconForBodyPart(bodyPart: String): Int {
    return BodyPart.fromString(bodyPart).iconResId
}