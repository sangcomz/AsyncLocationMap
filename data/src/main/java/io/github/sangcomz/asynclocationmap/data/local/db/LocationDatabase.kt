package io.github.sangcomz.asynclocationmap.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.sangcomz.asynclocationmap.data.local.dao.LocationDao
import io.github.sangcomz.asynclocationmap.data.local.entity.LocationEntity

/**
 * Location Room Database
 *
 * 위치 정보를 저장하는 Room Database입니다.
 *
 * @property locationDao 위치 정보 DAO
 */
@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    /**
     * LocationDao 인스턴스를 제공합니다.
     *
     * @return LocationDao
     */
    abstract fun locationDao(): LocationDao
}
