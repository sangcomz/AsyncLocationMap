package io.github.sangcomz.asynclocationmap.data.di

import io.github.sangcomz.asynclocationmap.data.repository.LocationRepositoryImpl
import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Module
 *
 * Repository 인터페이스와 구현체를 바인딩하는 Hilt Module입니다.
 * @Binds를 사용하여 인터페이스를 구현체에 연결합니다.
 *
 * 의존성 역전 원칙(DIP)을 따르며, Domain Layer의 인터페이스를
 * Data Layer의 구현체에 바인딩합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * LocationRepository 인터페이스를 LocationRepositoryImpl 구현체에 바인딩합니다.
     * Singleton으로 관리되어 앱 전체에서 하나의 인스턴스만 존재합니다.
     *
     * @param impl LocationRepositoryImpl 구현체
     * @return LocationRepository 인터페이스
     */
    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository
}
