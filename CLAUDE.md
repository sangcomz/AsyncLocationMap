# AsyncLocationMap - 구현 가이드

> 이 문서는 Claude Code가 프로젝트를 구현할 때 참고하는 가이드입니다.

## 프로젝트 개요

**목표**: 지도뷰에서 비동기적으로 현재 위치를 표시하는 안드로이드 앱

**핵심 요구사항**:
- "현재 위치" 버튼 클릭 → WorkManager로 위치 조회
- 조회된 위치 → Room DB 저장
- DB에서 위치 데이터 조회 → 지도에 마커 표시
- 백그라운드에서도 작업 지속
- Clean Architecture + MVVM 패턴
- 모듈 단위 분리 (app, presentation, data, domain)

---

## 핵심 기술 스택

### UI & Framework
- **Jetpack Compose**: 모든 UI 구현
- **Google Maps Compose** (v6.12.2): `GoogleMap`, `Marker`, `rememberMarkerState` 사용
- **Accompanist Permissions** (v0.37.3): 위치 권한 처리

### 아키텍처 & DI
- **Clean Architecture**: domain → data/presentation → app
- **MVVM**: ViewModel + UiState
- **Hilt** (v2.57.1): 의존성 주입
  - `@HiltAndroidApp` (Application)
  - `@HiltViewModel` (ViewModel)
  - `@HiltWorker` (Worker)

### 비동기 & 백그라운드
- **Kotlin Coroutines** (v1.10.0): suspend 함수, Flow
- **WorkManager** (v2.11.0) + **HiltWorker**: 백그라운드 위치 조회
- **Room** (v2.8.4): 로컬 DB, Flow 기반 데이터 스트림

### 버전 관리
- **Version Catalog**: `gradle/libs.versions.toml`
- **Kotlin**: v2.2.21
- **AGP**: v8.13.0
- **Compose BOM**: v2025.10.01

---

## 모듈 구조

```
AsyncLocationMap/
├── app/                    # Application 모듈
├── presentation/           # UI 레이어
├── domain/                # 비즈니스 로직 (순수 Kotlin)
└── data/                  # 데이터 레이어
```

### 의존성 방향
```
app → presentation, data, domain
presentation → domain
data → domain
domain (독립)
```

---

## 패키지 구조 및 주요 파일

### Domain 모듈 (`domain/`)
**역할**: 비즈니스 로직, 인터페이스 정의 (Android 의존성 없음)

```
domain/src/main/java/com/example/asynclocationmap/domain/
├── model/
│   └── Location.kt                      # Domain Model
├── repository/
│   └── LocationRepository.kt            # Repository Interface
└── usecase/
    ├── GetLocationsUseCase.kt           # 위치 목록 조회
    └── RequestLocationUpdateUseCase.kt  # 위치 업데이트 요청
```

**중요 포인트**:
- 순수 Kotlin 모듈 (Java Library)
- 다른 레이어에 의존하지 않음
- 인터페이스만 정의, 구현은 Data 레이어에서

---

### Data 모듈 (`data/`)
**역할**: Repository 구현, Room DB, WorkManager

```
data/src/main/java/com/example/asynclocationmap/data/
├── local/
│   ├── entity/
│   │   └── LocationEntity.kt            # Room Entity
│   ├── dao/
│   │   └── LocationDao.kt               # Room DAO
│   └── db/
│       └── LocationDatabase.kt          # Room Database
├── mapper/
│   └── LocationMapper.kt                # Entity ↔ Domain 변환
├── repository/
│   └── LocationRepositoryImpl.kt        # Repository 구현
├── worker/
│   └── LocationWorker.kt                # @HiltWorker
└── di/
    ├── DatabaseModule.kt                # Room DI
    ├── RepositoryModule.kt              # Repository DI
    └── LocationModule.kt                # FusedLocationClient, WorkManager DI
```

**중요 포인트**:
- `LocationWorker`는 반드시 `@HiltWorker` + `@AssistedInject` 사용
- Room은 Flow 반환으로 실시간 데이터 스트림 제공
- Mapper로 Entity와 Domain Model 분리

**LocationWorker 예시**:
```kotlin
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LocationRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result { /* ... */ }
}
```

---

### Presentation 모듈 (`presentation/`)
**역할**: UI (Compose), ViewModel, UiState

```
presentation/src/main/java/com/example/asynclocationmap/presentation/
└── ui/
    └── map/
        ├── MapScreen.kt        # Compose UI (GoogleMap)
        ├── MapViewModel.kt     # @HiltViewModel
        └── MapUiState.kt       # UI State
```

**중요 포인트**:
- `MapScreen`은 `GoogleMap` Composable 사용 (maps-compose)
- `MapViewModel`은 `@HiltViewModel` 어노테이션
- 단일 `MapUiState`로 모든 UI 상태 관리 (StateFlow)

**MapScreen 예시**:
```kotlin
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    GoogleMap(modifier = Modifier.fillMaxSize()) {
        uiState.locations.forEach { location ->
            Marker(
                state = rememberMarkerState(
                    position = LatLng(location.latitude, location.longitude)
                ),
                title = "Location"
            )
        }
    }

    // FloatingActionButton for current location
}
```

---

### App 모듈 (`app/`)
**역할**: Application, MainActivity, 모든 모듈 통합

```
app/src/main/java/com/example/asynclocationmap/
├── MyApplication.kt        # @HiltAndroidApp
└── MainActivity.kt         # @AndroidEntryPoint
```

**AndroidManifest.xml 필수 설정**:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />

<application>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="${MAPS_API_KEY}" />
</application>
```

---

## 데이터 흐름

### 사용자가 "현재 위치" 버튼 클릭
```
1. MapScreen (권한 확인)
   ↓
2. MapViewModel.onLocationButtonClick()
   ↓
3. RequestLocationUpdateUseCase()
   ↓
4. LocationRepository.requestLocationUpdate()
   ↓
5. WorkManager.enqueue(LocationWorker)
   ↓
[백그라운드 실행]
6. LocationWorker.doWork()
   - FusedLocationProviderClient로 위치 조회
   - LocationRepository.saveLocation() 호출
   ↓
7. Room DB 저장 (LocationDao.insertLocation)
   ↓
[자동 UI 업데이트]
8. LocationDao.getAllLocations() Flow emit
   ↓
9. GetLocationsUseCase → MapViewModel
   ↓
10. MapUiState 업데이트 (StateFlow)
   ↓
11. MapScreen recompose
   ↓
12. 지도에 새 마커 표시
```

---

## 핵심 구현 원칙

### 1. 의존성 역전 (Dependency Inversion)
- 모든 레이어는 인터페이스에 의존
- Repository, UseCase는 인터페이스로 정의
- 테스트 시 Mock 객체로 교체 가능

### 2. 생성자 주입 (Constructor Injection)
- Hilt를 통한 자동 주입
- 모든 의존성은 생성자 파라미터로 전달

### 3. 단일 책임 (Single Responsibility)
- 각 UseCase는 하나의 비즈니스 로직만
- ViewModel은 UI 상태 관리만
- Repository는 데이터 접근만

### 4. 반응형 프로그래밍 (Reactive)
- Room은 Flow 반환
- ViewModel은 StateFlow로 UI 상태 emit
- Compose는 collectAsState()로 자동 recompose

---

## Hilt 설정 가이드

### Application 레벨
```kotlin
@HiltAndroidApp
class MyApplication : Application()
```

### Activity
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() { /* ... */ }
```

### ViewModel
```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val requestLocationUpdateUseCase: RequestLocationUpdateUseCase
) : ViewModel()
```

### Worker (중요!)
```kotlin
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LocationRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : CoroutineWorker(context, params)
```

**AndroidManifest.xml에 추가**:
```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```

**Application에서 WorkManager 초기화**:
```kotlin
@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

---

## DI Modules 가이드

### DatabaseModule (data/di/)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLocationDatabase(@ApplicationContext context: Context): LocationDatabase {
        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            "location_database"
        ).build()
    }

    @Provides
    fun provideLocationDao(database: LocationDatabase): LocationDao {
        return database.locationDao()
    }
}
```

### LocationModule (data/di/)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}
```

### RepositoryModule (data/di/)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository
}
```

---

## Google Maps Compose 사용법

### 기본 설정
```kotlin
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition

@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // 마커 추가
        Marker(
            state = rememberMarkerState(position = LatLng(37.5665, 126.9780)),
            title = "Seoul",
            snippet = "Capital of South Korea"
        )
    }
}
```

---

## Room 설정 가이드

### Entity
```kotlin
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
```

### DAO (Flow 사용 필수)
```kotlin
@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>  // Flow 반환!

    @Insert
    suspend fun insertLocation(location: LocationEntity)
}
```

### Database
```kotlin
@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
```

---

## 테스트 가능성 체크리스트

- [ ] 모든 의존성은 인터페이스를 통해 주입
- [ ] Repository는 인터페이스로 정의
- [ ] UseCase는 단일 책임
- [ ] ViewModel은 생성자 주입
- [ ] Worker는 @HiltWorker + @AssistedInject
- [ ] Flow를 사용한 반응형 데이터 스트림

---

## 확장 시나리오

### 실시간 위치 추적 추가
```kotlin
class StartRealTimeTrackingUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(intervalMillis: Long) {
        repository.startRealTimeTracking(intervalMillis)
    }
}

// PeriodicWorkRequest 사용
class LocationRepositoryImpl {
    override suspend fun startRealTimeTracking(intervalMillis: Long) {
        val workRequest = PeriodicWorkRequestBuilder<LocationWorker>(
            intervalMillis, TimeUnit.MILLISECONDS
        ).build()
        workManager.enqueueUniquePeriodicWork(
            "location_tracking",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}
```

### 위치 삭제 기능 추가
```kotlin
// Domain
interface LocationRepository {
    suspend fun deleteLocation(id: Long)
    suspend fun deleteAllLocations()
}

// UseCase
class DeleteLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteLocation(id)
    }
}

// Data
class LocationRepositoryImpl {
    override suspend fun deleteLocation(id: Long) {
        locationDao.deleteLocation(id)
    }
}
```

---

## 구현 순서

### Phase 1: 프로젝트 초기 설정
1. Android Studio에서 Empty Activity 프로젝트 생성
2. 모듈 생성 (presentation, domain, data)
3. `gradle/libs.versions.toml` 작성
4. 각 모듈의 `build.gradle.kts` 설정
5. `settings.gradle.kts` 설정

### Phase 2: Domain Layer
1. `Location.kt` (Domain Model)
2. `LocationRepository.kt` (Interface)
3. `GetLocationsUseCase.kt`
4. `RequestLocationUpdateUseCase.kt`

### Phase 3: Data Layer
1. `LocationEntity.kt`
2. `LocationDao.kt`
3. `LocationDatabase.kt`
4. `LocationMapper.kt`
5. `LocationWorker.kt` (@HiltWorker)
6. `LocationRepositoryImpl.kt`
7. DI Modules (DatabaseModule, LocationModule, RepositoryModule)

### Phase 4: Presentation Layer
1. `MapUiState.kt`
2. `MapViewModel.kt` (@HiltViewModel)
3. `MapScreen.kt` (Compose + GoogleMap)

### Phase 5: App Module
1. `MyApplication.kt` (@HiltAndroidApp)
2. `MainActivity.kt` (@AndroidEntryPoint)
3. `AndroidManifest.xml` 설정

### Phase 6: 테스트 및 검증
1. 앱 실행
2. 권한 요청 확인
3. 위치 조회 및 DB 저장 확인
4. 지도에 마커 표시 확인
5. 백그라운드 동작 확인

---

## 주의사항

### Google Maps API Key
- Google Cloud Console에서 발급 필요
- `local.properties`에 저장: `MAPS_API_KEY=your_api_key`
- `build.gradle.kts`에서 읽어서 BuildConfig에 추가

### 권한 처리
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` 필수
- Accompanist Permissions로 런타임 권한 요청
- 버튼 클릭 시 권한 확인 후 WorkManager 실행

### WorkManager with Hilt
- 반드시 `@HiltWorker` + `@AssistedInject` 사용
- `HiltWorkerFactory` 주입 필요
- `Configuration.Provider` 구현 필요

### Room + Coroutines
- DAO는 `suspend` 또는 `Flow` 반환
- Flow는 자동으로 DB 변경 감지
- ViewModel에서 `viewModelScope.launch`로 수집

---

## 참고 문서

- `docs/01_project_architecture.md`: 전체 아키텍처
- `docs/02_module_dependencies.md`: 모듈 구조 및 Version Catalog
- `docs/03_data_flow_design.md`: 데이터 흐름 및 컴포넌트 상세 설계
- `docs/04_implementation_steps.md`: 단계별 구현 계획
- `docs/05_testability_extensibility.md`: 테스트 가능성 및 확장성

---

## 최종 체크리스트

구현 완료 전 확인 사항:

- [ ] 4개 모듈 생성 완료 (app, presentation, data, domain)
- [ ] Version Catalog 설정 완료
- [ ] Hilt 설정 완료 (@HiltAndroidApp, @HiltViewModel, @HiltWorker)
- [ ] Room Database 설정 완료
- [ ] WorkManager + HiltWorker 연동 완료
- [ ] Google Maps Compose 설정 완료
- [ ] 권한 처리 구현 (Accompanist Permissions)
- [ ] Flow 기반 데이터 스트림 구현
- [ ] StateFlow로 UI 상태 관리
- [ ] Clean Architecture 구조 준수 (domain → data/presentation → app)
- [ ] 모든 의존성은 인터페이스 기반 주입

---

**구현 시작 전 반드시 이 문서를 참고하세요!**
