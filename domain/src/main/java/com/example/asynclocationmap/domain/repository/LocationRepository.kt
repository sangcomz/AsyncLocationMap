package com.example.asynclocationmap.domain.repository

import com.example.asynclocationmap.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Location Repository Interface
 *
 * 위치 정보 관련 데이터 접근을 추상화한 인터페이스입니다.
 * Data Layer에서 이 인터페이스를 구현하며, Domain과 Presentation Layer에서 사용합니다.
 *
 * 의존성 역전 원칙(Dependency Inversion Principle)을 따릅니다.
 */
interface LocationRepository {

    /**
     * WorkManager를 통해 현재 위치 조회 작업을 요청합니다.
     * 이 함수는 즉시 반환되며, 실제 위치 조회는 백그라운드에서 비동기적으로 수행됩니다.
     */
    suspend fun requestLocationUpdate()

    /**
     * Room Database에 저장된 모든 위치 정보를 Flow로 반환합니다.
     * Flow를 사용하여 DB 변경 사항을 실시간으로 관찰할 수 있습니다.
     *
     * @return 위치 정보 리스트의 Flow (최신순으로 정렬됨)
     */
    fun getLocations(): Flow<List<Location>>

    /**
     * 위치 정보를 Room Database에 저장합니다.
     *
     * @param location 저장할 위치 정보
     */
    suspend fun saveLocation(location: Location)
}
