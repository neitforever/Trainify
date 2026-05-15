//package com.example.motivationcalendarapi.database
//
//import com.example.motivationcalendarapi.model.Exercise as NetworkExercise
//
//fun NetworkExercise.toDatabaseModel(): NetworkExercise {
//    return NetworkExercise(
//        id = this.id,
//        bodyPart = this.bodyPart,
//        name = this.name,
//        equipment = this.equipment,
//        target = this.target,
//        secondaryMuscles = this.secondaryMuscles,
//        instructions = this.instructions,
//        gifUrl = this.gifUrl,
//        favorite = this.favorite,
//        note = this.note
//    )
//}

//fun List<NetworkExercise>.toDatabaseModels(): List<NetworkExercise> {
//    return this.map { it.toDatabaseModel() }
//}


//fun Workout.toDatabaseModel():  {
//    return NetworkExercise(
//        id = this.id,
//        bodyPart = this.bodyPart,
//        name = this.name,
//        equipment = this.equipment,
//        target = this.target,
//        secondaryMuscles = this.secondaryMuscles,
//        instructions = this.instructions,
//        gifUrl = this.gifUrl
//    )
//}
