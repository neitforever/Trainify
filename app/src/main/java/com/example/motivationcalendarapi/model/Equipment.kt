package com.example.motivationcalendarapi.model

import com.example.motivationcalendarapi.R

sealed class Equipment(
    val iconResId: Int,
    val key: String,
    private val enName: String,
    private val ruName: String,
    private val beName: String
) {
    object Rope : Equipment(R.drawable.ic_equipment_rope, "rope", "rope", "канат", "вяроўка")
    object Assisted : Equipment(R.drawable.ic_equipment_assisted, "assisted", "assisted", "с поддержкой", "з падтрымкай")
    object BodyWeight : Equipment(R.drawable.ic_equipment_body_weight, "body weight", "body weight", "собственный вес", "уласны вес")
    object Weighted : Equipment(R.drawable.ic_equipment_body_weight, "weighted", "weighted", "утяжеленный", "утяжараны")
    object Hammer : Equipment(R.drawable.ic_equipment_hammer_machine, "hammer", "hammer", "хамер", "хамер")
    object BosuBall : Equipment(R.drawable.ic_equipment_bosu_ball, "bosu ball", "bosu ball", "мяч босу", "мяч босу")
    object SmithMachine : Equipment(R.drawable.ic_equipment_smith_machine, "smith machine", "smith machine", "Смита", "Сміта")
    object MedicineBall : Equipment(R.drawable.ic_equipment_medicine_ball, "medicine ball", "medicine ball", "медицинский мяч", "медыцынскі мяч")
    object StabilityBall : Equipment(R.drawable.ic_equipment_stability_ball, "stability ball", "stability ball", "фитбол", "фітбол")
    object Kettlebell : Equipment(R.drawable.ic_equipment_kettlebell, "kettlebell", "kettlebell", "гиря", "гіра")
    object Barbell : Equipment(R.drawable.ic_equipment_barbell, "barbell", "barbell", "штанга", "штанга")
    object Dumbbell : Equipment(R.drawable.ic_equipment_dumbbell, "dumbbell", "dumbbell", "гантель", "гантэль")
    object Cable : Equipment(R.drawable.ic_equipment_cable, "cable", "cable", "кроссовер", "кросовер")
    object LegPress : Equipment(R.drawable.ic_equipment_sled_machine, "leg press", "leg press", "жим ногами", "жым нагамі")
    object Band : Equipment(R.drawable.ic_equipment_band, "band", "band", "эспандер", "эспандэр")
    object EzBarbell : Equipment(R.drawable.ic_equipment_barbell_z, "ez barbell", "ez barbell", "EZ-штанга", "EZ-штанга")
    object TrapBar : Equipment(R.drawable.ic_equipment_trap_bar, "trap bar", "trap bar", "трап-гриф", "трап-грыф")
    object WheelRoller : Equipment(R.drawable.ic_equipment_wheel_roller, "wheel roller", "wheel roller", "ролик для пресса", "ролік для прэса")
    object LeverageMachine : Equipment(R.drawable.ic_equipment_leverage_machine, "leverage machine", "leverage machine", "рычажный", "рычагавы")
    object PecDeckMachine : Equipment(R.drawable.ic_equipment_pec_deck_machine, "pec deck machine", "pec deck machine", "бабочка", "матылёк")
    object SeatedRowMachine : Equipment(R.drawable.ic_equipment_seated_row_machine, "seated row machine", "seated row machine", "тяга на спину", "цяга на спіну")
    object AbductorAdductorMachine : Equipment(R.drawable.ic_equipment_pec_deck_machine, "abductor/adductor machine", "abductor/adductor machine", "отведение и приведение бедра", "адвядзенне і прывядзенне сцягна")
    object LegCurlMachine : Equipment(R.drawable.ic_equipment_leg_curl, "leg curl machine", "leg curl machine", "сгибание ног", "згінанне ног")
    object LegExtensionMachine : Equipment(R.drawable.ic_equipment_leg_curl, "leg extension machine", "leg extension machine", "разгибание ног", "разгінанне ног")
    object CalfMachine : Equipment(R.drawable.ic_equipment_calf_machine, "calf machine", "calf machine", "икры", "лыткі")
    object AbdominalCrunch : Equipment(R.drawable.ic_equipment_abdominal_crunch, "abdominal_crunch", "abdominal crunch", "пресс", "прэс")
    object HipThrustMachine : Equipment(R.drawable.ic_equipment_bench, "hip thrust machine", "hip thrust machine", "ягодичный мост", "ягадзічны мост")
    object OlympicBarbell : Equipment(R.drawable.ic_equipment_olympic_barbell, "olympic barbell", "olympic barbell", "олимпийская штанга", "алімпійская штанга")
    object Treadmill : Equipment(R.drawable.ic_card_treadmill, "treadmill", "treadmill", "беговая дорожка", "бегавая дарожка")
    object Bike : Equipment(R.drawable.ic_card_bike, "bike", "bike", "велосипед", "веласіпед")
    object EllipticalMachine : Equipment(R.drawable.ic_equipment_elliptical_machine, "elliptical machine", "elliptical machine", "эллиптический", "эліптычны")
    object Roller : Equipment(R.drawable.ic_equipment_wheel_roller, "roller", "roller", "ролик", "ролік")
    object SkiergMachine : Equipment(R.drawable.ic_equipment_skierg_machine, "skierg machine", "skierg machine", "лыжный", "лыжны")
    object StepmillMachine : Equipment(R.drawable.ic_equipment_stepmill_machine, "stepmill machine", "stepmill machine", "степпер", "стэпер")
    object Tire : Equipment(R.drawable.ic_equipment_tire, "tire", "tire", "покрышка", "пакрышка")
    object ResistanceBand : Equipment(R.drawable.ic_equipment_band, "resistance band", "resistance band", "резиновая лента", "рэзінавая стужка")

    object Unknown : Equipment(R.drawable.ic_dumbbell, "unknown", "unknown", "неизвестно", "невядома")

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
        val all: List<Equipment> = listOf(
            Rope, Assisted, BodyWeight, Weighted, Hammer, BosuBall, SmithMachine,
            MedicineBall, StabilityBall, Kettlebell, Barbell, Dumbbell, Cable,
            LegPress, Band, EzBarbell, TrapBar, WheelRoller, LeverageMachine,
            PecDeckMachine, SeatedRowMachine, AbductorAdductorMachine, LegCurlMachine,
            LegExtensionMachine, CalfMachine, AbdominalCrunch, HipThrustMachine,
            OlympicBarbell, Treadmill, Bike, EllipticalMachine, Roller, SkiergMachine,
            StepmillMachine, Tire, ResistanceBand
        )

        fun fromString(value: String): Equipment {
            val normalized = value.trim().lowercase()
            return all.firstOrNull { equipment ->
                normalized == equipment.key || equipment.toLocalizedMap().values.any { it.lowercase() == normalized }
            } ?: when (normalized) {
                "exercise bike", "bicycle", "cycling", "велосипед", "стационарный велосипед", "велотренажёр", "велотренажер" -> Bike
                "running machine", "беговая", "дорожка" -> Treadmill
                "ab wheel", "ab roller", "ролик для пресса" -> WheelRoller
                "elastic band", "rubber band", "резинка", "резиновая лента" -> ResistanceBand
                "elliptical", "орбитрек" -> EllipticalMachine
                "ski erg", "skierg" -> SkiergMachine
                "stepper", "stair climber", "stair mill" -> StepmillMachine
                "sled", "sled machine", "leg press machine", "жим ногами", "тренажер sled", "тренажёр sled" -> LegPress
                "butterfly", "butterfly machine", "pec deck", "chest fly machine", "тренажер бабочка", "тренажёр бабочка", "бабочка" -> PecDeckMachine
                "row machine", "back row machine", "hammer row", "hammer strength row", "seated cable row", "тяга на спину", "тренажер для тяги", "тренажер для тяги на спину" -> SeatedRowMachine
                "abductor machine", "adductor machine", "abductor adductor machine", "hip abductor", "hip adductor", "тренажер для отведения бедра", "тренажер для приведения бедра" -> AbductorAdductorMachine
                "leg curling", "leg curl", "lying leg curl", "seated leg curl", "сгибание ног", "тренажер для сгибания ног" -> LegCurlMachine
                "leg extension", "leg extensions", "разгибание ног", "тренажер для разгибания ног" -> LegExtensionMachine
                "calf raise machine", "standing calf raise", "seated calf raise", "тренажер для икр", "икры" -> CalfMachine
                "abdominal_crunch", "abdominal crunch", "ab crunch", "ab crunch machine", "crunch machine", "тренажер для пресса", "тренажёр для пресса", "пресс машина" -> AbdominalCrunch
                "hip trast", "hip thrust", "hip thrusts", "hip thrust machine", "glute bridge machine", "тренажер для ягодичного моста", "ягодичный мост" -> HipThrustMachine
                else -> Unknown
            }
        }
    }
}

fun getIconForEquipment(equipment: String): Int {
    if (equipment.isBlank()) return Equipment.Unknown.iconResId
    return runCatching { Equipment.fromString(equipment).iconResId }
        .getOrDefault(Equipment.Unknown.iconResId)
}