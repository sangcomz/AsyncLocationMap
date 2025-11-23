package io.github.sangcomz.asynclocationmap.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import io.github.sangcomz.asynclocationmap.data.datasource.LocationProvider
import io.github.sangcomz.asynclocationmap.domain.model.Location
import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Location Worker (HiltWorker with Foreground Service)
 *
 * WorkManager를 통해 백그라운드에서 현재 위치를 조회하고 저장하는 Worker입니다.
 * Foreground Service로 실행되어 백그라운드에서도 안정적으로 동작합니다.
 *
 * @HiltWorker를 사용하여 Hilt 의존성 주입을 지원합니다.
 *
 * 주요 기능:
 * 1. Foreground Service로 실행되어 백그라운드 제약 회피
 * 2. LocationProvider를 통해 현재 위치 조회 (추상화)
 * 3. 조회된 위치를 Repository를 통해 로컬 저장소에 저장
 * 4. 알림을 통해 사용자에게 위치 조회 중임을 표시
 *
 * Strategy Pattern을 적용하여 LocationProvider 구현체를 쉽게 교체할 수 있습니다.
 *
 * @param context Android Context (WorkManager가 자동 주입)
 * @param params Worker 파라미터 (WorkManager가 자동 주입)
 * @param repository 위치 정보 Repository (Hilt가 주입)
 * @param locationProvider 위치 조회 Provider (Hilt가 주입, 현재는 FusedLocationProvider)
 */
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LocationRepository,
    private val locationProvider: LocationProvider
) : CoroutineWorker(context, params) {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "location_worker_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "위치 추적"
        private const val NOTIFICATION_ID = 1
    }

    /**
     * Foreground Service로 실행하기 위한 ForegroundInfo를 생성합니다.
     * 알림 채널을 생성하고 알림을 표시합니다.
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    /**
     * ForegroundInfo를 생성합니다.
     * Android 8.0 이상에서는 알림 채널을 생성합니다.
     */
    private fun createForegroundInfo(): ForegroundInfo {
        // 알림 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // 알림 생성
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("위치 추적 중")
            .setContentText("현재 위치를 가져오고 있습니다...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    /**
     * 알림 채널을 생성합니다 (Android 8.0 이상).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "위치 추적 알림"
            }

            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 백그라운드에서 실행되는 작업입니다.
     * Foreground Service로 실행되어 백그라운드 제약을 회피합니다.
     * LocationProvider를 통해 현재 위치를 조회하여 저장합니다.
     *
     * @return Result.success() - 위치 조회 및 저장 성공
     *         Result.retry() - 위치를 가져올 수 없음 (일시적 실패)
     *         Result.failure() - 예외 발생 (영구 실패)
     */
    override suspend fun doWork(): Result {
        return try {
            // Foreground Service로 설정
            setForeground(getForegroundInfo())

            // LocationProvider를 통해 현재 위치 조회
            val androidLocation = locationProvider.getCurrentLocation()

            if (androidLocation != null) {
                // Domain Model로 변환
                val location = Location(
                    latitude = androidLocation.latitude,
                    longitude = androidLocation.longitude,
                    timestamp = System.currentTimeMillis()
                )

                // Repository를 통해 저장
                repository.saveLocation(location)

                Result.success()
            } else {
                // 위치를 가져올 수 없는 경우 (권한 없음 또는 위치 조회 실패)
                Result.retry()
            }
        } catch (e: Exception) {
            // 예외 발생 시 실패 처리
            Result.failure()
        }
    }
}
