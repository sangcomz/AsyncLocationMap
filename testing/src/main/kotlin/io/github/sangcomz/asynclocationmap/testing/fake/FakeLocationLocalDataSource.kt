package io.github.sangcomz.asynclocationmap.testing.fake

import io.github.sangcomz.asynclocationmap.data.datasource.LocationLocalDataSource
import io.github.sangcomz.asynclocationmap.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake Location Local Data Source
 *
 * 테스트용 LocationLocalDataSource 구현체입니다.
 * 메모리 기반 리스트로 데이터를 관리하며, 중복 검사 로직을 포함합니다.
 */
class FakeLocationLocalDataSource : LocationLocalDataSource {

    private val _locations = MutableStateFlow<List<Location>>(emptyList())

    /**
     * insertLocation 호출 횟수
     */
    var insertLocationCallCount = 0
        private set

    /**
     * 저장 시 발생시킬 예외
     */
    var exceptionToThrow: Exception? = null

    override fun getAllLocations(): Flow<List<Location>> {
        return _locations.asStateFlow()
    }

    override suspend fun insertLocation(location: Location) {
        insertLocationCallCount++

        exceptionToThrow?.let { throw it }

        val currentList = _locations.value.toMutableList()

        // 정규화된 좌표로 중복 검사 (소수점 5자리)
        val normalizedLat = normalizeCoordinate(location.latitude)
        val normalizedLng = normalizeCoordinate(location.longitude)

        val existingIndex = currentList.indexOfFirst {
            normalizeCoordinate(it.latitude) == normalizedLat &&
            normalizeCoordinate(it.longitude) == normalizedLng
        }

        if (existingIndex != -1) {
            // 중복 좌표인 경우 timestamp만 업데이트
            currentList[existingIndex] = currentList[existingIndex].copy(
                timestamp = location.timestamp
            )
        } else {
            // 새로운 위치 추가
            currentList.add(location)
        }

        // 최신순 정렬
        _locations.value = currentList.sortedByDescending { it.timestamp }
    }

    /**
     * 좌표 정규화 (소수점 5자리)
     */
    private fun normalizeCoordinate(value: Double): Double {
        val precision = 1e5
        return kotlin.math.round(value * precision) / precision
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
        exceptionToThrow = null
    }

    /**
     * 테스트 헬퍼: 현재 저장된 위치 리스트 반환
     */
    fun getCurrentLocations(): List<Location> {
        return _locations.value
    }
}

