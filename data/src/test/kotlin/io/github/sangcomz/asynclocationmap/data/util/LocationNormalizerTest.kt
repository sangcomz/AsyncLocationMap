package io.github.sangcomz.asynclocationmap.data.util

import io.github.sangcomz.asynclocationmap.testing.data.TestLocations
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * LocationNormalizer Unit Test
 *
 * 좌표 정규화 로직을 검증합니다.
 */
class LocationNormalizerTest {

    @Test
    fun `normalize should round to 5 decimal places`() {
        // Given: 소수점이 많은 좌표
        val latitude = 37.123456789
        val longitude = 127.987654321

        // When: 정규화
        val normalizedLat = LocationNormalizer.normalize(latitude)
        val normalizedLng = LocationNormalizer.normalize(longitude)

        // Then: 소수점 5자리로 반올림
        assertEquals(37.12346, normalizedLat, 0.000001)
        assertEquals(127.98765, normalizedLng, 0.000001)
    }

    @Test
    fun `normalize should handle exact 5 decimals`() {
        // Given: 정확히 소수점 5자리
        val value = 37.12345

        // When: 정규화
        val result = LocationNormalizer.normalize(value)

        // Then: 값이 변하지 않음
        assertEquals(37.12345, result, 0.000001)
    }

    @Test
    fun `normalize should handle integers`() {
        // Given: 정수 좌표
        val value = 90.0

        // When: 정규화
        val result = LocationNormalizer.normalize(value)

        // Then: 값이 변하지 않음
        assertEquals(90.0, result, 0.000001)
    }

    @Test
    fun `normalize should handle negative coordinates`() {
        // Given: 음수 좌표
        val latitude = -33.868888
        val longitude = -151.209333

        // When: 정규화
        val normalizedLat = LocationNormalizer.normalize(latitude)
        val normalizedLng = LocationNormalizer.normalize(longitude)

        // Then: 소수점 5자리로 반올림 (음수)
        assertEquals(-33.86889, normalizedLat, 0.000001)
        assertEquals(-151.20933, normalizedLng, 0.000001)
    }

    @Test
    fun `normalize should handle boundary values`() {
        // Given: 경계값
        val maxLat = 90.0
        val minLat = -90.0
        val maxLng = 180.0
        val minLng = -180.0

        // When: 정규화
        val normalizedMaxLat = LocationNormalizer.normalize(maxLat)
        val normalizedMinLat = LocationNormalizer.normalize(minLat)
        val normalizedMaxLng = LocationNormalizer.normalize(maxLng)
        val normalizedMinLng = LocationNormalizer.normalize(minLng)

        // Then: 값이 변하지 않음
        assertEquals(90.0, normalizedMaxLat, 0.000001)
        assertEquals(-90.0, normalizedMinLat, 0.000001)
        assertEquals(180.0, normalizedMaxLng, 0.000001)
        assertEquals(-180.0, normalizedMinLng, 0.000001)
    }

    @Test
    fun `normalize pair should normalize both coordinates`() {
        // Given: 좌표 쌍
        val latitude = 37.123456789
        val longitude = 127.987654321

        // When: Pair로 정규화
        val (normalizedLat, normalizedLng) = LocationNormalizer.normalize(latitude, longitude)

        // Then: 둘 다 정규화됨
        assertEquals(37.12346, normalizedLat, 0.000001)
        assertEquals(127.98765, normalizedLng, 0.000001)
    }

    @Test
    fun `normalize should make similar coordinates equal`() {
        // Given: 미세하게 다른 좌표 (소수점 6자리 이하 차이 - GPS 오차 범위)
        // 소수점 5자리까지는 동일하고, 6자리 이후가 다른 값
        val coord1 = 37.123450001  // 반올림 → 37.12345
        val coord2 = 37.123449999  // 반올림 → 37.12345

        // When: 정규화
        val normalized1 = LocationNormalizer.normalize(coord1)
        val normalized2 = LocationNormalizer.normalize(coord2)

        // Then: 동일한 값으로 정규화됨 (소수점 5자리)
        assertEquals(37.12345, normalized1, 0.000001)
        assertEquals(37.12345, normalized2, 0.000001)
        assertEquals(normalized1, normalized2, 0.000001)
    }

    @Test
    fun `normalize should handle zero`() {
        // Given: 0
        val value = 0.0

        // When: 정규화
        val result = LocationNormalizer.normalize(value)

        // Then: 0.0
        assertEquals(0.0, result, 0.000001)
    }

    @Test
    fun `normalize should handle very small decimals`() {
        // Given: 매우 작은 소수점
        val value = 0.000001

        // When: 정규화
        val result = LocationNormalizer.normalize(value)

        // Then: 0으로 반올림
        assertEquals(0.0, result, 0.000001)
    }
}

