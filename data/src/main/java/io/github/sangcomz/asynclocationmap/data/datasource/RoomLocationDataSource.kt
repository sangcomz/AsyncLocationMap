package io.github.sangcomz.asynclocationmap.data.datasource

import io.github.sangcomz.asynclocationmap.data.local.dao.LocationDao
import io.github.sangcomz.asynclocationmap.data.mapper.toDomain
import io.github.sangcomz.asynclocationmap.data.mapper.toEntity
import io.github.sangcomz.asynclocationmap.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room Location Data Source
 *
 * Room Database를 사용하여 위치 정보를 저장하고 조회하는 LocationLocalDataSource 구현체입니다.
 *
 * Room의 장점:
 * - SQL 쿼리의 컴파일 타임 검증
 * - Flow를 통한 reactive 데이터 스트림
 * - 타입 안정성
 * - 마이그레이션 지원
 *
 * @param locationDao Room Database의 DAO
 */
class RoomLocationDataSource @Inject constructor(
    private val locationDao: LocationDao
) : LocationLocalDataSource {

    /**
     * Room Database에서 모든 위치 정보를 Flow로 반환합니다.
     * Entity를 Domain Model로 변환하여 반환합니다.
     *
     * @return 위치 정보 리스트의 Flow
     */
    override fun getAllLocations(): Flow<List<Location>> {
        return locationDao.getAllLocations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * 위치 정보를 Room Database에 저장합니다.
     * Domain Model을 Entity로 변환하여 저장합니다.
     *
     * @param location 저장할 위치 정보
     */
    override suspend fun insertLocation(location: Location) {
        locationDao.insertLocation(location.toEntity())
    }
}
