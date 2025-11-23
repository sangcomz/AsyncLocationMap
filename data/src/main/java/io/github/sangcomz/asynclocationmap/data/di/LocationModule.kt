package io.github.sangcomz.asynclocationmap.data.di

import android.content.Context
import androidx.work.WorkManager
import io.github.sangcomz.asynclocationmap.data.datasource.LocationLocalDataSource
import io.github.sangcomz.asynclocationmap.data.datasource.LocationRemoteDataSource
import io.github.sangcomz.asynclocationmap.data.datasource.RoomLocationDataSource
import io.github.sangcomz.asynclocationmap.data.datasource.WorkManagerLocationDataSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Location Module
 *
 * 위치 관련 의존성을 제공하는 Hilt Module입니다.
 * FusedLocationProviderClient, WorkManager, LocationRemoteDataSource를 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    /**
     * FusedLocationProviderClient 인스턴스를 제공합니다.
     * Google Play Services의 위치 서비스 클라이언트입니다.
     *
     * @param context Application Context
     * @return FusedLocationProviderClient 인스턴스
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * WorkManager 인스턴스를 제공합니다.
     * 백그라운드 작업을 관리하는 WorkManager입니다.
     *
     * @param context Application Context
     * @return WorkManager 인스턴스
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}

/**
 * Location Data Source Module
 *
 * Location 관련 Data Source의 구현체를 바인딩하는 Hilt Module입니다.
 * Strategy Pattern을 적용하여 구현체를 쉽게 교체할 수 있습니다.
 *
 * Remote Data Source:
 * - 현재: WorkManagerLocationDataSource (WorkManager 기반)
 * - 미래: ForegroundService, AlarmManager 등으로 교체 가능
 *
 * Local Data Source:
 * - 현재: RoomLocationDataSource (Room Database 기반)
 * - 미래: DataStore, SQLDelight 등으로 교체 가능
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocationDataSourceModule {

    /**
     * LocationRemoteDataSource 인터페이스를
     * WorkManagerLocationDataSource 구현체에 바인딩합니다.
     *
     * 다른 구현체로 교체하려면 이 메서드만 수정하면 됩니다.
     *
     * @param impl WorkManagerLocationDataSource 구현체
     * @return LocationRemoteDataSource 인터페이스
     */
    @Binds
    @Singleton
    abstract fun bindLocationRemoteDataSource(
        impl: WorkManagerLocationDataSource
    ): LocationRemoteDataSource

    /**
     * LocationLocalDataSource 인터페이스를
     * RoomLocationDataSource 구현체에 바인딩합니다.
     *
     * 다른 구현체로 교체하려면 이 메서드만 수정하면 됩니다.
     *
     * @param impl RoomLocationDataSource 구현체
     * @return LocationLocalDataSource 인터페이스
     */
    @Binds
    @Singleton
    abstract fun bindLocationLocalDataSource(
        impl: RoomLocationDataSource
    ): LocationLocalDataSource
}
