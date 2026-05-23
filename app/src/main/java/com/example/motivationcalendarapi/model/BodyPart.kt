package com.example.motivationcalendarapi.model

import com.example.motivationcalendarapi.R

sealed class BodyPart(
    val iconResId: Int,
    val key: String,
    private val enName: String,
    private val ruName: String,
    private val beName: String
) {
    object Waist : BodyPart(R.drawable.ic_body_waist, "waist", "waist", "талия", "талія")
    object Back : BodyPart(R.drawable.ic_body_back, "back", "back", "спина", "спіна")
    object Chest : BodyPart(R.drawable.ic_body_chest, "chest", "chest", "грудь", "грудзі")
    object UpperLegs : BodyPart(R.drawable.ic_body_upper_legs, "upper legs", "upper legs", "верхние части ног", "верхнія часткі ног")
    object UpperArms : BodyPart(R.drawable.ic_body_upper_arms, "upper arms", "upper arms", "верхние части рук", "верхнія часткі рук")
    object Shoulders : BodyPart(R.drawable.ic_body_shoulders, "shoulders", "shoulders", "плечи", "плячы")
    object LowerArms : BodyPart(R.drawable.ic_body_lower_arms, "lower arms", "lower arms", "нижние части рук", "ніжнія часткі рук")
    object LowerLegs : BodyPart(R.drawable.ic_body_lower_legs, "lower legs", "lower legs", "нижние части ног", "ніжнія часткі ног")
    object Cardio : BodyPart(R.drawable.ic_body_cardio, "cardio", "cardio", "кардио", "кардыё")
    object Neck : BodyPart(R.drawable.ic_body_neck, "neck", "neck", "шея", "шыя")
    object Abductors : BodyPart(R.drawable.ic_body_upper_legs, "abductors", "abductors", "отводящие мышцы бедра", "адводзячыя мышцы сцягна")
    object Abs : BodyPart(R.drawable.ic_body_waist, "abs", "abs", "пресс", "прэс")
    object Adductors : BodyPart(R.drawable.ic_body_upper_legs, "adductors", "adductors", "приводящие мышцы бедра", "прыводзячыя мышцы сцягна")
    object Biceps : BodyPart(R.drawable.ic_body_upper_arms, "biceps", "biceps", "бицепс", "біцэпс")
    object Calves : BodyPart(R.drawable.ic_body_lower_legs, "calves", "calves", "икры", "ікры")
    object CardiovascularSystem : BodyPart(R.drawable.ic_body_cardio, "cardiovascular system", "cardiovascular system", "сердечно-сосудистая система", "сардэчна-сасудзістая сістэма")
    object Delts : BodyPart(R.drawable.ic_body_shoulders, "delts", "delts", "дельты", "дэльты")
    object Forearms : BodyPart(R.drawable.ic_body_upper_arms, "forearms", "forearms", "предплечья", "перадплеччы")
    object Glutes : BodyPart(R.drawable.ic_body_upper_legs, "glutes", "glutes", "ягодицы", "ягадзіцы")
    object Hamstrings : BodyPart(R.drawable.ic_body_upper_legs, "hamstrings", "hamstrings", "бицепс бедра", "біцэпс сцягна")
    object Lats : BodyPart(R.drawable.ic_body_back, "lats", "lats", "широчайшие мышцы спины", "найшырэйшыя мышцы спіны")
    object Pectorals : BodyPart(R.drawable.ic_body_chest, "pectorals", "pectorals", "грудные мышцы", "грудныя мышцы")
    object Quads : BodyPart(R.drawable.ic_body_upper_legs, "quads", "quads", "квадрицепсы", "квадрыцэпсы")
    object Spine : BodyPart(R.drawable.ic_body_back, "spine", "spine", "разгибатели позвоночника", "разгінальнікі пазваночніка")
    object Traps : BodyPart(R.drawable.ic_body_back, "traps", "traps", "трапеции", "трапецыі")
    object Triceps : BodyPart(R.drawable.ic_body_lower_arms, "triceps", "triceps", "трицепс", "трыцэпс")
    object UpperBack : BodyPart(R.drawable.ic_body_back, "upper back", "upper back", "верх спины", "верх спіны")

    object Unknown : BodyPart(R.drawable.ic_dumbbell, "unknown", "unknown", "неизвестно", "невядома")

    fun getLabel(lang: String): String = when (lang) {
        "ru" -> ruName
        "be" -> beName
        else -> enName
    }

    fun toLocalizedMap(): Map<String, String> = mapOf(
        "en" to enName,
        "ru" to ruName,
        "be" to beName
    )

    companion object {
        val all: List<BodyPart> = listOf(
            Waist, Back, Chest, UpperLegs, UpperArms, Shoulders, LowerArms, LowerLegs, Cardio, Neck,
            Abductors, Abs, Adductors, Biceps, Calves, CardiovascularSystem, Delts, Forearms,
            Glutes, Hamstrings, Lats, Pectorals, Quads, Spine, Traps, Triceps, UpperBack
        )

        fun fromString(value: String): BodyPart {
            val normalized = value.trim().lowercase()
            return all.firstOrNull { bodyPart ->
                normalized == bodyPart.key || bodyPart.toLocalizedMap().values.any { it.lowercase() == normalized }
            } ?: when (normalized) {
                "abdominals", "пресс", "мышцы пресса" -> Abs
                "bicep", "бицепс", "біцэпс" -> Biceps
                "tricep", "трицепс", "трыцэпс" -> Triceps
                "quadriceps", "квадрицепс" -> Quads
                "latissimus dorsi", "широчайшие" -> Lats
                "trapezius", "трапециевидные" -> Traps
                "calf", "икроножные" -> Calves
                "gluteus", "ягодичные" -> Glutes
                else -> Unknown
            }
        }
    }
}

fun getIconForBodyPart(bodyPart: String): Int {
    return BodyPart.fromString(bodyPart).iconResId
}