package io.github.sangcomz.asynclocationmap.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
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

    // 백그라운드 위치 권한 상태 관리 (Android 10 이상)
    val backgroundLocationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

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
            uiState.locations.firstOrNull()?.latLng ?: com.google.android.gms.maps.model.LatLng(DEFAULT_LAT, DEFAULT_LNG), // 기본값: 서울
            DEFAULT_ZOOM
        )
    }

    // 가장 최근 위치의 timestamp를 사용하여 카메라 이동
    // 동일한 위치라도 timestamp가 변경되므로 카메라 이동이 트리거됨
    LaunchedEffect(uiState.locations.firstOrNull()?.timestamp) {
        uiState.locations.firstOrNull()?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location.latLng, DEFAULT_ZOOM),
                durationMs = 1000
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 로딩 중이 아닐 때만 클릭 가능
                    if (!uiState.isLoading) {
                        when {
                            // 위치 권한이 없으면 위치 권한 요청
                            !locationPermissionState.status.isGranted -> {
                                locationPermissionState.launchPermissionRequest()
                            }
                            // 위치 권한은 있지만 백그라운드 권한이 없으면 백그라운드 권한 요청
                            !backgroundLocationPermissionState.status.isGranted -> {
                                backgroundLocationPermissionState.launchPermissionRequest()
                            }
                            // 모든 권한이 있으면 위치 조회 시작
                            else -> {
                                viewModel.onRequestCurrentLocation()
                            }
                        }
                    }
                }
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 백그라운드 위치 권한 경고 배너
            if (locationPermissionState.status.isGranted && !backgroundLocationPermissionState.status.isGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "경고",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "백그라운드 위치 권한 필요",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "앱이 백그라운드에 있을 때 위치를 가져올 수 없습니다",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                backgroundLocationPermissionState.launchPermissionRequest()
                            }
                        ) {
                            Text("허용")
                        }
                    }
                }
            }

            // Google Map
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // 저장된 모든 위치에 마커 표시
                    uiState.locations.forEach { locationUiModel ->
                        Marker(
                            state = MarkerState(position = locationUiModel.latLng),
                            title = "위치",
                            snippet = "위도: ${locationUiModel.latLng.latitude}, 경도: ${locationUiModel.latLng.longitude}"
                        )
                    }
                }
            }
        }
    }
}
