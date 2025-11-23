package io.github.sangcomz.asynclocationmap.testing.fake

import io.github.sangcomz.asynclocationmap.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake Location DAO
 *
 * 테스트용 LocationDao 구현체입니다.
 * Room의 실제 동작을 모방하여 메모리 기반으로 동작합니다.
 *
 * Note: LocationEntity 대신 Domain Location을 사용합니다 (testing 모듈은 data 모듈에 의존하지 않음)
 */
class FakeLocationDao {

    // 내부적으로 Domain Location 사용 (Entity 대신)
    private val _locations = MutableStateFlow<List<Location>>(emptyList())

    /**
     * 호출 횟수 추적
     */
    var insertLocationCallCount = 0
        private set
    var findLocationByLatLngCallCount = 0
        private set

    /**
     * 예외 발생 설정
     */
    var exceptionToThrow: Exception? = null

    fun getAllLocations(): Flow<List<Location>> {
        return _locations.asStateFlow()
    }

    suspend fun getLastLocation(): Location? {
        return _locations.value.maxByOrNull { it.timestamp }
    }

    suspend fun insertLocation(location: Location) {
        insertLocationCallCount++

        exceptionToThrow?.let { throw it }

        val currentList = _locations.value.toMutableList()

        // ID가 0이면 자동 생성 (Room의 autoGenerate 모방)
        val newLocation = if (location.id == 0L) {
            val newId = (currentList.maxOfOrNull { it.id } ?: 0L) + 1
            location.copy(id = newId)
        } else {
            location
        }

        // 동일 ID가 있으면 업데이트, 없으면 추가
        val existingIndex = currentList.indexOfFirst { it.id == newLocation.id }
        if (existingIndex != -1) {
            currentList[existingIndex] = newLocation
        } else {
            currentList.add(newLocation)
        }

        // timestamp 기준 내림차순 정렬
        _locations.value = currentList.sortedByDescending { it.timestamp }
    }

    suspend fun findLocationByLatLng(latitude: Double, longitude: Double): Location? {
        findLocationByLatLngCallCount++

        return _locations.value.find {
            it.latitude == latitude && it.longitude == longitude
        }
    }

    suspend fun deleteAllLocations() {
        _locations.value = emptyList()
    }

    /**
     * 테스트 헬퍼: 여러 위치를 한 번에 설정
     */
    fun setLocations(locations: List<Location>) {
        _locations.value = locations.sortedByDescending { it.timestamp }
    }

    /**
     * 테스트 헬퍼: 모든 데이터 초기화
     */
    fun clear() {
        _locations.value = emptyList()
        insertLocationCallCount = 0
        findLocationByLatLngCallCount = 0
        exceptionToThrow = null
    }

    /**
     * 테스트 헬퍼: 현재 저장된 위치 리스트 반환
     */
    fun getCurrentLocations(): List<Location> {
        return _locations.value
    }
}

