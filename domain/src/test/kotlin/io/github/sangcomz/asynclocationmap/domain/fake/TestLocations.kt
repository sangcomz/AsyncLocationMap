package io.github.sangcomz.asynclocationmap.domain.fake

import io.github.sangcomz.asynclocationmap.domain.model.Location

/**
 * Test Location Data for Domain Layer
 *
 * Domain 레이어 테스트에서 사용할 고정된 Location 데이터를 제공합니다.
 */
object TestLocations {

    private const val BASE_TIMESTAMP = 1700000000000L // 2023-11-14 22:13:20 UTC

    /**
     * 빈 리스트
     */
    val EMPTY_LIST = emptyList<Location>()

    /**
     * 서울 (정상적인 단일 위치)
     */
    val SEOUL = Location(
        id = 1L,
        latitude = 37.5665,
        longitude = 126.9780,
        timestamp = BASE_TIMESTAMP
    )

    /**
     * 부산 (두 번째 위치)
     */
    val BUSAN = Location(
        id = 2L,
        latitude = 35.1796,
        longitude = 129.0756,
        timestamp = BASE_TIMESTAMP + 1000
    )

    /**
     * 제주 (세 번째 위치)
     */
    val JEJU = Location(
        id = 3L,
        latitude = 33.4996,
        longitude = 126.5312,
        timestamp = BASE_TIMESTAMP + 2000
    )

    /**
     * 단일 위치 리스트
     */
    val SINGLE_LOCATION = listOf(SEOUL)

    /**
     * 여러 위치 리스트 (3개)
     */
    val MULTIPLE_LOCATIONS = listOf(SEOUL, BUSAN, JEJU)

    /**
     * 모든 테스트 데이터
     */
    val ALL_LOCATIONS = listOf(SEOUL, BUSAN, JEJU)
}

