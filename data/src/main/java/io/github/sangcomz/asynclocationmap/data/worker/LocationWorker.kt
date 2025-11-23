package io.github.sangcomz.asynclocationmap.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.sangcomz.asynclocationmap.data.datasource.LocationProvider
import io.github.sangcomz.asynclocationmap.domain.model.Location
import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Location Worker (HiltWorker)
 *
 * WorkManager를 통해 백그라운드에서 현재 위치를 조회하고 저장하는 Worker입니다.
 * @HiltWorker를 사용하여 Hilt 의존성 주입을 지원합니다.
 *
 * 주요 기능:
 * 1. LocationProvider를 통해 현재 위치 조회 (추상화)
 * 2. 조회된 위치를 Repository를 통해 로컬 저장소에 저장
 * 3. 앱이 백그라운드에 있어도 작업 지속
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

    /**
     * 백그라운드에서 실행되는 작업입니다.
     * LocationProvider를 통해 현재 위치를 조회하여 저장합니다.
     *
     * @return Result.success() - 위치 조회 및 저장 성공
     *         Result.retry() - 위치를 가져올 수 없음 (일시적 실패)
     *         Result.failure() - 예외 발생 (영구 실패)
     */
    override suspend fun doWork(): Result {
        return try {
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
