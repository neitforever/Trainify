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
        val all: List<BodyPart> = listOfNotNull(
            Waist, Back, Chest, UpperLegs, UpperArms, Shoulders, LowerArms, LowerLegs, Cardio, Neck,
            Abductors, Abs, Adductors, Biceps, Calves, Delts, Forearms,
            Glutes, Hamstrings, Lats, Pectorals, Quads, Spine, Traps, Triceps, UpperBack
        )

        private fun normalize(value: String?): String {
            return value.orEmpty()
                .trim()
                .lowercase()
                .replace('ё', 'е')
                .replace('ў', 'у')
                .replace(Regex("[\\s_\\-/]+"), " ")
                .replace(Regex("[^a-zа-я0-9 ]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        fun fromString(value: String?): BodyPart {
            val normalized = normalize(value)
            if (normalized.isBlank()) return Unknown

            return all.firstOrNull { bodyPart ->
                normalized == normalize(bodyPart.key) ||
                        bodyPart.toLocalizedMap().values.any { localizedName ->
                            normalized == normalize(localizedName)
                        }
            } ?: when (normalized) {
                "core", "waist", "талия", "талія" -> Waist
                "abdominal", "abdominals", "abs", "abdominal muscles", "пресс", "мышцы пресса", "прэс" -> Abs
                "back", "спина", "спіна" -> Back
                "upper back", "верх спины", "верх спіны" -> UpperBack
                "lat", "lats", "latissimus dorsi", "широчаишие", "широчайшие", "наишырэишыя мышцы спіны", "найшырэйшыя мышцы спіны" -> Lats
                "trap", "traps", "trapezius", "trapezius muscle", "трапеции", "трапециевидные", "трапецыі" -> Traps
                "spinal erectors", "erector spinae", "spine", "разгибатели позвоночника", "разгінальнікі пазваночніка" -> Spine
                "chest", "pectorals", "pecs", "pec", "грудь", "грудные мышцы", "грудзі", "грудныя мышцы" -> Pectorals

                "shoulder", "shoulders", "delts", "delt", "deltoids", "deltoid", "плечи", "дельты", "плячы", "дэльты" -> Shoulders
                "neck", "шея", "шыя" -> Neck
                "arm", "arms", "upper arms", "верхние части рук", "верхнія часткі рук" -> UpperArms
                "bicep", "biceps", "бицепс", "біцэпс" -> Biceps
                "tricep", "triceps", "трицепс", "трыцэпс" -> Triceps
                "forearm", "forearms", "lower arms", "предплечья", "нижние части рук", "перадплеччы", "ніжнія часткі рук" -> Forearms

                "leg", "legs", "upper legs", "thighs", "верхние части ног", "верхнія часткі ног" -> UpperLegs
                "quad", "quads", "quadriceps", "quadricep", "квадрицепс", "квадрицепсы", "квадрыцэпсы" -> Quads
                "hamstring", "hamstrings", "бицепс бедра", "біцэпс сцягна" -> Hamstrings
                "glute", "glutes", "gluteus", "gluteus maximus", "ягодицы", "ягодичные", "ягадзіцы" -> Glutes
                "abductor", "abductors", "отводящие мышцы бедра", "адводзячыя мышцы сцягна" -> Abductors
                "adductor", "adductors", "приводящие мышцы бедра", "прыводзячыя мышцы сцягна" -> Adductors
                "calf", "calves", "lower legs", "икры", "икроножные", "ікры", "нижние части ног", "ніжнія часткі ног" -> Calves
                "cardio", "кардио", "кардые", "кардыё" -> Cardio
                else -> Unknown
            }
        }
    }
}

fun getIconForBodyPart(bodyPart: String): Int {
    return BodyPart.fromString(bodyPart).iconResId
}