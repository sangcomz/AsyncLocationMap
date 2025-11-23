package io.github.sangcomz.asynclocationmap.testing.fake

import io.github.sangcomz.asynclocationmap.data.datasource.LocationRemoteDataSource

/**
 * Fake Location Remote Data Source
 *
 * 테스트용 LocationRemoteDataSource 구현체입니다.
 * 실제 작업을 수행하지 않고 호출 횟수만 추적합니다.
 */
class FakeLocationRemoteDataSource : LocationRemoteDataSource {

    /**
     * requestLocationUpdate 호출 횟수
     */
    var requestLocationUpdateCallCount = 0
        private set

    /**
     * 호출 시 발생시킬 예외
     */
    var exceptionToThrow: Exception? = null

    override suspend fun requestLocationUpdate() {
        requestLocationUpdateCallCount++

        exceptionToThrow?.let { throw it }
    }

    /**
     * 테스트 헬퍼: 상태 초기화
     */
    fun clear() {
        requestLocationUpdateCallCount = 0
        exceptionToThrow = null
    }
}

