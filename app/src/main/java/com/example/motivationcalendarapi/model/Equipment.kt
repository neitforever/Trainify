package com.example.motivationcalendarapi.model


import com.example.motivationcalendarapi.R

sealed class Equipment(
    val displayName: String,
    val iconResId: Int
) {
    object Rope : Equipment("Rope", R.drawable.ic_equipment_rope)
    object Assisted : Equipment("Assisted", R.drawable.ic_equipment_assisted)
    object BodyWeight : Equipment("Body weight", R.drawable.ic_equipment_body_weight)
    object Weighted : Equipment("Weighted", R.drawable.ic_equipment_body_weight)
    object Hammer : Equipment("Hammer", R.drawable.ic_equipment_hammer_machine)
    object BosuBall : Equipment("Bosu ball", R.drawable.ic_equipment_bosu_ball)
    object SmithMachine : Equipment("Smith machine", R.drawable.ic_equipment_smith_machine)
    object MedicineBall : Equipment("Medicine ball", R.drawable.ic_equipment_medicine_ball)
    object StabilityBall : Equipment("Stability ball", R.drawable.ic_equipment_stability_ball)
    object Kettlebell : Equipment("Kettlebell", R.drawable.ic_equipment_kettlebell)
    object Barbell : Equipment("Barbell", R.drawable.ic_equipment_barbell)
    object Dumbbell : Equipment("Dumbbell", R.drawable.ic_equipment_dumbbell)
    object Cable : Equipment("Cable", R.drawable.ic_equipment_cable)
    object SledMachine : Equipment("Sled machine", R.drawable.ic_equipment_sled_machine)
    object Band : Equipment("Band", R.drawable.ic_equipment_band)
    object EzBarbell : Equipment("Ez barbell", R.drawable.ic_equipment_barbell_z)
    object TrapBar : Equipment("Trap bar", R.drawable.ic_equipment_trap_bar)
    object WheelRoller : Equipment("Wheel roller", R.drawable.ic_equipment_wheel_roller)
    object LeverageMachine : Equipment("Leverage machine", R.drawable.ic_equipment_leverage_machine)
    object OlympicBarbell : Equipment("Olympic barbell", R.drawable.ic_equipment_olympic_barbell)
    object Unknown : Equipment("Unknown", R.drawable.ic_dumbbell)

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