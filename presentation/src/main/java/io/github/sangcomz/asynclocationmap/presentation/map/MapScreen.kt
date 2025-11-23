package io.github.sangcomz.asynclocationmap.presentation.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Map Screen
 *
 * 지도를 표시하고 현재 위치를 마커로 표시하는 메인 화면입니다.
 *
 * 구성 요소:
 * - GoogleMap: 지도 뷰 (Google Maps Compose)
 * - Marker: 위치 마커들
 * - FloatingActionButton: "현재 위치" 버튼
 * - Snackbar: 에러 메시지 표시
 * - CircularProgressIndicator: 로딩 인디케이터
 *
 * 위치 권한:
 * - Accompanist Permissions를 사용하여 위치 권한 요청
 * - 버튼 클릭 시 권한이 없으면 자동으로 권한 요청 다이얼로그 표시
 *
 * @param viewModel MapViewModel (Hilt를 통해 자동 주입)
 */

private const val DEFAULT_LAT = 37.5665
private const val DEFAULT_LNG = 126.9780
private const val DEFAULT_ZOOM = 15f

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 위치 권한 상태 관리
    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되면 즉시 위치 조회 시작
            viewModel.onRequestCurrentLocation()
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // 카메라 위치 상태 (현재 위치로 이동하기 위해 사용)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.lastCurrentLocation ?: com.google.android.gms.maps.model.LatLng(DEFAULT_LAT, DEFAULT_LNG), // 기본값: 서울
            15f
        )
    }

    // 현재 위치가 변경되면 카메라 이동
    LaunchedEffect(uiState.lastCurrentLocation) {
        uiState.lastCurrentLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, DEFAULT_ZOOM)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (locationPermissionState.status.isGranted) {
                        // 권한이 있으면 위치 조회 시작
                        viewModel.onRequestCurrentLocation()
                    } else {
                        // 권한이 없으면 권한 요청
                        locationPermissionState.launchPermissionRequest()
                    }
                }
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "현재 위치"
                    )
                }
            }
        }
    ) { paddingValues ->
        // Google Map
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState
        ) {
            // 저장된 모든 위치에 마커 표시
            uiState.locations.forEach { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "위치",
                    snippet = "위도: ${location.latitude}, 경도: ${location.longitude}"
                )
            }
        }
    }
}
