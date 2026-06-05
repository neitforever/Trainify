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
        val all: List<Equipment> by lazy(LazyThreadSafetyMode.NONE) {
            listOf(
            Rope, Assisted, BodyWeight, Weighted, Hammer, BosuBall, SmithMachine,
            MedicineBall, StabilityBall, Kettlebell, Barbell, Dumbbell, Cable,
            LegPress, Band, EzBarbell, TrapBar, WheelRoller, LeverageMachine,
            PecDeckMachine, SeatedRowMachine, AbductorAdductorMachine, LegCurlMachine,
            LegExtensionMachine, CalfMachine, AbdominalCrunch, HipThrustMachine,
            OlympicBarbell, Treadmill, Bike, EllipticalMachine, Roller, SkiergMachine,
                StepmillMachine, Tire, ResistanceBand
            ).filterNotNull()
        }

        private fun normalize(value: String?): String {
            return value.orEmpty()
                .trim()
                .lowercase()
                .replace('ё', 'е')
                .replace('ў', 'у')
                .replace('й', 'и')
                .replace(Regex("[\\s_\\-/\\.\\(\\)]+"), " ")
                .replace(Regex("[^a-zа-я0-9 ]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun compact(value: String): String = normalize(value).replace(" ", "")

        private fun inputCandidates(value: String): Set<String> {
            val normalized = normalize(value)
            if (normalized.isBlank()) return emptySet()

            val raw = value.orEmpty()
            val parts = raw
                .replace("{", " ")
                .replace("}", " ")
                .split(',', ';', '|', ':', '=', '\n', '\t')
                .map(::normalize)
                .filter { it.isNotBlank() }

            return buildSet {
                add(normalized)
                add(compact(normalized))
                parts.forEach { part ->
                    add(part)
                    add(compact(part))
                }
            }
        }

        private fun aliasesFor(equipment: Equipment): Set<String> {
            val baseAliases = buildSet {
                add(equipment.key)
                addAll(equipment.toLocalizedMap().values)
                when (equipment) {
                    BodyWeight -> addAll(listOf("bodyweight", "body weight", "body_weight", "body-weight", "own body weight", "no equipment", "without equipment", "собственныи вес", "собственный вес", "без оборудования", "без инвентаря", "уласны вес"))
                    Weighted -> addAll(listOf("weighted", "additional weight", "with weight", "с весом", "утяжеленныи", "утяжеленный", "утяжараны"))
                    Assisted -> addAll(listOf("assisted", "with assistance", "assisted machine", "с поддержкои", "с поддержкой", "з падтрымкаи", "з падтрымкай"))
                    Bike -> addAll(listOf("bike", "exercise bike", "stationary bike", "bicycle", "cycling", "велосипед", "велотренажер", "стационарныи велосипед", "стационарный велосипед"))
                    Treadmill -> addAll(listOf("treadmill", "tread mill", "running machine", "беговая дорожка", "беговая", "дорожка"))
                    WheelRoller -> addAll(listOf("wheel roller", "ab wheel", "ab roller", "roller wheel", "ролик для пресса", "ролик"))
                    Roller -> addAll(listOf("roller", "foam roller", "ролик", "массажныи ролик", "массажный ролик"))
                    Band, ResistanceBand -> addAll(listOf("band", "resistance band", "elastic band", "rubber band", "резинка", "резиновая лента", "эспандер", "эспандэр"))
                    EllipticalMachine -> addAll(listOf("elliptical", "elliptical machine", "elliptical trainer", "orbitrek", "орбитрек", "эллиптическии", "эллиптический"))
                    SkiergMachine -> addAll(listOf("ski erg", "skierg", "skierg machine", "ski erg machine", "лыжныи", "лыжный", "лыжныи тренажер", "лыжный тренажер"))
                    StepmillMachine -> addAll(listOf("stepmill", "step mill", "stepmill machine", "stepper", "stair climber", "stair mill", "степпер"))
                    LegPress -> addAll(listOf("sled", "sled machine", "leg press", "leg press machine", "жим ногами", "тренажер для жима ногами"))
                    SmithMachine -> addAll(listOf("smith", "smith machine", "smith_machine", "машина смита", "тренажер смита", "смита"))
                    Hammer -> addAll(listOf("hammer", "hammer machine", "hammer strength", "хамер", "хаммер"))
                    LeverageMachine -> addAll(listOf("leverage", "leverage machine", "рычажныи", "рычажный", "рычагавы"))
                    PecDeckMachine -> addAll(listOf("pec deck", "pec-deck", "pec_deck", "pec deck machine", "butterfly", "butterfly machine", "chest fly machine", "тренажер бабочка", "бабочка", "матылек"))
                    SeatedRowMachine -> addAll(listOf("seated row", "seated-row", "seated_row", "seated row machine", "row machine", "back row machine", "seated cable row", "тяга на спину", "тренажер для тяги", "тренажер для тяги на спину"))
                    AbductorAdductorMachine -> addAll(listOf("abductor", "adductor", "abductor adductor", "abductor adductor machine", "hip abductor", "hip adductor", "отведение бедра", "приведение бедра", "отведение и приведение бедра"))
                    LegCurlMachine -> addAll(listOf("leg curl", "leg-curl", "leg_curl", "leg curl machine", "lying leg curl", "seated leg curl", "сгибание ног", "тренажер для сгибания ног"))
                    LegExtensionMachine -> addAll(listOf("leg extension", "leg-extension", "leg_extension", "leg extensions", "leg extension machine", "разгибание ног", "тренажер для разгибания ног"))
                    CalfMachine -> addAll(listOf("calf", "calf machine", "calf raise", "calf raise machine", "standing calf raise", "seated calf raise", "икры", "лытки", "тренажер для икр"))
                    AbdominalCrunch -> addAll(listOf("abdominal crunch", "abdominal-crunch", "abdominal_crunch", "ab crunch", "ab crunch machine", "crunch machine", "пресс", "прес", "пресс машина", "тренажер для пресса"))
                    HipThrustMachine -> addAll(listOf("hip thrust", "hip-thrust", "hip_thrust", "hip thrust machine", "hip trast", "glute bridge machine", "ягодичныи мост", "ягодичный мост", "тренажер для ягодичного моста"))
                    EzBarbell -> addAll(listOf("ez bar", "ez-bar", "ez_bar", "ez curl bar", "ez barbell", "ez штанга"))
                    TrapBar -> addAll(listOf("trap bar", "trap-bar", "trap_bar", "hex bar", "трап гриф", "трап-гриф"))
                    OlympicBarbell -> addAll(listOf("olympic bar", "olympic barbell", "олимпииская штанга", "олимпийская штанга"))
                    Cable -> addAll(listOf("cable", "cable machine", "crossover", "кроссовер", "кросовер", "блок", "блочныи тренажер", "блочный тренажер"))
                    Rope -> addAll(listOf("rope", "battle rope", "cable rope", "канат", "вяроука"))
                    StabilityBall -> addAll(listOf("stability ball", "swiss ball", "fitball", "фитбол"))
                    BosuBall -> addAll(listOf("bosu", "bosu ball", "мяч босу"))
                    MedicineBall -> addAll(listOf("medicine ball", "med ball", "медицинскии мяч", "медицинский мяч"))
                    Kettlebell -> addAll(listOf("kettlebell", "kettle bell", "гиря", "гіра"))
                    Dumbbell -> addAll(listOf("dumbbell", "dumb bell", "гантель", "гантэль"))
                    Barbell -> addAll(listOf("barbell", "bar bell", "штанга"))
                    Tire -> addAll(listOf("tire", "tyre", "покрышка", "пакрышка"))
                    Unknown -> Unit
                }
            }

            return buildSet {
                baseAliases.forEach { alias ->
                    val normalizedAlias = normalize(alias)
                    if (normalizedAlias.isNotBlank()) {
                        add(normalizedAlias)
                        add(compact(normalizedAlias))
                    }
                }
            }
        }


        fun fromCatalogOption(option: LocalizedOption): Equipment {
            return all.firstOrNull { equipment ->
                equipment.key == option.key || equipment.toLocalizedMap() == option.localized
            } ?: fromCatalogText(option.key) ?: option.localized.values.firstNotNullOfOrNull { value ->
                fromCatalogText(value)
            } ?: Unknown
        }

        fun fromCatalogText(value: String?): Equipment? {
            val normalizedValue = normalize(value)
            if (normalizedValue.isBlank()) return null
            val compactValue = compact(normalizedValue)

            return all.firstOrNull { equipment ->
                val directValues = buildList {
                    add(equipment.key)
                    addAll(equipment.toLocalizedMap().values)
                }

                directValues.any { direct ->
                    val normalizedDirect = normalize(direct)
                    normalizedValue == normalizedDirect || compactValue == compact(normalizedDirect)
                }
            }
        }

        fun fromLocalizedMap(values: Map<String, String>): Equipment {
            if (values.isEmpty()) return Unknown

            all.firstOrNull { equipment ->
                val equipmentMap = equipment.toLocalizedMap()
                equipmentMap["en"] == values["en"] ||
                        equipmentMap["ru"] == values["ru"] ||
                        equipmentMap["be"] == values["be"] ||
                        equipment.key == values["key"]
            }?.let { return it }

            values["key"]?.let { key -> fromCatalogText(key)?.let { return it } }
            values["en"]?.let { en -> fromCatalogText(en)?.let { return it } }
            values["ru"]?.let { ru -> fromCatalogText(ru)?.let { return it } }
            values["be"]?.let { be -> fromCatalogText(be)?.let { return it } }

            values.values.forEach { value ->
                fromCatalogText(value)?.let { return it }
            }

            return Unknown
        }

        fun fromString(value: String): Equipment {
            val candidates = inputCandidates(value)
            if (candidates.isEmpty()) return Unknown

            val aliasesByEquipment = all.associateWith { aliasesFor(it) }

            aliasesByEquipment.firstNotNullOfOrNull { (equipment, aliases) ->
                equipment.takeIf { candidates.any { candidate -> candidate in aliases } }
            }?.let { return it }

            aliasesByEquipment.firstNotNullOfOrNull { (equipment, aliases) ->
                equipment.takeIf {
                    candidates.any { candidate ->
                        aliases.any { alias ->
                            alias.length >= 4 && candidate.length >= 4 &&
                                    (candidate.contains(alias) || alias.contains(candidate))
                        }
                    }
                }
            }?.let { return it }

            return Unknown
        }

    }
}

fun getIconForEquipment(equipment: String): Int {
    val value = equipment.trim()
    if (value.isBlank()) return Equipment.Unknown.iconResId

    val matched = runCatching { Equipment.fromString(value) }.getOrDefault(Equipment.Unknown)
    return if (matched != Equipment.Unknown) matched.iconResId else Equipment.Unknown.iconResId
}

fun getIconForEquipment(equipment: Equipment): Int = equipment.iconResId

fun getIconForEquipment(equipmentLocalized: Map<String, String>): Int {
    return Equipment.fromLocalizedMap(equipmentLocalized).iconResId
}

fun getIconForEquipmentOption(option: LocalizedOption): Int {
    return Equipment.fromCatalogOption(option).iconResId
}
