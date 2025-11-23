package io.github.sangcomz.asynclocationmap.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * StreetViewUrlBuilder Unit Test
 *
 * Street View URL 생성 로직을 검증합니다.
 */
class StreetViewUrlBuilderTest {

    private val testApiKey = "TEST_API_KEY_12345"

    @Test
    fun `buildUrl should create valid URL with default size`() {
        // Given: 기본 파라미터
        val latitude = 37.5665
        val longitude = 126.9780

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(latitude, longitude, testApiKey)

        // Then: 올바른 URL 생성
        assertTrue(url.startsWith("https://maps.googleapis.com/maps/api/streetview?"))
        assertTrue(url.contains("size=160x80"))
        assertTrue(url.contains("location=37.5665,126.978"))
        assertTrue(url.contains("key=$testApiKey"))
    }

    @Test
    fun `buildUrl should create URL with custom size`() {
        // Given: 커스텀 사이즈
        val latitude = 35.1796
        val longitude = 129.0756
        val width = 320
        val height = 160

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(
            latitude, longitude, testApiKey, width, height
        )

        // Then: 커스텀 사이즈가 반영됨
        assertTrue(url.contains("size=320x160"))
        assertTrue(url.contains("location=35.1796,129.0756"))
    }

    @Test
    fun `buildUrl should handle negative coordinates`() {
        // Given: 음수 좌표
        val latitude = -33.8688
        val longitude = 151.2093

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(latitude, longitude, testApiKey)

        // Then: 음수가 올바르게 포함됨
        assertTrue(url.contains("location=-33.8688,151.2093"))
    }

    @Test
    fun `buildUrl should handle boundary coordinates`() {
        // Given: 경계값
        val latitude = 90.0
        val longitude = 180.0

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(latitude, longitude, testApiKey)

        // Then: 경계값이 올바르게 포함됨
        assertTrue(url.contains("location=90.0,180.0"))
    }

    @Test
    fun `buildUrl should format URL correctly`() {
        // Given: 테스트 좌표
        val latitude = 37.5665
        val longitude = 126.9780

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(latitude, longitude, testApiKey)

        // Then: 올바른 포맷
        val expectedUrl = "https://maps.googleapis.com/maps/api/streetview?" +
                "size=160x80&" +
                "location=37.5665,126.978&" +
                "key=$testApiKey"
        assertEquals(expectedUrl, url)
    }

    @Test
    fun `buildUrl should handle zero coordinates`() {
        // Given: 0,0 좌표 (기준점)
        val latitude = 0.0
        val longitude = 0.0

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(latitude, longitude, testApiKey)

        // Then: 0이 올바르게 포함됨
        assertTrue(url.contains("location=0.0,0.0"))
    }

    @Test
    fun `buildUrl should preserve decimal precision`() {
        // Given: 정밀한 소수점
        val latitude = 37.123456
        val longitude = 127.987654

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(latitude, longitude, testApiKey)

        // Then: 소수점이 보존됨
        assertTrue(url.contains("location=37.123456,127.987654"))
    }

    @Test
    fun `buildUrl should handle different API keys`() {
        // Given: 다른 API 키들
        val apiKey1 = "KEY_1"
        val apiKey2 = "KEY_2"

        // When: 각각 URL 생성
        val url1 = StreetViewUrlBuilder.buildUrl(37.5, 127.0, apiKey1)
        val url2 = StreetViewUrlBuilder.buildUrl(37.5, 127.0, apiKey2)

        // Then: 각각 다른 키가 포함됨
        assertTrue(url1.contains("key=KEY_1"))
        assertTrue(url2.contains("key=KEY_2"))
    }

    @Test
    fun `buildUrl should handle large image size`() {
        // Given: 큰 이미지 사이즈
        val width = 640
        val height = 640

        // When: URL 생성
        val url = StreetViewUrlBuilder.buildUrl(
            37.5665, 126.9780, testApiKey, width, height
        )

        // Then: 큰 사이즈가 반영됨
        assertTrue(url.contains("size=640x640"))
    }
}

