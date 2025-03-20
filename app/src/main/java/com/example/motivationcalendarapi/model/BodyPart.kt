package com.example.motivationcalendarapi.model

import com.example.motivationcalendarapi.R

sealed class BodyPart(
    val displayName: String,
    val iconResId: Int
) {
    object Waist : BodyPart("waist", R.drawable.ic_body_waist)
    object Back : BodyPart("back", R.drawable.ic_body_back)
    object Chest : BodyPart("chest", R.drawable.ic_body_chest)
    object UpperLegs : BodyPart("upper legs", R.drawable.ic_body_upper_legs)
    object UpperArms : BodyPart("upper arms", R.drawable.ic_body_upper_arms)
    object Shoulders : BodyPart("shoulders", R.drawable.ic_body_shoulders)
    object LowerArms : BodyPart("lower arms", R.drawable.ic_body_lower_arms)
    object LowerLegs : BodyPart("lower legs", R.drawable.ic_body_lower_legs)
    object Cardio : BodyPart("cardio", R.drawable.ic_body_cardio)
    object Neck : BodyPart("neck", R.drawable.ic_body_neck)
    object Unknown : BodyPart("unknown", R.drawable.ic_dumbbell)

    companion object {
        fun fromString(value: String): BodyPart {
            return when (value.trim().lowercase()) {
                "waist" -> Waist
                "back" -> Back
                "chest" -> Chest
                "upper legs" -> UpperLegs
                "upper arms" -> UpperArms
                "shoulders" -> Shoulders
                "lower arms" -> LowerArms
                "lower legs" -> LowerLegs
                "cardio" -> Cardio
                "neck" -> Neck
                else -> Unknown
            }
        }
    }
}

fun getIconForBodyPart(bodyPart: String): Int {
    return BodyPart.fromString(bodyPart).iconResId
}