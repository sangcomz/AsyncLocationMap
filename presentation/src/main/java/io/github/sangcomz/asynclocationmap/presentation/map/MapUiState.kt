package io.github.sangcomz.asynclocationmap.presentation.map

import com.google.android.gms.maps.model.LatLng

/**
 * Map UI State
 *
 * 지도 화면의 모든 상태를 관리하는 UiState입니다.
 * Single State Pattern을 적용하여 화면의 모든 상태를 하나의 객체로 관리합니다.
 *
 * @property locations 지도에 표시할 위치 리스트
 * @property fetchedCurrentLocation 현재 위치 버튼을 통해 받아온 현재 위치
 * @property isLoading 위치 조회 중인지 여부
 * @property error 에러 메시지 (null이면 에러 없음)
 * @property hasLocationPermission 위치 권한 보유 여부
 */
data class MapUiState(
    val locations: List<LatLng> = emptyList(),
    val fetchedCurrentLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLocationPermission: Boolean = false
)
