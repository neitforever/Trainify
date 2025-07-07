package com.example.motivationcalendarapi.model


import com.example.motivationcalendarapi.R

sealed class Equipment(
    val iconResId: Int
) {
    object Rope : Equipment(R.drawable.ic_equipment_rope)
    object Assisted : Equipment(R.drawable.ic_equipment_assisted)
    object BodyWeight : Equipment(R.drawable.ic_equipment_body_weight)
    object Weighted : Equipment(R.drawable.ic_equipment_body_weight)
    object Hammer : Equipment(R.drawable.ic_equipment_hammer_machine)
    object BosuBall : Equipment(R.drawable.ic_equipment_bosu_ball)
    object SmithMachine : Equipment(R.drawable.ic_equipment_smith_machine)
    object MedicineBall : Equipment(R.drawable.ic_equipment_medicine_ball)
    object StabilityBall : Equipment(R.drawable.ic_equipment_stability_ball)
    object Kettlebell : Equipment(R.drawable.ic_equipment_kettlebell)
    object Barbell : Equipment(R.drawable.ic_equipment_barbell)
    object Dumbbell : Equipment(R.drawable.ic_equipment_dumbbell)
    object Cable : Equipment(R.drawable.ic_equipment_cable)
    object SledMachine : Equipment(R.drawable.ic_equipment_sled_machine)
    object Band : Equipment(R.drawable.ic_equipment_band)
    object EzBarbell : Equipment(R.drawable.ic_equipment_barbell_z)
    object TrapBar : Equipment(R.drawable.ic_equipment_trap_bar)
    object WheelRoller : Equipment(R.drawable.ic_equipment_wheel_roller)
    object LeverageMachine : Equipment(R.drawable.ic_equipment_leverage_machine)
    object OlympicBarbell : Equipment(R.drawable.ic_equipment_olympic_barbell)
    object Unknown : Equipment(R.drawable.ic_dumbbell)

    companion object {
        fun fromString(value: String): Equipment {
            return when (value.trim().lowercase()) {
                "rope" -> Rope
                "assisted" -> Assisted
                "body weight" -> BodyWeight
                "weighted" -> Weighted
                "hammer" -> Hammer
                "bosu ball" -> BosuBall
                "smith machine" -> SmithMachine
                "medicine ball" -> MedicineBall
                "stability ball" -> StabilityBall
                "kettlebell" -> Kettlebell
                "barbell" -> Barbell
                "dumbbell" -> Dumbbell
                "cable" -> Cable
                "sled machine" -> SledMachine
                "band" -> Band
                "ez barbell" -> EzBarbell
                "trap bar" -> TrapBar
                "wheel roller" -> WheelRoller
                "leverage machine" -> LeverageMachine
                "olympic barbell" -> OlympicBarbell
                else -> Unknown
            }
        }
    }
}

fun getIconForEquipment(equipment: String): Int {
    return Equipment.fromString(equipment).iconResId
}