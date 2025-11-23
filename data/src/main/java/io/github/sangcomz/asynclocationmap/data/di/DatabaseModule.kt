package io.github.sangcomz.asynclocationmap.data.di

import android.content.Context
import androidx.room.Room
import io.github.sangcomz.asynclocationmap.data.datasource.LocationLocalDataSource
import io.github.sangcomz.asynclocationmap.data.datasource.RoomLocationDataSource
import io.github.sangcomz.asynclocationmap.data.local.dao.LocationDao
import io.github.sangcomz.asynclocationmap.data.local.db.LocationDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Module
 *
 * Room Database와 DAO를 제공하는 Hilt Module입니다.
 * SingletonComponent에 설치되어 앱 전체에서 단일 인스턴스를 공유합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * LocationDatabase 인스턴스를 제공합니다.
     * Singleton으로 관리되어 앱 전체에서 하나의 인스턴스만 존재합니다.
     *
     * @param context Application Context
     * @return LocationDatabase 인스턴스
     */
    @Provides
    @Singleton
    fun provideLocationDatabase(
        @ApplicationContext context: Context
    ): LocationDatabase {
        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            "location_database"
        ).build()
    }

    /**
     * LocationDao 인스턴스를 제공합니다.
     *
     * @param database LocationDatabase 인스턴스
     * @return LocationDao 인스턴스
     */
    @Provides
    fun provideLocationDao(database: LocationDatabase): LocationDao {
        return database.locationDao()
    }
}

