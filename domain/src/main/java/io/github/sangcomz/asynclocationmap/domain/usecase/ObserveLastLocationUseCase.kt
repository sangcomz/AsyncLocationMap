package io.github.sangcomz.asynclocationmap.domain.usecase

import io.github.sangcomz.asynclocationmap.domain.model.Location
import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observe Last Location Use Case
 *
 * Room Database에 저장된 최신 위치 정보를 실시간으로 관찰하는 Use Case입니다.
 *
 * 단일 책임 원칙(Single Responsibility Principle)을 따르며,
 * 최신 위치 정보 관찰이라는 하나의 비즈니스 로직만 담당합니다.
 *
 * @property repository 위치 정보 Repository 인터페이스
 */
class ObserveLastLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    /**
     * 최신 위치 정보 리스트를 Flow로 반환합니다.
     * Flow를 사용하여 DB의 변경사항을 실시간으로 관찰할 수 있습니다.
     *
     * @return 위치 정보 리스트의 Flow (최신순으로 정렬됨)
     */
    operator fun invoke(): Flow<List<Location>> {
        return repository.getLocations()
    }
}
