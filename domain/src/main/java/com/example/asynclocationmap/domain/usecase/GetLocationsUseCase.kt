package com.example.asynclocationmap.domain.usecase

import com.example.asynclocationmap.domain.model.Location
import com.example.asynclocationmap.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Get Locations Use Case
 *
 * Room Database에 저장된 모든 위치 정보를 조회하는 Use Case입니다.
 *
 * 단일 책임 원칙(Single Responsibility Principle)을 따르며,
 * 위치 정보 조회라는 하나의 비즈니스 로직만 담당합니다.
 *
 * @property repository 위치 정보 Repository 인터페이스
 */
class GetLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    /**
     * 위치 정보 리스트를 Flow로 반환합니다.
     * Flow를 사용하여 DB의 변경사항을 실시간으로 관찰할 수 있습니다.
     *
     * @return 위치 정보 리스트의 Flow
     */
    operator fun invoke(): Flow<List<Location>> {
        return repository.getLocations()
    }
}
