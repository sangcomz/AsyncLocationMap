package io.github.sangcomz.asynclocationmap.domain.usecase

import io.github.sangcomz.asynclocationmap.domain.fake.FakeLocationRepository
import io.github.sangcomz.asynclocationmap.domain.fake.TestLocations
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * RequestLocationUpdateUseCase Unit Test
 *
 * 위치 업데이트 요청 UseCase의 동작을 검증합니다.
 */
class RequestLocationUpdateUseCaseTest {

    private lateinit var repository: FakeLocationRepository
    private lateinit var useCase: RequestLocationUpdateUseCase

    @Before
    fun setup() {
        repository = FakeLocationRepository()
        useCase = RequestLocationUpdateUseCase(repository)
    }

    @After
    fun tearDown() {
        repository.clear()
    }

    @Test
    fun `invoke should call repository requestLocationUpdate`() = runTest {
        // Given: 초기 상태
        assertEquals(0, repository.requestLocationUpdateCallCount)

        // When: UseCase 호출
        useCase()

        // Then: Repository의 requestLocationUpdate가 호출됨
        assertEquals(1, repository.requestLocationUpdateCallCount)
    }

    @Test
    fun `invoke should be called multiple times`() = runTest {
        // Given: 초기 상태

        // When: UseCase를 3번 호출
        useCase()
        useCase()
        useCase()

        // Then: 3번 호출됨
        assertEquals(3, repository.requestLocationUpdateCallCount)
    }

    @Test
    fun `invoke should add location when repository has locationToAdd configured`() = runTest {
        // Given: Repository에 추가할 위치 설정
        repository.locationToAdd = TestLocations.SEOUL

        // When: UseCase 호출
        useCase()

        // Then: 위치가 추가됨
        val locations = repository.getCurrentLocations()
        assertEquals(1, locations.size)
        assertEquals(TestLocations.SEOUL, locations[0])
    }

    @Test
    fun `invoke should add multiple locations on multiple calls`() = runTest {
        // Given: Repository에 추가할 위치 설정
        repository.locationToAdd = TestLocations.SEOUL

        // When: 첫 번째 호출
        useCase()

        // Then: 첫 번째 위치 추가됨
        assertEquals(1, repository.getCurrentLocations().size)

        // When: 다른 위치로 변경하고 두 번째 호출
        repository.locationToAdd = TestLocations.BUSAN
        useCase()

        // Then: 두 번째 위치도 추가됨
        assertEquals(2, repository.getCurrentLocations().size)
    }

    @Test(expected = RuntimeException::class)
    fun `invoke should throw exception when repository throws`() = runTest {
        // Given: Repository가 예외를 던지도록 설정
        repository.exceptionToThrow = RuntimeException("Network error")

        // When & Then: UseCase 호출 시 예외 발생
        useCase()
    }

    @Test
    fun `invoke should propagate exception message`() = runTest {
        // Given: Repository가 예외를 던지도록 설정
        val errorMessage = "Location service unavailable"
        repository.exceptionToThrow = RuntimeException(errorMessage)

        // When & Then: 예외를 잡아서 메시지 확인
        try {
            useCase()
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals(errorMessage, e.message)
        }
    }
}
