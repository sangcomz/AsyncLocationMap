package io.github.sangcomz.asynclocationmap.data.repository

import io.github.sangcomz.asynclocationmap.data.datasource.LocationRemoteDataSource
import io.github.sangcomz.asynclocationmap.data.local.dao.LocationDao
import io.github.sangcomz.asynclocationmap.data.mapper.toDomain
import io.github.sangcomz.asynclocationmap.data.mapper.toEntity
import io.github.sangcomz.asynclocationmap.domain.model.Location
import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Location Repository Implementation
 *
 * LocationRepository 인터페이스의 구현체입니다.
 * Local Data Source(Room)와 Remote Data Source(위치 조회)를 조합하여
 * 위치 정보를 관리합니다.
 *
 * 의존성 역전 원칙(DIP)을 따르며, Domain Layer의 인터페이스를 구현합니다.
 * Strategy Pattern을 적용하여 Remote Data Source를 쉽게 교체할 수 있습니다.
 *
 * @param locationDao Local Data Source (Room Database DAO)
 * @param remoteDataSource Remote Data Source (위치 조회 전략, 현재는 WorkManager)
 */
class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val remoteDataSource: LocationRemoteDataSource
) : LocationRepository {

    /**
     * Remote Data Source를 통해 현재 위치 조회 작업을 요청합니다.
     *
     * Remote Data Source의 구현체에 따라 다른 방식으로 위치를 조회합니다:
     * - WorkManagerLocationDataSource: WorkManager를 통한 백그라운드 작업
     * - 미래: ForegroundService, AlarmManager 등 다른 방식으로 교체 가능
     */
    override suspend fun requestLocationUpdate() {
        remoteDataSource.requestLocationUpdate()
    }

    /**
     * Room Database에서 모든 위치 정보를 Flow로 반환합니다.
     * Entity를 Domain Model로 변환하여 반환합니다.
     *
     * @return 위치 정보 리스트의 Flow
     */
    override fun getLocations(): Flow<List<Location>> {
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
    override suspend fun saveLocation(location: Location) {
        locationDao.insertLocation(location.toEntity())
    }
}
