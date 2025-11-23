package io.github.sangcomz.asynclocationmap.presentation.map

import com.google.android.gms.maps.model.LatLng

/**
 * Location UI Model
 *
 * 지도에 표시할 위치 정보를 담는 UI 모델입니다.
 * Domain의 Location 모델을 UI에서 사용하기 편한 형태로 변환한 것입니다.
 *
 * @property latLng 위도/경도 (Google Maps용)
 * @property timestamp 위치가 기록된 시간 (밀리초)
 */
data class LocationUiModel(
    val latLng: LatLng,
    val timestamp: Long
)
