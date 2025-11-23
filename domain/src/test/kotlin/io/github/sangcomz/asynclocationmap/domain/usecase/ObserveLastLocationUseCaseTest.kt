package io.github.sangcomz.asynclocationmap.domain.usecase

import io.github.sangcomz.asynclocationmap.domain.fake.FakeLocationRepository
import io.github.sangcomz.asynclocationmap.domain.fake.TestLocations
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ObserveLastLocationUseCase Unit Test
 *
 * Flow를 반환하는 UseCase의 동작을 검증합니다.
 * 간단한 방식(first(), toList())으로 Flow를 테스트합니다.
 */
class ObserveLastLocationUseCaseTest {

    private lateinit var repository: FakeLocationRepository
    private lateinit var useCase: ObserveLastLocationUseCase

    @Before
    fun setup() {
        repository = FakeLocationRepository()
        useCase = ObserveLastLocationUseCase(repository)
    }

    @After
    fun tearDown() {
        repository.clear()
    }

    @Test
    fun `invoke should return empty list when no locations`() = runTest {
        // Given: 빈 repository

        // When: UseCase 호출
        val result = useCase().first()

        // Then: 빈 리스트 반환
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should return single location`() = runTest {
        // Given: 단일 위치 설정
        repository.setLocations(TestLocations.SINGLE_LOCATION)

        // When: UseCase 호출
        val result = useCase().first()

        // Then: 1개의 위치 반환
        assertEquals(1, result.size)
        assertEquals(TestLocations.SEOUL, result[0])
    }

    @Test
    fun `invoke should return multiple locations sorted by timestamp desc`() = runTest {
        // Given: 여러 위치 설정
        repository.setLocations(TestLocations.MULTIPLE_LOCATIONS)

        // When: UseCase 호출
        val result = useCase().first()

        // Then: 3개의 위치가 최신순으로 반환
        assertEquals(3, result.size)
        assertEquals(TestLocations.JEJU, result[0]) // 가장 최신
        assertEquals(TestLocations.BUSAN, result[1])
        assertEquals(TestLocations.SEOUL, result[2]) // 가장 오래됨
    }

    @Test
    fun `invoke should return all test locations`() = runTest {
        // Given: 모든 테스트 데이터 설정
        repository.setLocations(TestLocations.ALL_LOCATIONS)

        // When: UseCase 호출
        val result = useCase().first()

        // Then: 모든 위치 반환
        assertEquals(TestLocations.ALL_LOCATIONS.size, result.size)
    }

    @Test
    fun `invoke should reflect repository changes`() = runTest {
        // Given: 초기에 빈 repository
        val initialResult = useCase().first()
        assertEquals(0, initialResult.size)

        // When: 위치 추가
        repository.saveLocation(TestLocations.SEOUL)
        val afterAddResult = useCase().first()

        // Then: 추가된 위치가 반환됨
        assertEquals(1, afterAddResult.size)
        assertEquals(TestLocations.SEOUL, afterAddResult[0])
    }
}
