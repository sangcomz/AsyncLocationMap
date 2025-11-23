package io.github.sangcomz.asynclocationmap.testing.data

import io.github.sangcomz.asynclocationmap.domain.model.Location

/**
 * Test Location Data
 *
 * 테스트에서 사용할 고정된 Location 데이터를 제공합니다.
 * 엣지 케이스를 포함하여 다양한 시나리오를 커버합니다.
 */
object TestLocations {

    // 현재 시간을 기준으로 한 타임스탬프
    private const val BASE_TIMESTAMP = 1700000000000L // 2023-11-14 22:13:20 UTC

    /**
     * 빈 리스트 - 데이터가 없는 경우
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
     * 서울과 동일한 좌표지만 다른 timestamp (중복 좌표 테스트용)
     */
    val SEOUL_DUPLICATE = Location(
        id = 4L,
        latitude = 37.5665,
        longitude = 126.9780,
        timestamp = BASE_TIMESTAMP + 3000
    )

    /**
     * 정규화 테스트용 - 소수점이 많은 좌표
     */
    val LOCATION_WITH_MANY_DECIMALS = Location(
        id = 5L,
        latitude = 37.123456789,
        longitude = 127.987654321,
        timestamp = BASE_TIMESTAMP + 4000
    )

    /**
     * 경계값 테스트 - 북극 (최대 위도)
     */
    val NORTH_POLE = Location(
        id = 6L,
        latitude = 90.0,
        longitude = 0.0,
        timestamp = BASE_TIMESTAMP + 5000
    )

    /**
     * 경계값 테스트 - 남극 (최소 위도)
     */
    val SOUTH_POLE = Location(
        id = 7L,
        latitude = -90.0,
        longitude = 0.0,
        timestamp = BASE_TIMESTAMP + 6000
    )

    /**
     * 경계값 테스트 - 날짜변경선 동쪽 (최대 경도)
     */
    val DATELINE_EAST = Location(
        id = 8L,
        latitude = 0.0,
        longitude = 180.0,
        timestamp = BASE_TIMESTAMP + 7000
    )

    /**
     * 경계값 테스트 - 날짜변경선 서쪽 (최소 경도)
     */
    val DATELINE_WEST = Location(
        id = 9L,
        latitude = 0.0,
        longitude = -180.0,
        timestamp = BASE_TIMESTAMP + 8000
    )

    /**
     * 음수 좌표 (남반구, 서반구)
     */
    val NEGATIVE_COORDINATES = Location(
        id = 10L,
        latitude = -33.8688,  // 시드니
        longitude = 151.2093,
        timestamp = BASE_TIMESTAMP + 9000
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
     * 중복 좌표 포함 리스트
     */
    val LOCATIONS_WITH_DUPLICATE = listOf(SEOUL, BUSAN, SEOUL_DUPLICATE)

    /**
     * 경계값 테스트 리스트
     */
    val BOUNDARY_LOCATIONS = listOf(
        NORTH_POLE,
        SOUTH_POLE,
        DATELINE_EAST,
        DATELINE_WEST
    )

    /**
     * 모든 테스트 데이터
     */
    val ALL_LOCATIONS = listOf(
        SEOUL,
        BUSAN,
        JEJU,
        SEOUL_DUPLICATE,
        LOCATION_WITH_MANY_DECIMALS,
        NORTH_POLE,
        SOUTH_POLE,
        DATELINE_EAST,
        DATELINE_WEST,
        NEGATIVE_COORDINATES
    )
}

