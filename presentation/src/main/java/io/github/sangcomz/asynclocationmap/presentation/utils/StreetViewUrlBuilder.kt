package io.github.sangcomz.asynclocationmap.presentation.utils

/**
 * Google Street View Static API URL Builder
 *
 * 위도/경도를 기반으로 Street View 이미지 URL을 생성합니다.
 *
 * API 문서: https://developers.google.com/maps/documentation/streetview/overview
 */
object StreetViewUrlBuilder {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/streetview"

    /**
     * Street View Static API URL을 생성합니다.
     *
     * @param latitude 위도
     * @param longitude 경도
     * @param apiKey Google Maps API Key
     * @param width 이미지 너비 (픽셀)
     * @param height 이미지 높이 (픽셀)
     * @return Street View 이미지 URL
     */
    fun buildUrl(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        width: Int = 160,
        height: Int = 80
    ): String {
        return "$BASE_URL?" +
                "size=${width}x${height}&" +
                "location=$latitude,$longitude&" +
                "key=$apiKey"
    }
}

