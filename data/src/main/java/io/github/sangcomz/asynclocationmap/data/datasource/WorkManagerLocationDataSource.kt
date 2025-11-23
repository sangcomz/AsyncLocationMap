package io.github.sangcomz.asynclocationmap.data.datasource

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.github.sangcomz.asynclocationmap.data.worker.LocationWorker
import javax.inject.Inject

/**
 * WorkManager Location Data Source
 *
 * WorkManager를 사용하여 현재 위치를 조회하는 LocationRemoteDataSource 구현체입니다.
 *
 * WorkManager의 장점:
 * - 앱이 종료되어도 작업 보장
 * - 시스템이 최적의 시점에 실행
 * - Doze 모드에서도 동작
 * - 재시작 후에도 작업 유지
 *
 * @param workManager WorkManager 인스턴스
 */
class WorkManagerLocationDataSource @Inject constructor(
    private val workManager: WorkManager
) : LocationRemoteDataSource {

    /**
     * WorkManager를 통해 LocationWorker를 백그라운드에서 실행합니다.
     *
     * OneTimeWorkRequest를 생성하여 WorkManager 큐에 추가합니다.
     * LocationWorker가 실행되어 현재 위치를 조회하고 데이터베이스에 저장합니다.
     */
    override suspend fun requestLocationUpdate() {
        val workRequest = OneTimeWorkRequestBuilder<LocationWorker>().build()
        workManager.enqueue(workRequest)
    }
}
