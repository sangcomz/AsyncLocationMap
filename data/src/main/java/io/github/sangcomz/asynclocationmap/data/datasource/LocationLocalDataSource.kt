package io.github.sangcomz.asynclocationmap.data.datasource

import io.github.sangcomz.asynclocationmap.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Location Local Data Source Interface
 *
 * 로컬에 위치 정보를 저장하고 조회하는 작업을 추상화한 인터페이스입니다.
 *
 * 현재는 Room Database를 사용하지만, 나중에 다른 방식으로 쉽게 교체할 수 있도록
 * Strategy Pattern을 적용했습니다.
 *
 * 가능한 구현체 예시:
 * - RoomLocationDataSource: Room Database 기반 구현 (현재)
 * - DataStoreLocationDataSource: DataStore 기반 구현 (미래)
 * - SQLDelightLocationDataSource: SQLDelight 기반 구현 (미래)
 * - InMemoryLocationDataSource: 메모리 기반 구현 (테스트용)
 */
interface LocationLocalDataSource {

    /**
     * 모든 위치 정보를 Flow로 반환합니다.
     * Flow를 통해 데이터 변경사항을 실시간으로 관찰할 수 있습니다.
     *
     * @return 위치 정보 리스트의 Flow (최신순으로 정렬됨)
     */
    fun getAllLocations(): Flow<List<Location>>

    /**
     * 위치 정보를 로컬 저장소에 저장합니다.
     *
     * @param location 저장할 위치 정보
     */
    suspend fun insertLocation(location: Location)
}
