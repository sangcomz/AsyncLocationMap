package io.github.sangcomz.asynclocationmap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.sangcomz.asynclocationmap.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Location Data Access Object (DAO)
 *
 * Room Database의 위치 정보 테이블에 접근하는 인터페이스입니다.
 * Flow를 사용하여 데이터베이스 변경사항을 실시간으로 관찰할 수 있습니다.
 */
@Dao
interface LocationDao {

    /**
     * 모든 위치 정보를 최신순으로 조회합니다.
     * Flow를 반환하여 데이터베이스 변경사항을 자동으로 관찰합니다.
     *
     * @return 위치 정보 리스트의 Flow (timestamp 기준 내림차순 정렬)
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    /**
     * 가장 최근 위치 정보를 조회합니다.
     *
     * @return 가장 최근 위치 정보 (없으면 null)
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLocation(): LocationEntity?

    /**
     * 위치 정보를 데이터베이스에 삽입합니다.
     *
     * @param location 저장할 위치 정보
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    /**
     * 특정 위치 정보를 검색합니다.
     *
     * @param latitude 검색할 위도
     * @param longitude 검색할 경도
     */
    @Query("SELECT * FROM locations WHERE latitude = :latitude AND longitude = :longitude")
    suspend fun findLocationByLatLng(latitude: Double, longitude: Double): LocationEntity?

    /**
     * 모든 위치 정보를 삭제합니다.
     * 테스트나 데이터 초기화 시 사용됩니다.
     */
    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
}
