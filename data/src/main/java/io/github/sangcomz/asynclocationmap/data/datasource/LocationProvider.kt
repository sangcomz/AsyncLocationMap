package io.github.sangcomz.asynclocationmap.data.datasource

import android.location.Location

/**
 * Location Provider Interface
 *
 * 현재 위치를 조회하는 작업을 추상화한 인터페이스입니다.
 *
 * 현재는 Google Play Services의 FusedLocationProviderClient를 사용하지만,
 * 나중에 다른 방식으로 쉽게 교체할 수 있도록 Strategy Pattern을 적용했습니다.
 *
 * 가능한 구현체 예시:
 * - FusedLocationProvider: Google Play Services 기반 구현 (현재)
 * - MockLocationProvider: 테스트용 Mock 위치 제공 (테스트)
 * - NetworkLocationProvider: 네트워크 기반 위치 제공 (미래)
 * - GpsLocationProvider: GPS 기반 위치 제공 (미래)
 */
interface LocationProvider {

    /**
     * 현재 위치를 조회합니다.
     *
     * 이 메서드는 suspend 함수로, 위치 조회가 완료될 때까지 대기합니다.
     * 위치 조회에 실패하거나 권한이 없는 경우 null을 반환합니다.
     *
     * 구현체에 따라 다른 메커니즘을 사용할 수 있습니다:
     * - FusedLocationProviderClient: 가장 정확한 위치 제공
     * - LocationManager: 레거시 API 사용
     * - Mock: 테스트용 고정 위치 반환
     *
     * @return 현재 위치 정보, 조회 실패 시 null
     */
    suspend fun getCurrentLocation(): Location?
}
