package io.github.sangcomz.asynclocationmap.data.repository

import io.github.sangcomz.asynclocationmap.testing.data.TestLocations
import io.github.sangcomz.asynclocationmap.testing.fake.FakeLocationLocalDataSource
import io.github.sangcomz.asynclocationmap.testing.fake.FakeLocationRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * LocationRepositoryImpl Unit Test
 *
 * Repository 구현체의 동작을 검증합니다.
 * Fake DataSource들을 사용하여 통합 동작을 테스트합니다.
 */
class LocationRepositoryImplTest {

    private lateinit var localDataSource: FakeLocationLocalDataSource
    private lateinit var remoteDataSource: FakeLocationRemoteDataSource
    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setup() {
        localDataSource = FakeLocationLocalDataSource()
        remoteDataSource = FakeLocationRemoteDataSource()
        repository = LocationRepositoryImpl(localDataSource, remoteDataSource)
    }

    @After
    fun tearDown() {
        localDataSource.clear()
        remoteDataSource.clear()
    }

    @Test
    fun `getLocations should return data from local data source`() = runTest {
        // Given: 로컬 데이터 소스에 위치 설정
        localDataSource.setLocations(TestLocations.MULTIPLE_LOCATIONS)

        // When: getLocations 호출
        val result = repository.getLocations().first()

        // Then: 로컬 데이터가 반환됨
        assertEquals(3, result.size)
        assertEquals(TestLocations.JEJU, result[0]) // 최신순
    }

    @Test
    fun `getLocations should return empty list when no data`() = runTest {
        // Given: 빈 로컬 데이터 소스

        // When: getLocations 호출
        val result = repository.getLocations().first()

        // Then: 빈 리스트 반환
        assertTrue(result.isEmpty())
    }

    @Test
    fun `requestLocationUpdate should call remote data source`() = runTest {
        // Given: 초기 상태
        assertEquals(0, remoteDataSource.requestLocationUpdateCallCount)

        // When: requestLocationUpdate 호출
        repository.requestLocationUpdate()

        // Then: 원격 데이터 소스가 호출됨
        assertEquals(1, remoteDataSource.requestLocationUpdateCallCount)
    }

    @Test
    fun `requestLocationUpdate should be called multiple times`() = runTest {
        // Given: 초기 상태

        // When: 여러 번 호출
        repository.requestLocationUpdate()
        repository.requestLocationUpdate()
        repository.requestLocationUpdate()

        // Then: 모두 원격 데이터 소스에 전달됨
        assertEquals(3, remoteDataSource.requestLocationUpdateCallCount)
    }

    @Test(expected = RuntimeException::class)
    fun `requestLocationUpdate should propagate exception from remote source`() = runTest {
        // Given: 원격 데이터 소스가 예외를 던지도록 설정
        remoteDataSource.exceptionToThrow = RuntimeException("Network error")

        // When & Then: 예외가 전파됨
        repository.requestLocationUpdate()
    }

    @Test
    fun `saveLocation should save to local data source`() = runTest {
        // Given: 저장할 위치
        val location = TestLocations.SEOUL

        // When: saveLocation 호출
        repository.saveLocation(location)

        // Then: 로컬 데이터 소스에 저장됨
        assertEquals(1, localDataSource.insertLocationCallCount)
        val stored = localDataSource.getCurrentLocations()
        assertEquals(1, stored.size)
        assertEquals(location, stored[0])
    }

    @Test
    fun `saveLocation should save multiple locations`() = runTest {
        // Given: 여러 위치

        // When: 여러 위치 저장
        repository.saveLocation(TestLocations.SEOUL)
        repository.saveLocation(TestLocations.BUSAN)
        repository.saveLocation(TestLocations.JEJU)

        // Then: 모두 저장됨
        assertEquals(3, localDataSource.insertLocationCallCount)
        val stored = localDataSource.getCurrentLocations()
        assertEquals(3, stored.size)
    }

    @Test
    fun `getLocations should reflect saved locations`() = runTest {
        // Given: 초기에 빈 상태
        val initial = repository.getLocations().first()
        assertEquals(0, initial.size)

        // When: 위치 저장
        repository.saveLocation(TestLocations.SEOUL)
        val afterSave = repository.getLocations().first()

        // Then: 저장된 위치가 조회됨
        assertEquals(1, afterSave.size)
        assertEquals(TestLocations.SEOUL, afterSave[0])
    }

    @Test
    fun `repository should handle duplicate coordinates through local data source`() = runTest {
        // Given: 동일 좌표 저장
        repository.saveLocation(TestLocations.SEOUL)
        repository.saveLocation(TestLocations.SEOUL_DUPLICATE)

        // When: 조회
        val result = repository.getLocations().first()

        // Then: 로컬 데이터 소스의 중복 처리 로직에 따라 동작
        // (FakeLocalDataSource는 중복을 1개로 유지)
        assertEquals(1, result.size)
        assertEquals(TestLocations.SEOUL_DUPLICATE.timestamp, result[0].timestamp)
    }

    @Test
    fun `getLocations flow should emit updates`() = runTest {
        // Given: 초기 상태

        // When: 첫 번째 조회
        val firstResult = repository.getLocations().first()
        assertEquals(0, firstResult.size)

        // When: 데이터 추가
        repository.saveLocation(TestLocations.SEOUL)

        // Then: Flow가 업데이트됨
        val secondResult = repository.getLocations().first()
        assertEquals(1, secondResult.size)

        // When: 더 추가
        repository.saveLocation(TestLocations.BUSAN)

        // Then: Flow가 다시 업데이트됨
        val thirdResult = repository.getLocations().first()
        assertEquals(2, thirdResult.size)
    }
}

