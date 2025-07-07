package com.example.motivationcalendarapi.model

import com.example.motivationcalendarapi.R

sealed class BodyPart(
    val iconResId: Int
) {
    object Waist : BodyPart(R.drawable.ic_body_waist)
    object Back : BodyPart(R.drawable.ic_body_back)
    object Chest : BodyPart(R.drawable.ic_body_chest)
    object UpperLegs : BodyPart(R.drawable.ic_body_upper_legs)
    object UpperArms : BodyPart(R.drawable.ic_body_upper_arms)
    object Shoulders : BodyPart(R.drawable.ic_body_shoulders)
    object LowerArms : BodyPart(R.drawable.ic_body_lower_arms)
    object LowerLegs : BodyPart(R.drawable.ic_body_lower_legs)
    object Cardio : BodyPart(R.drawable.ic_body_cardio)
    object Neck : BodyPart(R.drawable.ic_body_neck)
    object Unknown : BodyPart(R.drawable.ic_dumbbell)

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