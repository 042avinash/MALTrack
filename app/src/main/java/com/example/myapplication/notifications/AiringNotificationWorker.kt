package com.example.myapplication.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.local.getPreferredTitle
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

private const val AIRING_NOTIFICATION_CHANNEL_ID = "airing_updates"
private const val AIRING_NOTIFICATION_WORK_NAME = "airing_notification_work"
private const val AIRING_NOTIFICATION_IMMEDIATE_WORK_NAME = "airing_notification_immediate_work"
private const val INPUT_IS_TEST_NOTIFICATION = "is_test_notification"

object AiringNotificationScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<AiringNotificationWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AIRING_NOTIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun triggerNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<AiringNotificationWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            AIRING_NOTIFICATION_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun triggerTest(context: Context) {
        val request = OneTimeWorkRequestBuilder<AiringNotificationWorker>()
            .setInputData(
                Data.Builder()
                    .putBoolean(INPUT_IS_TEST_NOTIFICATION, true)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            AIRING_NOTIFICATION_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(AIRING_NOTIFICATION_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(AIRING_NOTIFICATION_IMMEDIATE_WORK_NAME)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AiringNotificationWorkerEntryPoint {
    fun repository(): AnimeRepository
    fun preferencesManager(): UserPreferencesManager
}

class AiringNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            AiringNotificationWorkerEntryPoint::class.java
        )
    }

    override suspend fun doWork(): Result {
        if (!canPostNotifications()) return Result.success()

        createNotificationChannel()

        return runCatching {
            if (inputData.getBoolean(INPUT_IS_TEST_NOTIFICATION, false)) {
                postNotification(
                    animeId = Int.MAX_VALUE,
                    title = "MALTrack",
                    airedEpisode = 1,
                    isTest = true
                )
                return Result.success()
            }

            val repository = entryPoint.repository()
            val prefsManager = entryPoint.preferencesManager()

            if (!prefsManager.episodeNotificationsEnabledFlow.first()) {
                return Result.success()
            }

            val baselines = prefsManager.episodeNotificationBaselinesFlow.first().toMutableMap()
            val watchingAnime = repository.getAllUserAnimeList(status = "watching", sort = "list_updated_at")
            val airingMap = watchingAnime
                .map { it.node.id }
                .distinct()
                .chunked(50)
                .flatMap { repository.getAiringAnimeDetails(it) }
                .mapNotNull { media -> media.idMal?.let { it to media } }
                .toMap()

            var baselinesChanged = false

            watchingAnime.forEach { anime ->
                val media = airingMap[anime.node.id] ?: return@forEach
                val nextEpisode = media.nextAiringEpisode?.episode ?: return@forEach
                val key = anime.node.id.toString()
                val previousBaseline = baselines[key]

                if (previousBaseline == null) {
                    baselines[key] = nextEpisode
                    baselinesChanged = true
                    return@forEach
                }

                if (nextEpisode > previousBaseline) {
                    val airedEpisode = nextEpisode - 1
                    postNotification(anime.node.id, anime.node.getPreferredTitle(TitleLanguage.ROMAJI), airedEpisode)
                    baselines[key] = nextEpisode
                    baselinesChanged = true
                }
            }

            if (baselinesChanged) {
                prefsManager.saveEpisodeNotificationBaselines(baselines)
            }

            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return false
        }

        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                AIRING_NOTIFICATION_CHANNEL_ID,
                "Airing Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when new watched anime episodes air"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun postNotification(animeId: Int, title: String, airedEpisode: Int, isTest: Boolean = false) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            animeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, AIRING_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(if (isTest) "Test notification" else "New episode aired")
            .setContentText(
                if (isTest) {
                    "Notifications are working on this device."
                } else {
                    "$title episode $airedEpisode is now out."
                }
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        if (isTest) {
                            "Notifications are working on this device."
                        } else {
                            "$title episode $airedEpisode is now out."
                        }
                    )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(animeId, notification)
    }
}
