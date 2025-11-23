# LocationWorker 리팩토링 계획

## 현재 문제점

### 1. FusedLocationProviderClient 직접 의존
- LocationWorker가 FusedLocationProviderClient를 직접 주입받아 사용
- Google Play Services에 강하게 결합되어 있음
- 테스트 및 교체가 어려움

### 2. HiltWorkerFactory 미연동
- HiltWorker 어노테이션은 사용 중
- 그러나 Application에서 HiltWorkerFactory 설정이 누락됨
- WorkManager가 Hilt의 의존성 주입을 제대로 활용하지 못함

---

## 해결 방안

### 1. LocationProvider 추상화

#### 1.1 LocationProvider 인터페이스 생성
**파일**: `data/datasource/LocationProvider.kt`

```kotlin
interface LocationProvider {
    suspend fun getCurrentLocation(): android.location.Location?
}
```

- 현재 위치 조회 작업을 추상화
- 구현체에 따라 다른 방식으로 위치 조회 가능

#### 1.2 FusedLocationProvider 구현체 생성
**파일**: `data/datasource/FusedLocationProvider.kt`

```kotlin
class FusedLocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationProvider {
    override suspend fun getCurrentLocation(): android.location.Location? {
        // FusedLocationProviderClient를 사용한 위치 조회
    }
}
```

- Google Play Services 기반 구현
- 기존 LocationWorker의 로직을 이동

**가능한 다른 구현체**:
- `MockLocationProvider`: 테스트용 Mock 위치 제공
- `NetworkLocationProvider`: 네트워크 기반 위치 제공
- `GpsLocationProvider`: GPS 기반 위치 제공

#### 1.3 LocationWorker 수정
- FusedLocationProviderClient 대신 LocationProvider 주입
- 추상화된 인터페이스 사용

```kotlin
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationProvider: LocationProvider,  // 변경
    private val repository: LocationRepository
) : CoroutineWorker(context, params)
```

#### 1.4 DI 모듈 업데이트
**파일**: `data/di/LocationModule.kt`

- LocationProvider 바인딩 추가
- FusedLocationProvider를 LocationProvider에 바인딩

---

### 2. HiltWorkerFactory 연동

#### 2.1 MyApplication 수정
**파일**: `app/src/main/java/.../MyApplication.kt`

```kotlin
@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

**변경 사항**:
- `Configuration.Provider` 인터페이스 구현
- `HiltWorkerFactory` 주입
- `workManagerConfiguration` 오버라이드하여 Hilt Factory 설정

#### 2.2 AndroidManifest.xml 수정
**파일**: `app/src/main/AndroidManifest.xml`

```xml
<application
    ...
    android:name=".MyApplication">

    <!-- WorkManager 기본 초기화 비활성화 -->
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

    ...
</application>
```

**목적**:
- WorkManager 자동 초기화 비활성화
- Application에서 수동으로 HiltWorkerFactory와 함께 초기화

---

## 구현 순서

### Phase 1: LocationProvider 추상화
1. ✅ LocationProvider 인터페이스 생성
2. ✅ FusedLocationProvider 구현체 생성
3. ✅ LocationWorker에서 LocationProvider 사용하도록 수정
4. ✅ LocationModule에 바인딩 추가
5. ✅ 빌드 검증

### Phase 2: HiltWorkerFactory 연동
1. ✅ MyApplication에 Configuration.Provider 구현
2. ✅ HiltWorkerFactory 주입 및 설정
3. ✅ AndroidManifest.xml에서 WorkManager 자동 초기화 비활성화
4. ✅ 빌드 및 런타임 검증

---

## 기대 효과

### LocationProvider 추상화
- ✅ **테스트 용이성**: Mock LocationProvider로 테스트 가능
- ✅ **유연성**: Google Play Services 없는 환경에서도 대체 가능
- ✅ **의존성 역전**: LocationWorker가 구체적 구현에 의존하지 않음
- ✅ **단일 책임**: 위치 조회 로직이 별도 클래스로 분리

### HiltWorkerFactory 연동
- ✅ **정상적인 DI**: Hilt가 Worker에 의존성을 제대로 주입
- ✅ **런타임 안정성**: WorkManager가 Hilt와 올바르게 통합
- ✅ **확장성**: 향후 다른 Worker 추가 시에도 동일한 방식 적용

---

## 주의사항

1. **권한 체크**: LocationProvider 구현체에서 권한 체크 필수
2. **Null 처리**: 위치 조회 실패 시 null 반환 및 적절한 에러 처리
3. **테스트**: HiltWorkerFactory 연동 후 실제 디바이스에서 테스트 필요
4. **마이그레이션**: 기존 대기 중인 WorkRequest는 영향 받지 않음

---

## 참고 자료

- [WorkManager with Hilt](https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager)
- [Custom WorkManager Configuration](https://developer.android.com/topic/libraries/architecture/workmanager/advanced/custom-configuration)
- [FusedLocationProviderClient API](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient)
