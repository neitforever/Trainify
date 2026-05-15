package com.example.motivationcalendarapi.model

import com.example.motivationcalendarapi.R

sealed class Equipment(
    val iconResId: Int,
    val key: String,
    private val enName: String,
    private val ruName: String,
    private val beName: String
) {
    object Rope : Equipment(R.drawable.ic_equipment_rope, "rope","rope", "канат", "вяроўка")
    object Assisted : Equipment(R.drawable.ic_equipment_assisted, "assisted","assisted", "с поддержкой", "з падтрымкай")
    object BodyWeight : Equipment(R.drawable.ic_equipment_body_weight, "body weight","body weight", "собственный вес", "уласны вес")
    object Weighted : Equipment(R.drawable.ic_equipment_body_weight, "weighted","weighted", "утяжеленный", "утяжелены")
    object Hammer : Equipment(R.drawable.ic_equipment_hammer_machine, "hammer","hammer", "хамер", "хамер")
    object BosuBall : Equipment(R.drawable.ic_equipment_bosu_ball, "bosu ball","bosu ball", "мяч босу", "мяч босу")
    object SmithMachine : Equipment(R.drawable.ic_equipment_smith_machine, "smith machine","smith machine", "смита", "сміта")
    object MedicineBall : Equipment(R.drawable.ic_equipment_medicine_ball, "medicine ball","medicine ball", "медицинский мяч", "медыцынскі мяч")
    object StabilityBall : Equipment(R.drawable.ic_equipment_stability_ball, "stability ball","stability ball", "фитбол", "фітбол")
    object Kettlebell : Equipment(R.drawable.ic_equipment_kettlebell, "kettlebell","kettlebell", "гиря", "гіра")
    object Barbell : Equipment(R.drawable.ic_equipment_barbell, "barbell","barbell", "штанга", "штанга")
    object Dumbbell : Equipment(R.drawable.ic_equipment_dumbbell, "dumbbell","dumbbell", "гантель", "гантэль")
    object Cable : Equipment(R.drawable.ic_equipment_cable, "cable","cable", "кабель", "кабель")
    object SledMachine : Equipment(R.drawable.ic_equipment_sled_machine, "sled machine","sled machine", "тренажер слэд", "тренажер слэд")
    object Band : Equipment(R.drawable.ic_equipment_band, "band","band", "эспандер", "эспандэр")
    object EzBarbell : Equipment(R.drawable.ic_equipment_barbell_z, "ez barbell","ez barbell", "EZ-штанга", "EZ-штанга")
    object TrapBar : Equipment(R.drawable.ic_equipment_trap_bar, "trap bar","trap bar", "трап-гриф", "трап-грыф")
    object WheelRoller : Equipment(R.drawable.ic_equipment_wheel_roller, "wheel roller","wheel roller", "ролик", "коцік")
    object LeverageMachine : Equipment(R.drawable.ic_equipment_leverage_machine, "leverage machine","leverage machine", "рычажный тренажер", "рычагавы трэнажор")
    object OlympicBarbell : Equipment(R.drawable.ic_equipment_olympic_barbell, "olympic barbell","olympic barbell", "олимпийская штанга", "алімпійская штанга")
    object Unknown : Equipment(R.drawable.ic_dumbbell, "unknown","unknown", "неизвестно", "невядома")

    companion object {
        fun fromString(value: String): Equipment {
            return when (value.trim().lowercase()) {
               "rope", "канат", "вяроўка" -> Rope
                "assisted", "с поддержкой", "з падтрымкай" -> Assisted
                "body weight", "собственный вес", "уласны вес" -> BodyWeight
                "weighted", "утяжеленный", "утяжелены" -> Weighted
                "hammer", "хамер", "молат" -> Hammer
                "bosu ball", "мяч босу" -> BosuBall
                "smith machine", "смита", "сміта" -> SmithMachine
                "medicine ball", "медицинский мяч", "медыцынскі мяч" -> MedicineBall
                "stability ball", "фитбол", "фітбол" -> StabilityBall
                "kettlebell", "гиря", "гіра" -> Kettlebell
                "barbell", "штанга" -> Barbell
                "dumbbell", "гантель", "гантэль" -> Dumbbell
                "cable", "кабель" -> Cable
                "sled machine", "тренажер слэд" -> SledMachine
                "band", "эспандер", "эспандэр" -> Band
                "ez barbell", "ez-штанга" -> EzBarbell
                "trap bar", "трап-гриф", "трап-грыф" -> TrapBar
                "wheel roller", "ролик", "коцік" -> WheelRoller
                "leverage machine", "рычажный тренажер", "рычагавы трэнажор" -> LeverageMachine
                "olympic barbell", "олимпийская штанга", "алімпійская штанга" -> OlympicBarbell
                else -> Unknown
            }
        }
    }
}

fun getIconForEquipment(equipment: String): Int {
    return Equipment.fromString(equipment).iconResId
}