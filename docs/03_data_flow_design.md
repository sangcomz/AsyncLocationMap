# 데이터 흐름 및 컴포넌트 설계

## 1. 전체 데이터 흐름

```
사용자 액션: "현재 위치" 버튼 클릭
    ↓
MapScreen (권한 확인)
    ↓ (권한 있음)
MapViewModel.requestLocationUpdate()
    ↓
RequestLocationUpdateUseCase
    ↓
LocationRepository.requestLocationUpdate()
    ↓
WorkManager.enqueue(LocationWorker)
    ↓
[백그라운드 실행]
LocationWorker.doWork()
    ↓
FusedLocationProviderClient (실제 위치 조회)
    ↓
LocationRepository.saveLocation()
    ↓
Room Database (저장)
    ↓
[UI 업데이트 트리거]
LocationRepository.getLocations() (Flow)
    ↓
ObserveLastLocationUseCase
    ↓
MapViewModel (StateFlow 업데이트)
    ↓
MapScreen (recompose)
    ↓
Google Map에 마커 표시
```

---

## 2. 주요 컴포넌트 상세 설계

### 2.1 Domain Layer

#### Location (Domain Model)
```kotlin
package com.example.asynclocationmap.domain.model

data class Location(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
```

#### LocationRepository (Interface)
```kotlin
package com.example.asynclocationmap.domain.repository

import com.example.asynclocationmap.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun requestLocationUpdate()
    fun getLocations(): Flow<List<Location>>
    suspend fun saveLocation(location: Location)
}
```

#### Use Cases

**ObserveLastLocationUseCase**:
```kotlin
package com.example.asynclocationmap.domain.usecase

import com.example.asynclocationmap.domain.model.Location
import com.example.asynclocationmap.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLastLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<Location>> {
        return repository.getLocations()
    }
}
```

**RequestLocationUpdateUseCase**:
```kotlin
package com.example.asynclocationmap.domain.usecase

import com.example.asynclocationmap.domain.repository.LocationRepository
import javax.inject.Inject

class RequestLocationUpdateUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke() {
        repository.requestLocationUpdate()
    }
}
```

---

### 2.2 Data Layer

#### LocationEntity (Room Entity)
```kotlin
package com.example.asynclocationmap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
```

#### LocationDao
```kotlin
package com.example.asynclocationmap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.asynclocationmap.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Insert
    suspend fun insertLocation(location: LocationEntity)

    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
}
```

#### LocationDatabase
```kotlin
package com.example.asynclocationmap.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.asynclocationmap.data.local.dao.LocationDao
import com.example.asynclocationmap.data.local.entity.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
```

#### LocationMapper
```kotlin
package com.example.asynclocationmap.data.mapper

import com.example.asynclocationmap.data.local.entity.LocationEntity
import com.example.asynclocationmap.domain.model.Location

fun LocationEntity.toDomain(): Location {
    return Location(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}

fun Location.toEntity(): LocationEntity {
    return LocationEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}
```

#### LocationWorker
```kotlin
package com.example.asynclocationmap.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.asynclocationmap.domain.model.Location
import com.example.asynclocationmap.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LocationRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 위치 권한은 이미 확인됨 (버튼 클릭 시 확인)
            val location = fusedLocationClient.lastLocation.await()

            if (location != null) {
                val domainLocation = Location(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveLocation(domainLocation)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
```

#### LocationRepositoryImpl
```kotlin
package com.example.asynclocationmap.data.repository

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.asynclocationmap.data.local.dao.LocationDao
import com.example.asynclocationmap.data.mapper.toDomain
import com.example.asynclocationmap.data.mapper.toEntity
import com.example.asynclocationmap.data.worker.LocationWorker
import com.example.asynclocationmap.domain.model.Location
import com.example.asynclocationmap.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val workManager: WorkManager
) : LocationRepository {

    override suspend fun requestLocationUpdate() {
        val workRequest = OneTimeWorkRequestBuilder<LocationWorker>().build()
        workManager.enqueue(workRequest)
    }

    override fun getLocations(): Flow<List<Location>> {
        return locationDao.getAllLocations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveLocation(location: Location) {
        locationDao.insertLocation(location.toEntity())
    }
}
```

---

### 2.3 Presentation Layer

#### MapUiState
```kotlin
package com.example.asynclocationmap.presentation.ui.map

import com.example.asynclocationmap.domain.model.Location

data class MapUiState(
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

#### MapViewModel
```kotlin
package com.example.asynclocationmap.presentation.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asynclocationmap.domain.usecase.ObserveLastLocationUseCase
import com.example.asynclocationmap.domain.usecase.RequestLocationUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private fun observeLocations() {
        viewModelScope.launch {
            observeLastLocationUseCase()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { locations ->
                    _uiState.update { it.copy(locations = locations) }
                }
        }
    }

    fun onLocationButtonClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                requestLocationUpdateUseCase()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
```

#### MapScreen (Google Maps Compose 사용)
```kotlin
package com.example.asynclocationmap.presentation.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Google Maps Compose 사용
            // - GoogleMap: Compose 기반 지도 컴포저블
            // - Marker: 선언적 방식으로 마커 추가
            // - rememberMarkerState: 마커 상태 관리
            GoogleMap(
                modifier = Modifier.fillMaxSize()
            ) {
                uiState.locations.forEach { location ->
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(location.latitude, location.longitude)
                        ),
                        title = "Location"
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    if (locationPermissionsState.allPermissionsGranted) {
                        viewModel.onLocationButtonClick()
                    } else {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "Get current location"
                )
            }
        }
    }
}
```

---

## 3. 상태 관리 전략

### StateFlow 사용
- ViewModel에서 `StateFlow<MapUiState>` 사용
- UI는 `collectAsState()`로 구독
- 단일 상태 객체로 모든 UI 상태 관리

### Room Flow
- `LocationDao.getAllLocations()`는 `Flow<List<LocationEntity>>` 반환
- DB 변경 시 자동으로 새로운 데이터 emit
- ViewModel에서 지속적으로 관찰하여 UI 업데이트

---

## 4. 백그라운드 작업 처리

### WorkManager 사용 이유
- 앱이 종료되어도 작업 보장
- 시스템이 최적의 시점에 실행
- Doze 모드에서도 동작
- 재시작 후에도 작업 유지

### Worker 생명주기
1. 버튼 클릭 → WorkManager.enqueue()
2. 시스템이 Worker 시작
3. doWork() 실행 (코루틴 환경)
4. FusedLocationProviderClient로 위치 조회
5. Repository를 통해 DB 저장
6. Result.success() 반환
7. Room Flow가 변경 감지 → UI 자동 업데이트

---

## 5. 권한 처리 흐름

```
사용자가 "현재 위치" 버튼 클릭
    ↓
권한 체크 (Accompanist Permissions)
    ↓
    ├─ 권한 있음 → viewModel.onLocationButtonClick()
    └─ 권한 없음 → launchMultiplePermissionRequest()
            ↓
        시스템 권한 다이얼로그 표시
            ↓
        사용자 허용/거부 선택
            ↓
        허용 시 → viewModel.onLocationButtonClick()
```

### 필요한 권한
- `ACCESS_FINE_LOCATION`: 정확한 위치
- `ACCESS_COARSE_LOCATION`: 대략적인 위치
- (Android 10+ 백그라운드 위치 필요 시) `ACCESS_BACKGROUND_LOCATION`

---

## 6. 에러 처리 전략

### ViewModel
- try-catch로 예외 포착
- UiState의 error 필드에 메시지 저장
- UI에서 Snackbar 등으로 표시

### Worker
- Result.success(): 성공
- Result.retry(): 재시도 (일시적 실패)
- Result.failure(): 영구 실패

### Repository
- suspend 함수에서 예외 throw
- Flow의 catch 연산자로 처리