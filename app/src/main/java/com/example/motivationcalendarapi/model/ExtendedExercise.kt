package com.example.motivationcalendarapi.model

import java.util.UUID

data class ExtendedExercise(
    val exercise: Exercise = Exercise(),
    val sets: List<ExerciseSet> = listOf(
        ExerciseSet(
            rep = 0,
            weight = 0f,
            time = 0f,
            resistance = 0f,
            incline = 0f,
            status = SetStatus.NONE
        )
    ),
    val supersetGroupId: String? = null,
    val supersetOrder: Int? = null
)

enum class SetStatus {
    NONE, WARMUP, FAILED, COMPLETED
}

enum class ExerciseSetType {
    NORMAL, DROP_SET, CLUSTER_SET
}

data class DropSetPart(
    val weight: Float = 0f,
    val rep: Int = 0,
    val status: SetStatus = SetStatus.NONE
)

data class ClusterSetPart(
    val weight: Float = 0f,
    val rep: Int = 0,
    val status: SetStatus = SetStatus.NONE
)

data class ClusterSetData(
    val weight: Float = 0f,
    val clusterCount: Int = 1,
    val repsPerCluster: Int = 1,
    val restBetweenClustersSec: Int = 20
)

data class ExerciseSet(
    val rep: Int = 0,
    val weight: Float = 0f,

    val time: Float = 0f,
    val resistance: Float = 0f,
    val incline: Float = 0f,

    val status: SetStatus = SetStatus.NONE,
    val type: ExerciseSetType = ExerciseSetType.NORMAL,
    val dropSetParts: List<DropSetPart> = emptyList(),
    val clusterSetData: ClusterSetData? = null,
    val clusterSetParts: List<ClusterSetPart> = emptyList()
)

fun ExerciseSet.toNormalSet(): ExerciseSet = copy(
    type = ExerciseSetType.NORMAL,
    dropSetParts = emptyList(),
    clusterSetData = null,
    clusterSetParts = emptyList()
)

fun ExerciseSet.toDefaultDropSet(): ExerciseSet {
    val firstWeight = weight.coerceAtLeast(0f)
    val firstRep = rep.coerceAtLeast(0)
    val secondWeight = (firstWeight * 0.8f).coerceAtLeast(0f)
    val secondRep = firstRep.takeIf { it > 0 } ?: 8
    return copy(
        type = ExerciseSetType.DROP_SET,
        rep = firstRep,
        weight = firstWeight,
        dropSetParts = listOf(
            DropSetPart(weight = firstWeight, rep = secondRep),
            DropSetPart(weight = secondWeight, rep = secondRep)
        ),
        clusterSetData = null,
        clusterSetParts = emptyList()
    )
}

fun ExerciseSet.toDefaultClusterSet(): ExerciseSet {
    val baseRep = rep.takeIf { it > 0 } ?: 8
    val repsPerCluster = 2
    val clusterCount = (baseRep / repsPerCluster).coerceAtLeast(1)
    return copy(
        type = ExerciseSetType.CLUSTER_SET,
        rep = clusterCount * repsPerCluster,
        weight = weight.coerceAtLeast(0f),
        dropSetParts = emptyList(),
        clusterSetData = ClusterSetData(
            weight = weight.coerceAtLeast(0f),
            clusterCount = clusterCount,
            repsPerCluster = repsPerCluster,
            restBetweenClustersSec = 20
        ),
        clusterSetParts = List(clusterCount) {
            ClusterSetPart(
                weight = weight.coerceAtLeast(0f),
                rep = repsPerCluster,
                status = SetStatus.NONE
            )
        }
    )
}

fun newSupersetGroupId(): String = UUID.randomUUID().toString()
