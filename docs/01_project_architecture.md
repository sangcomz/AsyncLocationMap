# 프로젝트 아키텍처 설계

## 1. 프로젝트 개요
**목표**: 지도뷰에서 비동기적으로 현재 위치를 표시하는 안드로이드 앱 개발

## 2. 기술 스택
- **UI**: Jetpack Compose
- **지도 UI**: Google Maps Compose (maps-compose)
- **아키텍처 패턴**: MVVM + Clean Architecture
- **비동기 처리**: Kotlin Coroutines
- **백그라운드 작업**: WorkManager + HiltWorker
- **로컬 DB**: Room
- **지도**: Google Maps SDK for Android
- **의존성 주입**: Hilt (Dagger)
- **빌드 시스템**: Version Catalog

## 3. 모듈 구조 (Clean Architecture)

```
AsyncLocationMap/
├── app/                    # Application 모듈
│   ├── di/                # Application level DI
│   └── MyApplication.kt   # Application class
│
├── presentation/          # Presentation 레이어
│   ├── ui/
│   │   ├── map/
│   │   │   ├── MapScreen.kt
│   │   │   ├── MapViewModel.kt
│   │   │   └── MapUiState.kt
│   │   └── components/
│   └── di/               # Presentation DI
│
├── domain/               # Domain 레이어 (비즈니스 로직)
│   ├── model/
│   │   └── Location.kt  # Domain model
│   ├── repository/
│   │   └── LocationRepository.kt  # Repository interface
│   └── usecase/
│       ├── GetLocationUseCase.kt
│       └── SaveLocationUseCase.kt
│
└── data/                 # Data 레이어
    ├── repository/
    │   └── LocationRepositoryImpl.kt
    ├── local/
    │   ├── db/
    │   │   └── LocationDatabase.kt
    │   ├── dao/
    │   │   └── LocationDao.kt
    │   └── entity/
    │       └── LocationEntity.kt
    ├── worker/
    │   └── LocationWorker.kt
    └── di/              # Data DI
```

## 4. 레이어별 책임

### App 모듈
- Application 클래스 정의
- Hilt 설정
- 모든 모듈 통합

### Presentation 모듈
- UI 컴포넌트 (Compose)
- ViewModel (UI 로직)
- UiState 관리
- 권한 요청 처리

### Domain 모듈
- 비즈니스 모델 정의
- Repository 인터페이스
- Use Case (비즈니스 로직)
- 다른 레이어에 의존하지 않음

### Data 모듈
- Repository 구현
- Room Database 구현
- WorkManager Worker 구현
- 외부 데이터 소스 처리

## 5. 주요 컴포넌트

### MapViewModel
```kotlin
data class MapUiState(
    val currentLocation: Location? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val markers: List<Location> = emptyList()
)
```

### LocationWorker (HiltWorker)
- `@HiltWorker` 어노테이션으로 Hilt 통합
- `@AssistedInject`를 통한 의존성 주입
- WorkManager를 통한 백그라운드 위치 조회
- Room DB에 위치 정보 저장
- 앱이 백그라운드에 있어도 작업 지속
- Repository와 FusedLocationProviderClient를 생성자로 주입받음

### LocationRepository
- 위치 정보 조회 및 저장 추상화
- Room DB 접근
- 코루틴을 통한 비동기 처리

## 6. 데이터 흐름

```
User Action (버튼 클릭)
    ↓
ViewModel (권한 확인)
    ↓
WorkManager 작업 시작
    ↓
LocationWorker (위치 조회)
    ↓
Repository (Room DB 저장)
    ↓
ViewModel (DB에서 조회)
    ↓
UI 업데이트 (지도에 마커 표시)
```

## 7. 권한 처리
- 위치 권한: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- 백그라운드 위치 권한 (Android 10+): `ACCESS_BACKGROUND_LOCATION`
- "현재 위치" 버튼 클릭 시 권한 요청
- Accompanist Permissions 라이브러리 활용

## 8. 핵심 기능 구현 방식

### 비동기 처리
- ViewModel: `viewModelScope`
- Repository: `suspend` 함수
- Room: `@Query` with suspend
- WorkManager: 백그라운드 작업 자동 관리

### 백그라운드 작업 지속성
- WorkManager의 `OneTimeWorkRequest` 사용
- Constraints를 통한 네트워크/배터리 조건 설정 가능
- Work의 결과를 DB에 저장하여 UI와 분리

### UI 상태 관리
- 단일 `MapUiState` 사용
- ViewModel의 `StateFlow`로 상태 emit
- Compose의 `collectAsState()`로 UI 반영

## 9. 테스트 가능성 및 확장성 설계 원칙

### 의존성 역전 원칙 (Dependency Inversion)
- 모든 레이어는 구체 클래스가 아닌 인터페이스에 의존
- Repository, UseCase 등 모든 컴포넌트를 인터페이스로 추상화
- 테스트 시 Mock 객체로 쉽게 교체 가능

### 생성자 주입 (Constructor Injection)
- Hilt를 통한 의존성 주입
- 모든 의존성은 생성자를 통해 주입
- 테스트 더블(Mock, Fake, Stub) 사용 용이

### 단일 책임 원칙 (Single Responsibility)
- 각 UseCase는 하나의 비즈니스 로직만 담당
- ViewModel은 UI 상태 관리만 담당
- Repository는 데이터 접근 로직만 담당

### 확장 가능한 구조
- **UseCase 추가**: 새로운 기능은 새로운 UseCase로 독립적으로 추가
- **Data Source 추상화**: 로컬/원격 데이터 소스를 쉽게 교체 가능
- **Strategy Pattern**: 위치 제공자, 내보내기 전략 등을 인터페이스로 분리
- **sealed class**: UI 상태, 에러 타입 등을 타입 안전하게 확장

### 테스트 전략
- **Unit Test**: Domain Layer (UseCase, Repository interface) 100% 커버리지 목표
- **Integration Test**: Data Layer (Repository 구현, Room Dao) 80%+ 커버리지
- **UI Test**: Presentation Layer (ViewModel, Compose UI) 주요 시나리오 테스트
- **MockK**: Kotlin 친화적인 Mocking 프레임워크 사용
- **Turbine**: Flow 테스트를 위한 라이브러리 활용

상세 내용은 `docs/05_testability_extensibility.md` 참조