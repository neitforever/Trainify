package com.example.motivationcalendarapi.model

data class LocalizedOption(
    val key: String,
    val localized: Map<String, String>
) {
    fun getLabel(lang: String): String {
        return localized[lang] ?: localized["en"] ?: key
    }
}

data class LocalizedOptionGroup(
    val key: String,
    val title: Map<String, String>,
    val options: List<LocalizedOption>
) {
    fun getTitle(lang: String): String = title[lang] ?: title["en"] ?: key
}

object ExerciseCatalog {
    private fun bodyPartOption(bodyPart: BodyPart) = LocalizedOption(
        key = bodyPart.key,
        localized = bodyPart.toLocalizedMap()
    )

    private fun equipmentOption(equipment: Equipment) = LocalizedOption(
        key = equipment.key,
        localized = equipment.toLocalizedMap()
    )

    val bodyPartGroups: List<LocalizedOptionGroup> = listOf(
        LocalizedOptionGroup(
            key = "torso",
            title = mapOf("en" to "Torso", "ru" to "Корпус", "be" to "Корпус"),
            options = listOf(BodyPart.Chest, BodyPart.Pectorals, BodyPart.Back, BodyPart.UpperBack, BodyPart.Lats, BodyPart.Traps, BodyPart.Spine, BodyPart.Waist, BodyPart.Abs).map(::bodyPartOption)
        ),
        LocalizedOptionGroup(
            key = "arms_shoulders",
            title = mapOf("en" to "Arms and shoulders", "ru" to "Руки и плечи", "be" to "Рукі і плечы"),
            options = listOf(BodyPart.Shoulders, BodyPart.Delts, BodyPart.Neck, BodyPart.UpperArms, BodyPart.Biceps, BodyPart.Triceps, BodyPart.LowerArms, BodyPart.Forearms).map(::bodyPartOption)
        ),
        LocalizedOptionGroup(
            key = "legs_glutes",
            title = mapOf("en" to "Legs and glutes", "ru" to "Ноги и ягодицы", "be" to "Ногі і ягадзіцы"),
            options = listOf(BodyPart.UpperLegs, BodyPart.Quads, BodyPart.Hamstrings, BodyPart.Glutes, BodyPart.Abductors, BodyPart.Adductors, BodyPart.LowerLegs, BodyPart.Calves).map(::bodyPartOption)
        ),
        LocalizedOptionGroup(
            key = "cardio",
            title = mapOf("en" to "Cardio", "ru" to "Кардио", "be" to "Кардыё"),
            options = listOf(BodyPart.Cardio).map(::bodyPartOption)
        )
    )

    val equipmentGroups: List<LocalizedOptionGroup> = listOf(
        LocalizedOptionGroup(
            key = "body_weight",
            title = mapOf("en" to "Body weight", "ru" to "Собственный вес", "be" to "Уласны вес"),
            options = listOf(Equipment.BodyWeight, Equipment.Weighted, Equipment.Assisted).map(::equipmentOption)
        ),
        LocalizedOptionGroup(
            key = "free_weights",
            title = mapOf("en" to "Free weights", "ru" to "Свободные веса", "be" to "Свабодныя вагі"),
            options = listOf(Equipment.Dumbbell, Equipment.Kettlebell, Equipment.Barbell, Equipment.OlympicBarbell, Equipment.EzBarbell, Equipment.TrapBar, Equipment.MedicineBall).map(::equipmentOption)
        ),
        LocalizedOptionGroup(
            key = "cables_bands",
            title = mapOf("en" to "Cables and bands", "ru" to "Кроссоверы и ленты", "be" to "Кросоверы і стужкі"),
            options = listOf(Equipment.Cable, Equipment.Rope, Equipment.Band, Equipment.ResistanceBand).map(::equipmentOption)
        ),
        LocalizedOptionGroup(
            key = "strength_machines",
            title = mapOf("en" to "Strength machines", "ru" to "Силовое оборудование", "be" to "Сілавое абсталяванне"),
            options = listOf(Equipment.SmithMachine, Equipment.Hammer, Equipment.LeverageMachine, Equipment.PecDeckMachine, Equipment.SeatedRowMachine, Equipment.AbductorAdductorMachine, Equipment.LegPress, Equipment.LegCurlMachine, Equipment.LegExtensionMachine, Equipment.CalfMachine, Equipment.AbdominalCrunch, Equipment.HipThrustMachine).map(::equipmentOption)
        ),
        LocalizedOptionGroup(
            key = "balls_support",
            title = mapOf("en" to "Balls and support", "ru" to "Мячи и опоры", "be" to "Мячы і апоры"),
            options = listOf(Equipment.BosuBall, Equipment.StabilityBall, Equipment.WheelRoller, Equipment.Roller).map(::equipmentOption)
        ),
        LocalizedOptionGroup(
            key = "cardio",
            title = mapOf("en" to "Cardio machines", "ru" to "Кардиооборудование", "be" to "Кардыяабсталяванне"),
            options = listOf(Equipment.Treadmill, Equipment.Bike, Equipment.EllipticalMachine, Equipment.SkiergMachine, Equipment.StepmillMachine).map(::equipmentOption)
        ),
        LocalizedOptionGroup(
            key = "functional",
            title = mapOf("en" to "Functional training", "ru" to "Функциональный тренинг", "be" to "Функцыянальны трэнінг"),
            options = listOf(Equipment.Tire).map(::equipmentOption)
        )
    )

    val bodyParts: List<LocalizedOption> = bodyPartGroups.flatMap { it.options }
    val equipment: List<LocalizedOption> = equipmentGroups.flatMap { it.options }

    fun groupedBodyPartLabels(lang: String): List<Pair<String, List<String>>> = bodyPartGroups.map { group ->
        group.getTitle(lang) to group.options.map { it.getLabel(lang) }
    }

    fun groupedEquipmentLabels(lang: String): List<Pair<String, List<String>>> = equipmentGroups.map { group ->
        group.getTitle(lang) to group.options.map { it.getLabel(lang) }
    }
}
