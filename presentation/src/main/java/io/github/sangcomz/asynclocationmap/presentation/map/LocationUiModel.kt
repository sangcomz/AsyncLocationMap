package io.github.sangcomz.asynclocationmap.presentation.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Location UI Model
 *
 * 지도에 표시할 위치 정보를 담는 UI 모델입니다.
 * Domain의 Location 모델을 UI에서 사용하기 편한 형태로 변환한 것입니다.
 * ClusterItem을 구현하여 마커 클러스터링 기능을 지원합니다.
 *
 * @property latLng 위도/경도 (Google Maps용)
 * @property timestamp 위치가 기록된 시간 (밀리초)
 */
data class LocationUiModel(
    val latLng: LatLng,
    val timestamp: Long
) : ClusterItem {
    override fun getPosition(): LatLng = latLng
    override fun getTitle(): String = "위치"
    override fun getSnippet(): String = "위도: ${latLng.latitude}, 경도: ${latLng.longitude}"
    override fun getZIndex(): Float = 0f
}
