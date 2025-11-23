package io.github.sangcomz.asynclocationmap.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sangcomz.asynclocationmap.domain.usecase.ObserveLastLocationUseCase
import io.github.sangcomz.asynclocationmap.domain.usecase.RequestLocationUpdateUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Map ViewModel
 *
 * 지도 화면의 비즈니스 로직을 처리하는 ViewModel입니다.
 * @HiltViewModel을 사용하여 Hilt DI를 통해 의존성을 주입받습니다.
 *
 * Single State Pattern을 적용하여 MapUiState 하나로 모든 UI 상태를 관리합니다.
 * UI는 이 StateFlow를 구독하여 상태 변화에 반응합니다.
 *
 * @property observeLastLocationUseCase 위치 정보를 관찰하는 UseCase
 * @property requestLocationUpdateUseCase 현재 위치 조회를 요청하는 UseCase
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val observeLastLocationUseCase: ObserveLastLocationUseCase,
    private val requestLocationUpdateUseCase: RequestLocationUpdateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        observeLocations()
    }

    /**
     * Room Database에서 위치 정보를 관찰합니다.
     * Flow를 통해 데이터베이스 변경사항을 실시간으로 수신하고 UI를 업데이트합니다.
     */
    private fun observeLocations() {
        viewModelScope.launch {
            observeLastLocationUseCase()
                .map { locations ->
                    locations.map { location ->
                        LocationUiModel(
                            id = location.id,
                            latLng = LatLng(location.latitude, location.longitude),
                            timestamp = location.timestamp
                        )
                    }
                }
                .catch { exception ->
                    _uiState.update { it.copy(error = exception.message) }
                }
                .collect { locationUiModels ->
                    _uiState.update {
                        it.copy(
                            locations = locationUiModels,
                            error = null,
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * "현재 위치" 버튼 클릭 시 호출됩니다.
     * WorkManager를 통해 백그라운드에서 위치 조회를 시작합니다.
     *
     * 흐름:
     * 1. isLoading = true 설정
     * 2. RequestLocationUpdateUseCase 호출
     * 3. WorkManager가 LocationWorker를 백그라운드에서 실행
     * 4. LocationWorker가 위치를 조회하고 timestamp와 함께 Room DB에 저장
     * 5. Room DB Flow를 통해 자동으로 UI 업데이트 (locations에 timestamp 포함)
     */
    fun onRequestCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                requestLocationUpdateUseCase()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "위치 조회 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    /**
     * 에러 메시지를 지웁니다.
     * 사용자가 에러 Snackbar를 닫을 때 호출됩니다.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * BottomSheet의 표시 상태를 토글합니다.
     */
    fun toggleBottomSheet() {
        _uiState.update { it.copy(isBottomSheetVisible = !it.isBottomSheetVisible) }
    }

    /**
     * BottomSheet를 숨깁니다.
     */
    fun hideBottomSheet() {
        _uiState.update { it.copy(isBottomSheetVisible = false) }
    }

    /**
     * 위치를 선택하여 지도 카메라를 해당 위치로 이동합니다.
     * BottomSheet의 위치 카드 클릭 시 호출됩니다.
     *
     * @param locationId 선택된 위치의 ID
     */
    fun onLocationSelected(locationId: Long) {
        _uiState.update {
            it.copy(
                selectedLocationId = locationId,
                isBottomSheetVisible = false  // BottomSheet를 닫음
            )
        }
    }

    /**
     * 선택된 위치 상태를 초기화합니다.
     * 카메라 이동 후 호출되어 중복 이동을 방지합니다.
     */
    fun clearSelectedLocation() {
        _uiState.update { it.copy(selectedLocationId = null) }
    }
}
