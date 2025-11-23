package io.github.sangcomz.asynclocationmap.presentation.map

import io.github.sangcomz.asynclocationmap.domain.usecase.ObserveLastLocationUseCase
import io.github.sangcomz.asynclocationmap.domain.usecase.RequestLocationUpdateUseCase
import io.github.sangcomz.asynclocationmap.testing.data.TestLocations
import io.github.sangcomz.asynclocationmap.testing.fake.FakeLocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * MapViewModel Unit Test
 *
 * ViewModel의 상태 관리 및 UI 로직을 검증합니다.
 * 실제 UseCase를 사용하고 Repository만 Fake로 주입하여 테스트합니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeLocationRepository
    private lateinit var observeLastLocationUseCase: ObserveLastLocationUseCase
    private lateinit var requestLocationUpdateUseCase: RequestLocationUpdateUseCase
    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Fake Repository 생성
        fakeRepository = FakeLocationRepository()

        // 실제 UseCase 생성 (Fake Repository 주입)
        observeLastLocationUseCase = ObserveLastLocationUseCase(fakeRepository)
        requestLocationUpdateUseCase = RequestLocationUpdateUseCase(fakeRepository)

        // ViewModel 생성
        viewModel = MapViewModel(observeLastLocationUseCase, requestLocationUpdateUseCase)

        // init 블록의 observeLocations() 완료 대기
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty and not loading`() = runTest {
        // Given: 초기 ViewModel
        advanceUntilIdle() // Flow 수집 완료 대기

        // When: 초기 상태 확인
        val state = viewModel.uiState.value

        // Then: 기본 상태
        assertTrue(state.locations.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isBottomSheetVisible)
        assertNull(state.selectedLocationId)
    }

    @Test
    fun `init should observe locations from use case`() = runTest {
        // Given: Repository에 위치 데이터 설정
        fakeRepository.setLocations(TestLocations.SINGLE_LOCATION)

        // When: ViewModel 생성 (init 블록 실행)
        val newViewModel = MapViewModel(observeLastLocationUseCase, requestLocationUpdateUseCase)
        advanceUntilIdle()

        // Then: 상태가 업데이트됨
        val state = newViewModel.uiState.value
        assertEquals(1, state.locations.size)
    }

    @Test
    fun `onRequestCurrentLocation should call request use case`() = runTest {
        // Given: 초기 상태
        assertEquals(0, fakeRepository.requestLocationUpdateCallCount)

        // When: 위치 요청
        viewModel.onRequestCurrentLocation()
        advanceUntilIdle()

        // Then: Repository의 requestLocationUpdate가 호출됨
        assertEquals(1, fakeRepository.requestLocationUpdateCallCount)
    }

    @Test
    fun `onRequestCurrentLocation should handle error`() = runTest {
        // Given: Repository가 예외를 던지도록 설정
        val errorMessage = "Network error"
        fakeRepository.exceptionToThrow = RuntimeException(errorMessage)

        // When: 위치 요청
        viewModel.onRequestCurrentLocation()
        advanceUntilIdle()

        // Then: 에러 상태로 업데이트
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `onRequestCurrentLocation should clear previous error`() = runTest {
        // Given: 이전 에러가 있는 상태
        fakeRepository.exceptionToThrow = RuntimeException("First error")
        viewModel.onRequestCurrentLocation()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When: 에러 없이 다시 요청
        fakeRepository.exceptionToThrow = null
        viewModel.onRequestCurrentLocation()
        advanceUntilIdle()

        // Then: 에러가 제거됨
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `clearError should remove error message`() = runTest {
        // Given: 에러가 있는 상태
        fakeRepository.exceptionToThrow = RuntimeException("Error")
        viewModel.onRequestCurrentLocation()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When: clearError 호출
        viewModel.clearError()
        advanceUntilIdle()

        // Then: 에러가 제거됨
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `toggleBottomSheet should change visibility`() = runTest {
        // Given: BottomSheet가 닫힌 상태
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isBottomSheetVisible)

        // When: 토글
        viewModel.toggleBottomSheet()
        advanceUntilIdle()

        // Then: 열림
        assertTrue(viewModel.uiState.value.isBottomSheetVisible)

        // When: 다시 토글
        viewModel.toggleBottomSheet()
        advanceUntilIdle()

        // Then: 닫힘
        assertFalse(viewModel.uiState.value.isBottomSheetVisible)
    }

    @Test
    fun `hideBottomSheet should close bottom sheet`() = runTest {
        // Given: BottomSheet가 열린 상태
        viewModel.toggleBottomSheet()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isBottomSheetVisible)

        // When: 닫기
        viewModel.hideBottomSheet()
        advanceUntilIdle()

        // Then: 닫힘
        assertFalse(viewModel.uiState.value.isBottomSheetVisible)
    }

    @Test
    fun `onLocationSelected should set selected location and close bottom sheet`() = runTest {
        // Given: BottomSheet가 열린 상태
        viewModel.toggleBottomSheet()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isBottomSheetVisible)

        // When: 위치 선택
        val locationId = 123L
        viewModel.onLocationSelected(locationId)
        advanceUntilIdle()

        // Then: 선택된 ID가 설정되고 BottomSheet가 닫힘
        val state = viewModel.uiState.value
        assertEquals(locationId, state.selectedLocationId)
        assertFalse(state.isBottomSheetVisible)
    }

    @Test
    fun `clearSelectedLocation should reset selected location`() = runTest {
        // Given: 위치가 선택된 상태
        viewModel.onLocationSelected(123L)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.selectedLocationId)

        // When: 선택 해제
        viewModel.clearSelectedLocation()
        advanceUntilIdle()

        // Then: 선택된 위치가 제거됨
        assertNull(viewModel.uiState.value.selectedLocationId)
    }

    @Test
    fun `locations should be mapped to LocationUiModel`() = runTest {
        // Given: Repository에 위치 데이터 설정
        fakeRepository.setLocations(TestLocations.MULTIPLE_LOCATIONS)

        // When: ViewModel 생성
        val newViewModel = MapViewModel(observeLastLocationUseCase, requestLocationUpdateUseCase)
        advanceUntilIdle()

        // Then: LocationUiModel로 변환됨
        val state = newViewModel.uiState.value
        assertEquals(3, state.locations.size)

        // 첫 번째 위치 확인 (최신순이므로 JEJU)
        val firstLocation = state.locations[0]
        assertEquals(TestLocations.JEJU.id, firstLocation.id)
        assertEquals(TestLocations.JEJU.latitude, firstLocation.latLng.latitude, 0.0001)
        assertEquals(TestLocations.JEJU.longitude, firstLocation.latLng.longitude, 0.0001)
        assertEquals(TestLocations.JEJU.timestamp, firstLocation.timestamp)
    }
}

