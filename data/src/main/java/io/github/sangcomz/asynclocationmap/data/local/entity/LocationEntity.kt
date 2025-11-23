package io.github.sangcomz.asynclocationmap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity for Location
 *
 * Room Database에 저장되는 위치 정보 테이블입니다.
 * Domain Model과 분리하여 데이터베이스 구조를 독립적으로 관리합니다.
 *
 * @property id 위치 정보의 고유 ID (자동 생성)
 * @property latitude 위도
 * @property longitude 경도
 * @property timestamp 위치 정보가 기록된 시간 (milliseconds)
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
