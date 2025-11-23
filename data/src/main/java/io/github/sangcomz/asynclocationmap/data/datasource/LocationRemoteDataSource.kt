package io.github.sangcomz.asynclocationmap.data.datasource

/**
 * Location Remote Data Source Interface
 *
 * 원격으로 현재 위치를 조회하는 작업을 추상화한 인터페이스입니다.
 *
 * 현재는 WorkManager를 사용하지만, 나중에 다른 방식으로 쉽게 교체할 수 있도록
 * Strategy Pattern을 적용했습니다.
 *
 * 가능한 구현체 예시:
 * - WorkManagerLocationDataSource: WorkManager 기반 구현 (현재)
 * - ForegroundServiceLocationDataSource: Foreground Service 기반 구현 (미래)
 * - AlarmManagerLocationDataSource: AlarmManager 기반 구현 (미래)
 * - PeriodicLocationDataSource: 주기적 위치 업데이트 (미래)
 */
interface LocationRemoteDataSource {

    /**
     * 현재 위치 조회 작업을 요청합니다.
     *
     * 이 메서드는 즉시 반환되며, 실제 위치 조회는 비동기적으로 수행됩니다.
     * 조회된 위치는 자동으로 로컬 데이터베이스에 저장됩니다.
     *
     * 구현체에 따라 다른 메커니즘을 사용할 수 있습니다:
     * - WorkManager: 백그라운드 작업으로 큐에 추가
     * - Foreground Service: 포그라운드 서비스 시작
     * - AlarmManager: 알람 스케줄링
     */
    suspend fun requestLocationUpdate()
}
