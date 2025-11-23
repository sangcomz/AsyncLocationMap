# 테스트 가능성 및 확장성 설계

## 1. 테스트 가능한 아키텍처 설계 원칙

### 1.1 의존성 역전 원칙 (Dependency Inversion)

모든 레이어는 구체적 구현이 아닌 인터페이스에 의존합니다.

```kotlin
// ❌ 나쁜 예: 구체 클래스에 의존
class MapViewModel(
    private val repositoryImpl: LocationRepositoryImpl // 구체 클래스
)

// ✅ 좋은 예: 인터페이스에 의존
class MapViewModel(
    private val repository: LocationRepository // 인터페이스
)
```

### 1.2 생성자 주입 (Constructor Injection)

모든 의존성은 생성자를 통해 주입되어 테스트 시 Mock 객체로 쉽게 교체 가능합니다.

```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val requestLocationUpdateUseCase: RequestLocationUpdateUseCase
) : ViewModel() {
    // 모든 의존성이 생성자를 통해 주입됨
}
```

### 1.3 단일 책임 원칙 (Single Responsibility)

각 클래스와 함수는 하나의 명확한 책임만 가집니다.

```kotlin
// UseCase는 하나의 비즈니스 로직만 담당
class GetLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<Location>> {
        return repository.getLocations()
    }
}

// 별도의 UseCase로 분리
class RequestLocationUpdateUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke() {
        repository.requestLocationUpdate()
    }
}
```

---

## 2. 레이어별 테스트 전략

### 2.1 Domain Layer 테스트

Domain 레이어는 순수 Kotlin이므로 가장 테스트하기 쉽습니다.

#### Use Case 테스트 예시

```kotlin
// domain/src/test/kotlin/usecase/GetLocationsUseCaseTest.kt
class GetLocationsUseCaseTest {

    private lateinit var repository: LocationRepository
    private lateinit var useCase: GetLocationsUseCase

    @Before
    fun setup() {
        repository = mockk() // MockK 사용
        useCase = GetLocationsUseCase(repository)
    }

    @Test
    fun `invoke should return locations from repository`() = runTest {
        // Given
        val expectedLocations = listOf(
            Location(1, 37.5, 127.0, 1234567890L),
            Location(2, 37.6, 127.1, 1234567891L)
        )
        every { repository.getLocations() } returns flowOf(expectedLocations)

        // When
        val result = useCase().first()

        // Then
        assertEquals(expectedLocations, result)
        verify { repository.getLocations() }
    }
}
```

### 2.2 Data Layer 테스트

#### Repository 테스트

```kotlin
// data/src/test/kotlin/repository/LocationRepositoryImplTest.kt
@RunWith(JUnit4::class)
class LocationRepositoryImplTest {

    private lateinit var locationDao: LocationDao
    private lateinit var workManager: WorkManager
    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setup() {
        locationDao = mockk()
        workManager = mockk()
        repository = LocationRepositoryImpl(locationDao, workManager)
    }

    @Test
    fun `getLocations should map entities to domain models`() = runTest {
        // Given
        val entities = listOf(
            LocationEntity(1, 37.5, 127.0, 1234567890L)
        )
        every { locationDao.getAllLocations() } returns flowOf(entities)

        // When
        val result = repository.getLocations().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(37.5, result[0].latitude, 0.001)
    }

    @Test
    fun `requestLocationUpdate should enqueue work`() = runTest {
        // Given
        every { workManager.enqueue(any<OneTimeWorkRequest>()) } returns mockk()

        // When
        repository.requestLocationUpdate()

        // Then
        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }
}
```

#### Room Dao 테스트 (Instrumented Test)

```kotlin
// data/src/androidTest/kotlin/local/dao/LocationDaoTest.kt
@RunWith(AndroidJUnit4::class)
class LocationDaoTest {

    private lateinit var database: LocationDatabase
    private lateinit var locationDao: LocationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            LocationDatabase::class.java
        ).build()
        locationDao = database.locationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetLocations() = runTest {
        // Given
        val location = LocationEntity(0, 37.5, 127.0, 1234567890L)

        // When
        locationDao.insertLocation(location)
        val locations = locationDao.getAllLocations().first()

        // Then
        assertEquals(1, locations.size)
        assertEquals(37.5, locations[0].latitude, 0.001)
    }
}
```

### 2.3 Presentation Layer 테스트

#### ViewModel 테스트

```kotlin
// presentation/src/test/kotlin/ui/map/MapViewModelTest.kt
@ExperimentalCoroutinesTest
class MapViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getLocationsUseCase: GetLocationsUseCase
    private lateinit var requestLocationUpdateUseCase: RequestLocationUpdateUseCase
    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getLocationsUseCase = mockk()
        requestLocationUpdateUseCase = mockk()
        viewModel = MapViewModel(getLocationsUseCase, requestLocationUpdateUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should observe locations`() = runTest {
        // Given
        val expectedLocations = listOf(
            Location(1, 37.5, 127.0, 1234567890L)
        )
        every { getLocationsUseCase() } returns flowOf(expectedLocations)

        // When
        viewModel = MapViewModel(getLocationsUseCase, requestLocationUpdateUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedLocations, viewModel.uiState.value.locations)
    }

    @Test
    fun `onLocationButtonClick should update loading state`() = runTest {
        // Given
        every { getLocationsUseCase() } returns flowOf(emptyList())
        coEvery { requestLocationUpdateUseCase() } just Runs

        // When
        viewModel.onLocationButtonClick()

        // Then (loading starts)
        assertEquals(true, viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        // Then (loading ends)
        assertEquals(false, viewModel.uiState.value.isLoading)
        coVerify { requestLocationUpdateUseCase() }
    }
}
```

---

## 3. 확장 가능한 구조 설계

### 3.1 인터페이스 기반 설계

새로운 기능 추가 시 기존 코드 변경 최소화

```kotlin
// 현재 구현
interface LocationRepository {
    suspend fun requestLocationUpdate()
    fun getLocations(): Flow<List<Location>>
    suspend fun saveLocation(location: Location)
}

// 확장 예시 1: 위치 삭제 기능 추가
interface LocationRepository {
    suspend fun requestLocationUpdate()
    fun getLocations(): Flow<List<Location>>
    suspend fun saveLocation(location: Location)
    suspend fun deleteLocation(id: Long) // 새 기능 추가
    suspend fun deleteAllLocations() // 새 기능 추가
}

// 확장 예시 2: 필터링 기능 추가
interface LocationRepository {
    suspend fun requestLocationUpdate()
    fun getLocations(): Flow<List<Location>>
    fun getLocationsByDateRange(startTime: Long, endTime: Long): Flow<List<Location>>
    suspend fun saveLocation(location: Location)
}
```

### 3.2 UseCase 패턴으로 비즈니스 로직 분리

새로운 기능을 독립적인 UseCase로 추가

```kotlin
// 기존 UseCase
class GetLocationsUseCase @Inject constructor(...)
class RequestLocationUpdateUseCase @Inject constructor(...)

// 확장: 새로운 UseCase 추가
class DeleteLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteLocation(id)
    }
}

class DeleteAllLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllLocations()
    }
}

class GetLocationsByDateRangeUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(startTime: Long, endTime: Long): Flow<List<Location>> {
        return repository.getLocationsByDateRange(startTime, endTime)
    }
}
```

### 3.3 sealed class/interface로 UI State 확장

```kotlin
// 기본 구조
data class MapUiState(
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// 확장 예시: 더 복잡한 상태 관리
data class MapUiState(
    val locations: List<Location> = emptyList(),
    val selectedLocation: Location? = null, // 선택된 위치
    val filterType: LocationFilterType = LocationFilterType.ALL, // 필터 타입
    val isLoading: Boolean = false,
    val error: UiError? = null // 더 구체적인 에러 타입
)

sealed interface LocationFilterType {
    object ALL : LocationFilterType
    data class DateRange(val start: Long, val end: Long) : LocationFilterType
    data class Radius(val centerLat: Double, val centerLng: Double, val radiusMeters: Double) : LocationFilterType
}

sealed interface UiError {
    object NetworkError : UiError
    object LocationPermissionDenied : UiError
    object LocationServiceDisabled : UiError
    data class Unknown(val message: String) : UiError
}
```

### 3.4 Strategy Pattern으로 위치 제공자 교체 가능

```kotlin
// 위치 제공 전략 인터페이스
interface LocationProvider {
    suspend fun getCurrentLocation(): Location?
}

// Google Play Services 구현
class FusedLocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationProvider {
    override suspend fun getCurrentLocation(): Location? {
        val androidLocation = fusedLocationClient.lastLocation.await()
        return androidLocation?.let {
            Location(
                latitude = it.latitude,
                longitude = it.longitude,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}

// 테스트용 Mock 구현
class MockLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): Location {
        return Location(
            latitude = 37.5665,
            longitude = 126.9780,
            timestamp = System.currentTimeMillis()
        )
    }
}

// Worker에서 사용
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: LocationRepository,
    private val locationProvider: LocationProvider // 인터페이스에 의존
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                repository.saveLocation(location)
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

### 3.5 Data Source 추상화

로컬/원격 데이터 소스를 쉽게 교체 가능

```kotlin
// 데이터 소스 인터페이스
interface LocationLocalDataSource {
    fun getLocations(): Flow<List<Location>>
    suspend fun saveLocation(location: Location)
    suspend fun deleteLocation(id: Long)
}

interface LocationRemoteDataSource {
    suspend fun syncLocations(locations: List<Location>)
    suspend fun fetchLocations(): List<Location>
}

// Room 구현
class LocationLocalDataSourceImpl @Inject constructor(
    private val locationDao: LocationDao
) : LocationLocalDataSource {
    override fun getLocations(): Flow<List<Location>> {
        return locationDao.getAllLocations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveLocation(location: Location) {
        locationDao.insertLocation(location.toEntity())
    }

    override suspend fun deleteLocation(id: Long) {
        locationDao.deleteLocation(id)
    }
}

// Repository는 데이터 소스에만 의존
class LocationRepositoryImpl @Inject constructor(
    private val localDataSource: LocationLocalDataSource,
    private val remoteDataSource: LocationRemoteDataSource?, // Optional
    private val workManager: WorkManager
) : LocationRepository {

    override fun getLocations(): Flow<List<Location>> {
        return localDataSource.getLocations()
    }

    override suspend fun saveLocation(location: Location) {
        localDataSource.saveLocation(location)
        // 필요시 원격 동기화
        remoteDataSource?.syncLocations(listOf(location))
    }
}
```

---

## 4. 테스트 의존성 추가

### build.gradle.kts (각 모듈)

```kotlin
// Version Catalog에 추가할 테스트 라이브러리
[versions]
junit = "4.13.2"
mockk = "1.13.13"
coroutines-test = "1.10.0"
turbine = "1.2.0"
robolectric = "4.14.1"
androidx-test = "1.6.1"
espresso = "3.6.1"

[libraries]
# Unit Test
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines-test" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

# Android Test
androidx-test-core = { group = "androidx.test", name = "core", version.ref = "androidx-test" }
androidx-test-runner = { group = "androidx.test", name = "runner", version.ref = "androidx-test" }
androidx-test-rules = { group = "androidx.test", name = "rules", version.ref = "androidx-test" }
androidx-arch-core-testing = { group = "androidx.arch.core", name = "core-testing", version = "2.2.0" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
hilt-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }
work-testing = { group = "androidx.work", name = "work-testing", version.ref = "work" }

# UI Test
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
```

---

## 5. 확장 시나리오 예시

### 5.1 실시간 위치 추적 기능 추가

```kotlin
// 1. UseCase 추가
class StartRealTimeTrackingUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(intervalMillis: Long) {
        repository.startRealTimeTracking(intervalMillis)
    }
}

class StopRealTimeTrackingUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke() {
        repository.stopRealTimeTracking()
    }
}

// 2. Repository에 메서드 추가
interface LocationRepository {
    // 기존 메서드들...
    suspend fun startRealTimeTracking(intervalMillis: Long)
    suspend fun stopRealTimeTracking()
}

// 3. PeriodicWorkRequest로 구현
class LocationRepositoryImpl @Inject constructor(...) : LocationRepository {
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

    override suspend fun stopRealTimeTracking() {
        workManager.cancelUniqueWork("location_tracking")
    }
}
```

### 5.2 위치 기록 내보내기 기능 추가

```kotlin
// 1. UseCase 추가
class ExportLocationsUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val exportStrategy: LocationExportStrategy
) {
    suspend operator fun invoke(outputPath: String): Result<Unit> {
        return try {
            val locations = repository.getLocations().first()
            exportStrategy.export(locations, outputPath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// 2. Export Strategy 인터페이스
interface LocationExportStrategy {
    suspend fun export(locations: List<Location>, outputPath: String)
}

class JsonExportStrategy : LocationExportStrategy {
    override suspend fun export(locations: List<Location>, outputPath: String) {
        // JSON 형식으로 내보내기
    }
}

class CsvExportStrategy : LocationExportStrategy {
    override suspend fun export(locations: List<Location>, outputPath: String) {
        // CSV 형식으로 내보내기
    }
}
```

### 5.3 지오펜싱 기능 추가

```kotlin
// 1. Domain Model 확장
data class Geofence(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val name: String
)

// 2. Repository 확장
interface LocationRepository {
    // 기존 메서드들...
    suspend fun addGeofence(geofence: Geofence)
    suspend fun removeGeofence(id: String)
    fun getGeofences(): Flow<List<Geofence>>
}

// 3. UseCase 추가
class AddGeofenceUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(geofence: Geofence) {
        repository.addGeofence(geofence)
    }
}
```

---

## 6. 테스트 커버리지 목표

### 레이어별 커버리지 목표
- **Domain Layer**: 100% (비즈니스 로직 핵심)
- **Data Layer**: 80%+ (Repository, Mapper)
- **Presentation Layer**: 80%+ (ViewModel)
- **UI Layer**: 주요 시나리오 테스트

### 테스트 피라미드
```
        /\
       /  \     E2E Tests (5%)
      /____\
     /      \   Integration Tests (15%)
    /________\
   /          \ Unit Tests (80%)
  /____________\
```

---

## 7. CI/CD에서 테스트 자동화

### GitHub Actions 예시

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Run Unit Tests
        run: ./gradlew test

      - name: Run Android Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          script: ./gradlew connectedAndroidTest

      - name: Upload Test Reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: '**/build/reports/tests/'
```

---

## 8. 요약: 테스트 가능하고 확장 가능한 설계 체크리스트

- [x] 모든 의존성은 인터페이스를 통해 주입
- [x] 생성자 주입 사용 (Hilt)
- [x] 각 클래스는 단일 책임만 가짐
- [x] UseCase 패턴으로 비즈니스 로직 분리
- [x] Repository 패턴으로 데이터 소스 추상화
- [x] Strategy 패턴으로 알고리즘 교체 가능
- [x] sealed class/interface로 타입 안전성 확보
- [x] Flow를 사용한 반응형 데이터 스트림
- [x] Unit/Integration/UI 테스트 작성
- [x] Mock 프레임워크 활용 (MockK)
- [x] 테스트 더블 사용 (Fake, Stub, Mock)
