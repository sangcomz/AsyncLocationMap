# AsyncLocationMap

비동기적으로 현재 위치를 조회하고 지도에 표시하는 Android 애플리케이션

## 🤖 AI 개발 프롬프트

프로젝트 관련 프롬프트 히스토리와 개발 과정을 확인할 수 있습니다:

- 📁 **로컬 파일**: [`prompt/`](./prompt/) 디렉토리에서 모든 프롬프트 및 대화 기록 확인
- 🌐 **온라인 뷰어**: [Prompt Viewer](https://prompt-viewer-eight.vercel.app)에서 대화형 UI로 확인

---

## 📱 앱 소개

AsyncLocationMap은 **WorkManager**를 활용하여 백그라운드에서 비동기적으로 현재 위치를 조회하고, Google Maps에 마커로 표시하는 애플리케이션입니다. Clean Architecture 패턴을 적용하여 확장 가능하고 테스트하기 쉬운 구조로 설계되었습니다.

### 주요 기능

- ✅ **비동기 위치 조회**: WorkManager를 통한 백그라운드 위치 추적
- ✅ **실시간 지도 표시**: Google Maps Compose로 구현된 지도 뷰
- ✅ **위치 히스토리**: Room Database에 저장된 위치 이력 확인
- ✅ **마커 클러스터링**: 여러 마커를 그룹화하여 표시
- ✅ **Street View 미리보기**: 각 위치의 Street View 이미지 제공
- ✅ **다크 모드 지원**: 시스템 테마에 따른 지도 스타일 변경

## 🎬 사용 방법

### 1. 초기 실행

앱을 처음 실행하면 지도 화면이 표시됩니다. 기본 위치는 서울(37.5665, 126.9780)입니다.

### 2. 위치 권한 허용

<img src="docs/images/permission_request.png" width="300" alt="권한 요청">

현재 위치 버튼(📍)을 처음 클릭하면 위치 권한 요청 다이얼로그가 표시됩니다.

- **정확한 위치**: 더 정확한 위치 정보 제공
- **대략적인 위치**: 대략적인 위치만 제공

권한을 허용해야 앱의 모든 기능을 사용할 수 있습니다.

### 3. 현재 위치 조회

<img src="docs/images/get_location.png" width="300" alt="위치 조회">

**현재 위치 버튼(📍)** 클릭:
1. 로딩 인디케이터 표시
2. WorkManager가 백그라운드에서 위치 조회 시작
3. FusedLocationProvider를 통해 현재 위치 획득
4. Room Database에 위치 저장 (timestamp 포함)
5. 지도에 마커 자동 추가 및 카메라 이동

### 4. 위치 히스토리 확인

<img src="docs/images/location_history.png" width="300" alt="위치 히스토리">

**위치 목록 버튼(📋)** 클릭:
1. 하단 시트(Bottom Sheet) 표시
2. 저장된 모든 위치 목록 표시 (최신순)
3. 각 위치 카드에는:
   - 위도/경도 정보
   - 저장 시간
   - Street View 미리보기 이미지

### 5. 특정 위치로 이동

위치 히스토리 목록에서 특정 위치 카드 클릭:
1. 하단 시트 자동 닫힘
2. 지도 카메라가 해당 위치로 부드럽게 이동
3. 해당 마커가 중앙에 표시됨

### 6. 마커 클러스터링

<img src="docs/images/clustering.png" width="300" alt="마커 클러스터링">

여러 위치가 가까이 있을 때:
- 자동으로 클러스터로 그룹화
- 클러스터 숫자는 포함된 마커 개수
- 지도를 확대하면 개별 마커로 분리됨

## 🏗️ 기술 스택

### UI
- **Jetpack Compose**: 선언적 UI 프레임워크
- **Google Maps Compose**: Compose 기반 지도 컴포넌트
- **Material Design 3**: 최신 Material 디자인 시스템

### 아키텍처
- **Clean Architecture**: Presentation, Domain, Data 레이어 분리
- **MVVM Pattern**: ViewModel을 통한 UI 상태 관리
- **Single State Pattern**: MapUiState로 모든 UI 상태 통합 관리

### 비동기 처리
- **Kotlin Coroutines**: 비동기 작업 처리
- **Flow**: 반응형 데이터 스트림
- **WorkManager**: 백그라운드 작업 관리

### 로컬 저장소
- **Room Database**: 위치 데이터 영구 저장
- **Flow 기반 Query**: 실시간 데이터 업데이트

### 의존성 주입
- **Hilt (Dagger)**: 의존성 주입 프레임워크
- **HiltViewModel**: ViewModel 자동 주입
- **HiltWorker**: Worker 의존성 주입

### 위치 서비스
- **FusedLocationProviderClient**: Google Play Services 위치 API
- **Strategy Pattern**: LocationProvider 인터페이스로 추상화

## 📦 프로젝트 구조

```
AsyncLocationMap/
├── app/                    # Application 모듈
│   └── MyApplication.kt   # Hilt 설정
│
├── presentation/          # Presentation 레이어
│   ├── map/
│   │   ├── MapScreen.kt           # 지도 화면 Composable
│   │   ├── MapViewModel.kt        # UI 상태 관리
│   │   ├── MapUiState.kt          # UI 상태 정의
│   │   └── LocationUiModel.kt     # UI 모델
│   ├── components/
│   │   ├── LocationHistoryBottomSheet.kt  # 위치 목록 하단 시트
│   │   └── LocationCard.kt                # 위치 카드
│   └── utils/
│       └── StreetViewUrlBuilder.kt        # Street View URL 생성
│
├── domain/               # Domain 레이어
│   ├── model/
│   │   └── Location.kt             # 도메인 모델
│   ├── repository/
│   │   └── LocationRepository.kt   # Repository 인터페이스
│   └── usecase/
│       ├── ObserveLastLocationUseCase.kt        # 위치 관찰
│       └── RequestLocationUpdateUseCase.kt      # 위치 요청
│
├── data/                 # Data 레이어
│   ├── repository/
│   │   └── LocationRepositoryImpl.kt
│   ├── datasource/
│   │   ├── LocationLocalDataSource.kt       # 인터페이스
│   │   ├── LocationRemoteDataSource.kt      # 인터페이스
│   │   ├── RoomLocationDataSource.kt        # Room 구현
│   │   ├── WorkManagerLocationDataSource.kt # WorkManager 구현
│   │   ├── LocationProvider.kt              # 위치 조회 인터페이스
│   │   └── FusedLocationProvider.kt         # GPS 구현
│   ├── local/
│   │   ├── db/LocationDatabase.kt
│   │   ├── dao/LocationDao.kt
│   │   └── entity/LocationEntity.kt
│   ├── worker/
│   │   └── LocationWorker.kt                # 위치 조회 Worker
│   ├── mapper/
│   │   └── LocationMapper.kt                # Entity ↔ Domain 변환
│   └── util/
│       └── LocationNormalizer.kt            # 좌표 정규화
│
└── testing/              # 테스트 모듈 (Android Library)
    ├── fake/
    │   ├── FakeLocationRepository.kt
    │   ├── FakeLocationLocalDataSource.kt
    │   └── FakeLocationRemoteDataSource.kt
    └── data/
        └── TestLocations.kt
```

## 🔧 빌드 및 실행

### 사전 요구사항

- Android Studio Hedgehog (2023.1.1) 이상
- JDK 21
- Android SDK 24 이상
- Google Maps API Key

### Google Maps API Key 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
2. Maps SDK for Android 활성화
3. API Key 생성
4. `local.properties` 파일에 추가:

```properties
MAPS_API_KEY=YOUR_API_KEY_HERE
```

### 빌드 명령어

```bash
# 프로젝트 클론
git clone https://github.com/yourusername/AsyncLocationMap.git
cd AsyncLocationMap

# Debug 빌드
./gradlew assembleDebug

# Release 빌드
./gradlew assembleRelease

# 테스트 실행
./gradlew test

# 앱 설치 및 실행
./gradlew installDebug
```

## 🧪 테스트

### Unit Test (74개)

```bash
# 전체 Unit Test 실행
./gradlew test

# 모듈별 테스트
./gradlew :domain:test      # Domain Layer (12개)
./gradlew :data:test        # Data Layer (37개)
./gradlew :presentation:test # Presentation Layer (25개)
```

### 테스트 커버리지

- **Domain Layer**: UseCase 비즈니스 로직 검증
- **Data Layer**: Repository, Mapper, Normalizer, DataSource 검증
- **Presentation Layer**: ViewModel 상태 관리, URL Builder 검증

모든 테스트는 **Fake 객체 기반**으로 작성되어 Mock 프레임워크 없이 실제 동작을 검증합니다.

## 📖 주요 기능 상세

### 1. 비동기 위치 조회 (WorkManager)

```kotlin
// 위치 요청 흐름
사용자 클릭 → ViewModel → UseCase → Repository → WorkManager
→ LocationWorker → FusedLocationProvider → GPS
→ Room Database 저장 → Flow 업데이트 → UI 자동 갱신
```

**장점**:
- 앱이 백그라운드에 있어도 작업 보장
- 시스템이 최적의 시점에 실행
- Doze 모드에서도 동작
- 재시작 후에도 작업 유지

### 2. 위치 정규화 (LocationNormalizer)

GPS 오차로 인한 미세한 좌표 차이를 동일한 위치로 간주:
- 소수점 5자리까지 유지 (약 1.1m 정확도)
- 중복 위치 검색의 정확도 향상
- 데이터베이스 저장 공간 절약

### 3. 실시간 데이터 반영 (Room + Flow)

```kotlin
// Room Query with Flow
@Query("SELECT * FROM locations ORDER BY timestamp DESC")
fun getAllLocations(): Flow<List<LocationEntity>>
```

데이터베이스 변경 시 UI가 자동으로 업데이트됩니다.

### 4. Street View 미리보기

```kotlin
// Street View Static API 사용
https://maps.googleapis.com/maps/api/streetview?
  size=160x80&
  location=37.5665,126.978&
  key=YOUR_API_KEY
```

각 위치 카드에 해당 위치의 Street View 이미지를 표시합니다.

## 🔒 권한

앱이 사용하는 권한:

```xml
<!-- 위치 권한 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Foreground Service (위치 조회용) -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

<!-- 인터넷 (Google Maps 사용) -->
<uses-permission android:name="android.permission.INTERNET" />
```