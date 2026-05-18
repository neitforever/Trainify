package com.example.motivationcalendarapi.repositories.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToLong

class HealthConnectRepository(private val context: Context) {
    private val providerPackageName = "com.google.android.apps.healthdata"

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    )

    val permissionContract
        get() = PermissionController.createRequestPermissionResultContract()

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context, providerPackageName) == HealthConnectClient.SDK_AVAILABLE

    fun openInstallIntent(): Intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding")
        setPackage("com.android.vending")
    }

    private fun client(): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    private suspend fun <T> safeHealthRead(block: suspend () -> T): T? {
        return try {
            block()
        } catch (exception: SecurityException) {
            Log.w("HealthConnectRepository", "Health Connect permission/foreground restriction", exception)
            null
        } catch (exception: Exception) {
            Log.w("HealthConnectRepository", "Health Connect read failed", exception)
            null
        }
    }

    suspend fun hasPermissions(): Boolean {
        if (!isAvailable()) return false
        return safeHealthRead {
            client().permissionController.getGrantedPermissions().containsAll(permissions)
        } ?: false
    }


    suspend fun readTodaySteps(): Long? {
        if (!hasPermissions()) return null
        return safeHealthRead {
            val range = todayLocalRange()
            val response = client().aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(range.first, range.second)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        }
    }


    suspend fun readTodayCalories(): Long? {
        if (!hasPermissions()) return null
        return safeHealthRead {
            val range = todayLocalRange()
            val response = client().aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(range.first, range.second)
                )
            )

            response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                ?.inKilocalories
                ?.roundToLong()
                ?: 0L
        }
    }


    suspend fun readLatestHeartRate(): Long? {
        if (!hasPermissions()) return null
        return safeHealthRead {
            val now = Instant.now()
            val response = client().readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minusSeconds(60 * 60), now),
                    ascendingOrder = false,
                    pageSize = 100
                )
            )
            response.records
                .asSequence()
                .flatMap { it.samples.asSequence() }
                .maxByOrNull { it.time }
                ?.beatsPerMinute
        }
    }


    suspend fun readAverageHeartRateSince(startMillis: Long): Long? {
        if (!hasPermissions() || startMillis <= 0L) return null
        return safeHealthRead {
            val start = Instant.ofEpochMilli(startMillis)
            val end = Instant.now()
            val response = client().readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                    pageSize = 1000
                )
            )
            val samples = response.records
                .asSequence()
                .flatMap { it.samples.asSequence() }
                .map { it.beatsPerMinute }
                .toList()

            samples.takeIf { it.isNotEmpty() }?.average()?.roundToLong()
        }
    }


    suspend fun readConnectedDeviceLabel(): String? {
        if (!hasPermissions()) return null
        val records = readRecentHeartRateRecords()

        return records
            .asSequence()
            .mapNotNull { record ->
                val device = record.metadata.device ?: return@mapNotNull null
                buildDeviceLabel(device.manufacturer, device.model)
            }
            .firstOrNull { it.isNotBlank() }
    }


    suspend fun hasHeartRateSource(): Boolean {
        if (!hasPermissions()) return false
        return readRecentHeartRateRecords().any { record ->
            record.samples.any { sample ->
                sample.beatsPerMinute > 0
            }
        }
    }


    private suspend fun readRecentHeartRateRecords(): List<HeartRateRecord> {
        return safeHealthRead {
            val now = Instant.now()
            val start = now.minusSeconds(5 * 60)
            client().readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, now),
                    pageSize = 100
                )
            ).records
        } ?: emptyList()
    }

    private fun buildDeviceLabel(manufacturer: String?, model: String?): String {
        val parts = listOfNotNull(
            manufacturer?.takeIf { it.isNotBlank() },
            model?.takeIf { it.isNotBlank() }
        ).distinct()
        return parts.joinToString(" ")
    }

    private fun todayLocalRange(): Pair<LocalDateTime, LocalDateTime> {
        val start = LocalDate.now().atStartOfDay()
        val end = LocalDateTime.now()
        return start to end
    }
}
