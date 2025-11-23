package io.github.sangcomz.asynclocationmap.data.datasource

import io.github.sangcomz.asynclocationmap.data.local.dao.LocationDao
import io.github.sangcomz.asynclocationmap.data.local.entity.LocationEntity
import io.github.sangcomz.asynclocationmap.data.mapper.toDomain
import io.github.sangcomz.asynclocationmap.domain.model.Location
import io.github.sangcomz.asynclocationmap.testing.data.TestLocations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * RoomLocationDataSource Unit Test
 *
 * Room 기반 LocalDataSource의 동작을 검증합니다.
 * Fake DAO를 사용하여 중복 검사 로직을 테스트합니다.
 */
class RoomLocationDataSourceTest {

    private lateinit var fakeDao: FakeLocationDaoImpl
    private lateinit var dataSource: RoomLocationDataSource

    @Before
    fun setup() {
        fakeDao = FakeLocationDaoImpl()
        dataSource = RoomLocationDataSource(fakeDao)
    }

    @After
    fun tearDown() {
        fakeDao.clear()
    }

    /**
     * 테스트용 Fake LocationDao 구현
     */
    private class FakeLocationDaoImpl : LocationDao {
        private val _entities = MutableStateFlow<List<LocationEntity>>(emptyList())

        var insertLocationCallCount = 0
        var findLocationByLatLngCallCount = 0

        override fun getAllLocations(): Flow<List<LocationEntity>> {
            return _entities
        }

        override suspend fun getLastLocation(): LocationEntity? {
            return _entities.value.maxByOrNull { it.timestamp }
        }

        override suspend fun insertLocation(location: LocationEntity) {
            insertLocationCallCount++

            val currentList = _entities.value.toMutableList()

            // ID가 0이면 자동 생성
            val newEntity = if (location.id == 0L) {
                val newId = (currentList.maxOfOrNull { it.id } ?: 0L) + 1
                location.copy(id = newId)
            } else {
                location
            }

            // 동일 ID가 있으면 업데이트
            val existingIndex = currentList.indexOfFirst { it.id == newEntity.id }
            if (existingIndex != -1) {
                currentList[existingIndex] = newEntity
            } else {
                currentList.add(newEntity)
            }

            _entities.value = currentList.sortedByDescending { it.timestamp }
        }

        override suspend fun findLocationByLatLng(latitude: Double, longitude: Double): LocationEntity? {
            findLocationByLatLngCallCount++
            return _entities.value.find {
                it.latitude == latitude && it.longitude == longitude
            }
        }

        override suspend fun deleteAllLocations() {
            _entities.value = emptyList()
        }

        fun setEntities(entities: List<LocationEntity>) {
            _entities.value = entities.sortedByDescending { it.timestamp }
        }

        fun clear() {
            _entities.value = emptyList()
            insertLocationCallCount = 0
            findLocationByLatLngCallCount = 0
        }

        fun getCurrentEntities(): List<LocationEntity> {
            return _entities.value
        }
    }

    private fun Location.toEntity(): LocationEntity {
        return LocationEntity(
            id = id,
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp
        )
    }

    @Test
    fun `getAllLocations should return empty list initially`() = runTest {
        // Given: 빈 DAO

        // When: getAllLocations 호출
        val result = dataSource.getAllLocations().first()

        // Then: 빈 리스트 반환
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllLocations should return all locations from DAO`() = runTest {
        // Given: DAO에 위치 설정
        val entities = TestLocations.MULTIPLE_LOCATIONS.map { it.toEntity() }
        fakeDao.setEntities(entities)

        // When: getAllLocations 호출
        val result = dataSource.getAllLocations().first()

        // Then: 모든 위치가 반환됨
        assertEquals(3, result.size)
    }

    @Test
    fun `insertLocation should add new location to DAO`() = runTest {
        // Given: 새 위치
        val location = TestLocations.SEOUL

        // When: insertLocation 호출
        dataSource.insertLocation(location)

        // Then: DAO에 위치가 추가됨
        val stored = fakeDao.getCurrentEntities()
        assertEquals(1, stored.size)
    }

    @Test
    fun `insertLocation should update timestamp for duplicate coordinates`() = runTest {
        // Given: 동일한 좌표의 위치 저장
        dataSource.insertLocation(TestLocations.SEOUL)

        // When: 동일 좌표, 다른 timestamp로 다시 저장
        val updatedLocation = TestLocations.SEOUL_DUPLICATE
        dataSource.insertLocation(updatedLocation)

        // Then: 개수는 1개로 유지되고, timestamp만 업데이트됨
        val stored = fakeDao.getCurrentEntities()
        assertEquals(1, stored.size)
        assertEquals(updatedLocation.timestamp, stored[0].timestamp)
    }

    @Test
    fun `insertLocation should normalize coordinates before duplicate check`() = runTest {
        // Given: 정규화되면 동일한 좌표
        val location1 = TestLocations.LOCATION_WITH_MANY_DECIMALS
        dataSource.insertLocation(location1)

        // When: 정규화된 좌표로 다시 저장
        val normalizedLocation = location1.copy(
            id = location1.id + 1,
            latitude = 37.12346,  // 정규화된 값
            longitude = 127.98765, // 정규화된 값
            timestamp = location1.timestamp + 1000
        )
        dataSource.insertLocation(normalizedLocation)

        // Then: 중복으로 간주되어 1개만 존재 (timestamp 업데이트)
        val stored = fakeDao.getCurrentEntities()
        assertEquals(1, stored.size)
    }

    @Test
    fun `insertLocation should add different locations separately`() = runTest {
        // Given: 서로 다른 위치

        // When: 여러 위치 저장
        dataSource.insertLocation(TestLocations.SEOUL)
        dataSource.insertLocation(TestLocations.BUSAN)
        dataSource.insertLocation(TestLocations.JEJU)

        // Then: 3개 모두 저장됨
        val stored = fakeDao.getCurrentEntities()
        assertEquals(3, stored.size)
    }

    @Test
    fun `insertLocation should handle boundary coordinates`() = runTest {
        // Given: 경계값 좌표

        // When: 경계값 저장
        dataSource.insertLocation(TestLocations.NORTH_POLE)
        dataSource.insertLocation(TestLocations.SOUTH_POLE)

        // Then: 정상 저장됨
        val stored = fakeDao.getCurrentEntities()
        assertEquals(2, stored.size)
    }

    @Test
    fun `getAllLocations should emit updates when data changes`() = runTest {
        // Given: 초기 상태
        val initialLocations = dataSource.getAllLocations().first()
        assertEquals(0, initialLocations.size)

        // When: 위치 추가
        dataSource.insertLocation(TestLocations.SEOUL)
        val afterInsert = dataSource.getAllLocations().first()

        // Then: Flow가 업데이트됨
        assertEquals(1, afterInsert.size)
        assertEquals(TestLocations.SEOUL.latitude, afterInsert[0].latitude, 0.00001)
    }

    @Test
    fun `insertLocation should call DAO insert methods`() = runTest {
        // Given: 위치
        val location = TestLocations.SEOUL

        // When: insertLocation 호출
        dataSource.insertLocation(location)

        // Then: DAO 메서드가 호출됨
        assertEquals(1, fakeDao.findLocationByLatLngCallCount)
        assertEquals(1, fakeDao.insertLocationCallCount)
    }
}

