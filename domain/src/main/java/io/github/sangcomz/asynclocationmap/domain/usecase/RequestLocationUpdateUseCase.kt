package io.github.sangcomz.asynclocationmap.domain.usecase

import io.github.sangcomz.asynclocationmap.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Request Location Update Use Case
 *
 * WorkManager를 통해 현재 위치 조회를 요청하는 Use Case입니다.
 *
 * 단일 책임 원칙(Single Responsibility Principle)을 따르며,
 * 위치 업데이트 요청이라는 하나의 비즈니스 로직만 담당합니다.
 *
 * 이 Use Case를 호출하면:
 * 1. WorkManager가 LocationWorker를 백그라운드에서 실행
 * 2. LocationWorker가 FusedLocationProviderClient로 현재 위치 조회
 * 3. 조회된 위치를 Room Database에 저장
 * 4. Room의 Flow를 통해 UI가 자동으로 업데이트
 *
 * @property repository 위치 정보 Repository 인터페이스
 */
class RequestLocationUpdateUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    /**
     * 위치 업데이트를 요청합니다.
     * 이 함수는 즉시 반환되며, 실제 위치 조회는 백그라운드에서 비동기적으로 수행됩니다.
     */
    suspend operator fun invoke() {
        repository.requestLocationUpdate()
    }
}
