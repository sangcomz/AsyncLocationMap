package io.github.sangcomz.asynclocationmap.data.util

import kotlin.math.roundToInt

/**
 * Location Normalizer Utility
 *
 * 위도와 경도를 정규화하는 유틸리티 객체입니다.
 *
 * 정규화의 목적:
 * 1. GPS 오차로 인한 미세한 좌표 차이를 같은 위치로 간주
 * 2. 데이터베이스 저장 공간 절약
 * 3. 중복 위치 검색의 정확도 향상
 *
 * 정규화 방식:
 * - 소수점 5자리까지 유지 (약 1.1m 정확도)
 * - 예: 37.123456789 -> 37.12345
 *
 * 사용처:
 * - LocationMapper: Domain Model -> Entity 변환 시
 * - RoomLocationDataSource: 중복 위치 검색 시
 */
object LocationNormalizer {

    private const val PRECISION = 1e5

    /**
     * 위도 또는 경도 값을 정규화합니다.
     *
     * @param value 정규화할 좌표 값 (위도 또는 경도)
     * @return 정규화된 좌표 값 (소수점 5자리)
     */
    fun normalize(value: Double): Double {
        return (value * PRECISION).roundToInt() / PRECISION
    }

    /**
     * 위도와 경도를 함께 정규화합니다.
     *
     * @param latitude 위도
     * @param longitude 경도
     * @return 정규화된 (위도, 경도) Pair
     */
    fun normalize(latitude: Double, longitude: Double): Pair<Double, Double> {
        return Pair(normalize(latitude), normalize(longitude))
    }
}

