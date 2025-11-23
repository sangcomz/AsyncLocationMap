package io.github.sangcomz.asynclocationmap.data.datasource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Fused Location Provider
 *
 * Google Play Services의 FusedLocationProviderClient를 사용하여
 * 현재 위치를 조회하는 LocationProvider 구현체입니다.
 *
 * FusedLocationProviderClient의 장점:
 * - 배터리 효율적
 * - 다양한 소스(GPS, WiFi, Cell)를 조합하여 최적의 위치 제공
 * - Google Play Services와 통합
 *
 * @param context Application Context
 * @param fusedLocationClient FusedLocationProviderClient 인스턴스
 */
class FusedLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationProvider {

    /**
     * 현재 위치를 조회합니다.
     *
     * 위치 권한을 확인하고, FusedLocationProviderClient를 통해
     * 마지막으로 알려진 위치를 반환합니다.
     *
     * @return 현재 위치 정보, 권한 없음 또는 위치 조회 실패 시 null
     */
    override suspend fun getCurrentLocation(): Location? {
        return try {
            // 위치 권한 확인
            if (!hasLocationPermission()) {
                return null
            }

            // 마지막 위치 조회
            fusedLocationClient.lastLocation.await()
        } catch (e: SecurityException) {
            // 권한 문제
            null
        } catch (e: Exception) {
            // 기타 예외
            null
        }
    }

    /**
     * 위치 권한이 있는지 확인합니다.
     *
     * ACCESS_FINE_LOCATION 또는 ACCESS_COARSE_LOCATION 중
     * 하나라도 있으면 true를 반환합니다.
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
