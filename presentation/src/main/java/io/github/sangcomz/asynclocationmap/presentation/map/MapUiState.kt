package io.github.sangcomz.asynclocationmap.presentation.map

import com.google.android.gms.maps.model.LatLng

/**
 * Map UI State
 *
 * 지도 화면의 모든 상태를 관리하는 UiState입니다.
 * Single State Pattern을 적용하여 화면의 모든 상태를 하나의 객체로 관리합니다.
 *
 * @property locations 지도에 표시할 위치 리스트
 * @property currentLocation 현재 위치 (카메라 이동에 사용)
 * @property isLoading 위치 조회 중인지 여부
 * @property error 에러 메시지 (null이면 에러 없음)
 * @property hasLocationPermission 위치 권한 보유 여부
 */
data class MapUiState(
    val locations: List<LatLng> = emptyList(),
    val currentLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLocationPermission: Boolean = false
)
