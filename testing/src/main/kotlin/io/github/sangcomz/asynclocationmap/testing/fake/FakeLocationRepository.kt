package io.github.sangcomz.asynclocationmap.testing.fake

import io.github.sangcomz.asynclocationmap.domain.model.Location
import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake Location Repository
 *
 * 테스트용 LocationRepository 구현체입니다.
 * 메모리 기반으로 동작하며, MutableStateFlow를 사용하여 실시간 데이터 변경을 지원합니다.
 */
class FakeLocationRepository : LocationRepository {

    private val _locations = MutableStateFlow<List<Location>>(emptyList())

    /**
     * requestLocationUpdate 호출 횟수
     */
    var requestLocationUpdateCallCount = 0
        private set

    /**
     * requestLocationUpdate 호출 시 추가할 위치
     * 테스트에서 설정할 수 있습니다.
     */
    var locationToAdd: Location? = null

    /**
     * requestLocationUpdate 호출 시 발생시킬 예외
     * 테스트에서 설정할 수 있습니다.
     */
    var exceptionToThrow: Exception? = null

    override suspend fun requestLocationUpdate() {
        requestLocationUpdateCallCount++

        exceptionToThrow?.let { throw it }

        locationToAdd?.let { location ->
            saveLocation(location)
        }
    }

    override fun getLocations(): Flow<List<Location>> {
        return _locations.asStateFlow()
    }

    override suspend fun saveLocation(location: Location) {
        val currentList = _locations.value.toMutableList()

        // 동일한 ID가 있으면 업데이트, 없으면 추가
        val existingIndex = currentList.indexOfFirst { it.id == location.id }
        if (existingIndex != -1) {
            currentList[existingIndex] = location
        } else {
            currentList.add(location)
        }

        // 최신순 정렬 (timestamp 기준 내림차순)
        _locations.value = currentList.sortedByDescending { it.timestamp }
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
        requestLocationUpdateCallCount = 0
        locationToAdd = null
        exceptionToThrow = null
    }

    /**
     * 테스트 헬퍼: 현재 저장된 위치 리스트 반환
     */
    fun getCurrentLocations(): List<Location> {
        return _locations.value
    }
}

