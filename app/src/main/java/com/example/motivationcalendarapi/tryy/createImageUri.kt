package com.example.motivationcalendarapi.tryy

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream



fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    val file = File(context.filesDir, "progress_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}