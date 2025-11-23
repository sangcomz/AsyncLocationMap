package io.github.sangcomz.asynclocationmap.data.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.sangcomz.asynclocationmap.domain.model.Location
import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

/**
 * Location Worker (HiltWorker)
 *
 * WorkManager를 통해 백그라운드에서 현재 위치를 조회하고 저장하는 Worker입니다.
 * @HiltWorker를 사용하여 Hilt 의존성 주입을 지원합니다.
 *
 * 주요 기능:
 * 1. FusedLocationProviderClient를 사용하여 현재 위치 조회
 * 2. 조회된 위치를 Repository를 통해 Room Database에 저장
 * 3. 앱이 백그라운드에 있어도 작업 지속
 *
 * @param context Android Context (WorkManager가 자동 주입)
 * @param params Worker 파라미터 (WorkManager가 자동 주입)
 * @param repository 위치 정보 Repository (Hilt가 주입)
 * @param fusedLocationClient Google Play Services Location Client (Hilt가 주입)
 */
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LocationRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : CoroutineWorker(context, params) {

    /**
     * 백그라운드에서 실행되는 작업입니다.
     * 위치 권한을 확인하고, 현재 위치를 조회하여 저장합니다.
     *
     * @return Result.success() - 위치 조회 및 저장 성공
     *         Result.retry() - 위치를 가져올 수 없음 (일시적 실패)
     *         Result.failure() - 권한 없음 또는 예외 발생 (영구 실패)
     */
    override suspend fun doWork(): Result {
        return try {
            // 위치 권한 확인
            if (!hasLocationPermission()) {
                return Result.failure()
            }

            // 현재 위치 조회
            val androidLocation = fusedLocationClient.lastLocation.await()

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
                // 위치를 가져올 수 없는 경우 재시도
                Result.retry()
            }
        } catch (e: SecurityException) {
            // 권한 문제
            Result.failure()
        } catch (e: Exception) {
            // 기타 예외
            Result.failure()
        }
    }

    /**
     * 위치 권한이 있는지 확인합니다.
     *
     * @return true - 권한 있음, false - 권한 없음
     */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}
