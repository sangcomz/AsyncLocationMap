package io.github.sangcomz.asynclocationmap.data.mapper

import io.github.sangcomz.asynclocationmap.data.local.entity.LocationEntity
import io.github.sangcomz.asynclocationmap.testing.data.TestLocations
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * LocationMapper Unit Test
 *
 * Entity와 Domain Model 간의 변환을 검증합니다.
 */
class LocationMapperTest {

    @Test
    fun `toDomain should convert LocationEntity to Location`() {
        // Given: LocationEntity
        val entity = LocationEntity(
            id = 1L,
            latitude = 37.5665,
            longitude = 126.9780,
            timestamp = 1700000000000L
        )

        // When: Domain으로 변환
        val domain = entity.toDomain()

        // Then: 모든 필드가 올바르게 변환됨
        assertEquals(entity.id, domain.id)
        assertEquals(entity.latitude, domain.latitude, 0.000001)
        assertEquals(entity.longitude, domain.longitude, 0.000001)
        assertEquals(entity.timestamp, domain.timestamp)
    }

    @Test
    fun `toEntity should convert Location to LocationEntity`() {
        // Given: Domain Location
        val location = TestLocations.SEOUL

        // When: Entity로 변환
        val entity = location.toEntity()

        // Then: 모든 필드가 올바르게 변환됨
        assertEquals(location.id, entity.id)
        assertEquals(location.timestamp, entity.timestamp)
        // 좌표는 정규화되어야 함
    }

    @Test
    fun `toEntity should normalize coordinates`() {
        // Given: 소수점이 많은 좌표
        val location = TestLocations.LOCATION_WITH_MANY_DECIMALS

        // When: Entity로 변환
        val entity = location.toEntity()

        // Then: 좌표가 정규화됨 (소수점 5자리)
        assertEquals(37.12346, entity.latitude, 0.000001)
        assertEquals(127.98765, entity.longitude, 0.000001)
    }

    @Test
    fun `toEntity should preserve id and timestamp`() {
        // Given: Location
        val location = TestLocations.BUSAN

        // When: Entity로 변환
        val entity = location.toEntity()

        // Then: ID와 timestamp는 그대로 유지
        assertEquals(location.id, entity.id)
        assertEquals(location.timestamp, entity.timestamp)
    }

    @Test
    fun `round trip conversion should preserve data`() {
        // Given: LocationEntity
        val originalEntity = LocationEntity(
            id = 1L,
            latitude = 37.12345,
            longitude = 127.98765,
            timestamp = 1700000000000L
        )

        // When: Entity -> Domain -> Entity
        val domain = originalEntity.toDomain()
        val convertedEntity = domain.toEntity()

        // Then: 데이터가 보존됨 (정규화로 인해 좌표는 동일)
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.latitude, convertedEntity.latitude, 0.000001)
        assertEquals(originalEntity.longitude, convertedEntity.longitude, 0.000001)
        assertEquals(originalEntity.timestamp, convertedEntity.timestamp)
    }

    @Test
    fun `toEntity should handle boundary coordinates`() {
        // Given: 경계값 좌표
        val northPole = TestLocations.NORTH_POLE
        val southPole = TestLocations.SOUTH_POLE

        // When: Entity로 변환
        val northEntity = northPole.toEntity()
        val southEntity = southPole.toEntity()

        // Then: 경계값이 유지됨
        assertEquals(90.0, northEntity.latitude, 0.000001)
        assertEquals(-90.0, southEntity.latitude, 0.000001)
    }

    @Test
    fun `toEntity should handle negative coordinates`() {
        // Given: 음수 좌표
        val location = TestLocations.NEGATIVE_COORDINATES

        // When: Entity로 변환
        val entity = location.toEntity()

        // Then: 음수가 올바르게 처리됨
        assertEquals(location.latitude, entity.latitude, 0.00001)
        assertEquals(location.longitude, entity.longitude, 0.00001)
    }

    @Test
    fun `toDomain should handle zero id`() {
        // Given: ID가 0인 Entity
        val entity = LocationEntity(
            id = 0L,
            latitude = 37.5665,
            longitude = 126.9780,
            timestamp = 1700000000000L
        )

        // When: Domain으로 변환
        val domain = entity.toDomain()

        // Then: ID가 0으로 유지됨
        assertEquals(0L, domain.id)
    }
}

