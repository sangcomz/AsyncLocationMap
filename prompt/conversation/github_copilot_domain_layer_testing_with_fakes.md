# AsyncLocationMap 테스트 코드 작성 대화 전체 기록

## 초기 요청

**User**: 프로젝트를 분석하고 모듈별로 필요한 테스트 코드에 대해서 알려주고, 어떤 식으로 작성할지 계획을 알려줘

**Assistant**: 프로젝트 구조를 분석하고 테스트 계획을 수립했습니다.

Clean Architecture 기반의 AsyncLocationMap 프로젝트에 대한 포괄적인 테스트 전략을 제시했습니다:

### Plan: AsyncLocationMap 테스트 코드 작성 계획 및 구현 완료 보고서

**Steps**:
1. Testing 모듈 생성 및 프로젝트 설정
2. Testing 모듈에 테스트 데이터 작성
3. Testing 모듈에 Fake 구현체 작성
4. Domain 모듈 Unit Test 작성
5. Data 모듈 테스트 작성
6. Presentation 모듈 Unit Test 작성

---

## Mock vs Fake 객체에 대한 논의

**User**: 아 그리고 테스트 코드의 경우 Mock은 최대한 피하고 Fake 객체를 사용하면 좋겠어. 일단 Unit Test까지만 작성해주는 계획으로 다시 해줘

**Assistant**: 좋습니다! Fake 객체를 사용하고 Unit Test에 집중한 테스트 계획을 다시 작성하겠습니다.

### Plan: AsyncLocationMap Unit Test 작성 (Fake 객체 기반)

별도의 `testing` 모듈을 생성하여 모든 Fake 객체와 테스트 데이터를 중앙 집중화합니다. 고정된 테스트 데이터를 사용하고, Flow는 `first()`, `toList()` 같은 간단한 방식으로 검증합니다.

**Steps**:
1. testing 모듈 생성 및 설정
2. testing 모듈에 Fake 객체 작성
3. Domain 모듈 Unit Test 작성
4. Data 모듈 Unit Test 작성
5. Presentation 모듈 Unit Test 작성

---

## 테스트 데이터 및 Flow 테스트 방식 결정

**User**: fake 객체만 있는 모듈을 생성해서 진행하면 좋겠어. 고정된 데이터로 진행하고 싶어. Flow는 일단 간단한 방식으로 테스트하는 계획으로 하고 싶어

**Assistant**: 좋습니다! Fake 객체만 있는 별도 테스트 모듈을 생성하고, 고정 데이터와 간단한 Flow 테스트 방식으로 계획을 수정하겠습니다.

### Plan: AsyncLocationMap Unit Test 작성 (Testing 모듈 기반)

`testing` 모듈을 생성하여 모든 Fake 객체와 테스트 데이터를 중앙화합니다. 고정된 테스트 데이터를 사용하고, Flow는 `first()`, `toList()` 같은 간단한 방식으로 검증합니다.

**Further Considerations**:
1. 모듈 이름: `test-fixtures`가 좋을까요, 아니면 `testing` 또는 `test-common`?
2. Fake 객체의 복잡도
3. 테스트 데이터 개수

---

## 모듈 및 테스트 전략 최종 결정

**User**: 이름은 testing이 좋겠어. 복잡도는 만들어지는 테스트에 통과할 수 있을정도면 될 것 같은데. 테스트 데이터 개수는 엣지 케이스들이 커버될 수 있으면 좋겠어

**Assistant**: 완벽합니다! `testing` 모듈로 진행하고, 엣지 케이스를 커버할 수 있도록 계획을 최종 수정하겠습니다.

### Plan: AsyncLocationMap Unit Test 작성 (Testing 모듈 기반) - 최종

**Steps**:
1. testing 모듈 생성 및 프로젝트 설정 (Kotlin JVM)
2. testing 모듈에 테스트 데이터 객체 작성 (엣지 케이스 포함)
3. testing 모듈에 Fake 구현체 작성
4. Domain 모듈 Unit Test 작성
5. Data 모듈 Unit Test 작성
6. Presentation 모듈 Unit Test 작성

---

## 구현 시작

**User**: 응 Android를 직접적으로 쓰고 있는 객체는 진행하지 않아도 돼, 순서대로 작성해줘. 더 필요한 엣지케이스는 모르겠어.

**Assistant**: 완벽합니다! Android 의존성이 있는 클래스는 제외하고, Domain → Data → Presentation 순서로 진행하겠습니다.

**User**: Start implementation

**Assistant**: 구현을 시작했습니다.

### 구현 완료 내용:

1. **testing 모듈 생성** (`testing/build.gradle.kts`)
2. **settings.gradle.kts에 testing 모듈 추가**
3. **각 모듈의 build.gradle.kts에 testing 의존성 추가**
4. **TestLocations.kt 생성** - 10가지 시나리오의 테스트 데이터
5. **Fake 구현체 6개 생성**:
   - FakeLocationRepository
   - FakeLocationLocalDataSource
   - FakeLocationRemoteDataSource
   - FakeLocationDao
   - FakeObserveLastLocationUseCase
   - FakeRequestLocationUpdateUseCase
6. **Domain 모듈 테스트 작성** (12개 테스트)
7. **Data 모듈 테스트 작성** (37개 테스트)
8. **Presentation 모듈 테스트 작성** (25개 테스트)

총 **74개의 Unit Test** 작성 완료

---

## UseCase 테스트 전략에 대한 중요한 수정

**User**: UseCase는 실제 구현체를 사용하고, 그 파라미터들의 경우에만 Fake를 쓰면 되지 않아?

**Assistant**: 아! 정확히 맞습니다. UseCase는 실제 구현체를 사용하고, 그 의존성(Repository)만 Fake로 주입하는 것이 맞습니다.

수정 내용:
- MapViewModelTest에서 Fake UseCase 제거
- 실제 UseCase 인스턴스를 생성하고 Fake Repository 주입
- FakeObserveLastLocationUseCase.kt 삭제
- FakeRequestLocationUpdateUseCase.kt 삭제

### 수정 전 (잘못된 방식):
```kotlin
private lateinit var observeUseCase: FakeObserveLastLocationUseCase
private lateinit var requestUseCase: FakeRequestLocationUpdateUseCase
```

### 수정 후 (올바른 방식):
```kotlin
private lateinit var fakeRepository: FakeLocationRepository
private lateinit var observeUseCase: ObserveLastLocationUseCase
private lateinit var requestUseCase: RequestLocationUpdateUseCase

@Before
fun setup() {
    fakeRepository = FakeLocationRepository()
    observeUseCase = ObserveLastLocationUseCase(fakeRepository)
    requestUseCase = RequestLocationUpdateUseCase(fakeRepository)
    viewModel = MapViewModel(observeUseCase, requestUseCase)
}
```

**핵심 원칙**: UseCase는 비즈니스 로직이므로 실제 코드를 테스트해야 함. Fake를 만들면 실제 로직을 테스트하지 못함.

---

## Testing 모듈의 인터페이스 구현 문제

**User**: 이 케이스에서 FakeLocationLocalDataSource LocationLocalDataSource를 구현하고 있지 않아서 LocationRepositoryImpl에서 받을 수가 없어. testing 모듈의 의존성을 갖고, 실제 구현체를 구현해야하지 않아?

**Assistant**: 완전히 맞습니다! 정확한 지적입니다. `FakeLocationLocalDataSource`가 `LocationLocalDataSource` 인터페이스를 구현하지 않으면 `LocationRepositoryImpl`에 주입할 수 없습니다.

### 문제점:
```
testing/ [Kotlin JVM]
  ↓ (의존 시도)
data/ [Android Library]  ❌ 불가능!
```

Kotlin JVM 모듈은 Android 모듈에 의존할 수 없음.

### 해결 방법:

**testing 모듈을 Android Library로 변경**

```kotlin
// testing/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)  // Kotlin JVM → Android Library
    alias(libs.plugins.kotlin.android)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))  // 이제 data 모듈 의존 가능!
}
```

**Fake 클래스들이 실제 인터페이스 구현**:
```kotlin
class FakeLocationLocalDataSource : LocationLocalDataSource {
    override fun getAllLocations(): Flow<List<Location>> { ... }
    override suspend fun insertLocation(location: Location) { ... }
}
```

이제 타입이 일치하여 주입 가능!

---

## LocationNormalizer 테스트 실패 수정

**User**: 
```kotlin
@Test
fun `normalize should make similar coordinates equal`() {
    val coord1 = 37.123456
    val coord2 = 37.123454
    val normalized1 = LocationNormalizer.normalize(coord1)
    val normalized2 = LocationNormalizer.normalize(coord2)
    assertEquals(normalized1, normalized2, 0.000001)
}
```
이 테스트에 실패해

**Assistant**: 이 테스트가 실패하는 이유를 확인하고 수정하겠습니다.

**문제 원인**:
- `37.123456 * 100000 = 3712345.6` → `roundToInt()` → `3712346` → `/100000` = `37.12346`
- `37.123454 * 100000 = 3712345.4` → `roundToInt()` → `3712345` → `/100000` = `37.12345`

결과가 **다릅니다**! 두 값은 소수점 5자리 자체가 다름 (37.12345**6** vs 37.12345**4**)

**수정 후**:
```kotlin
@Test
fun `normalize should make similar coordinates equal`() {
    // 소수점 5자리까지는 동일하고, 6자리 이후가 다른 값
    val coord1 = 37.123450001  // 반올림 → 37.12345
    val coord2 = 37.123449999  // 반올림 → 37.12345
    
    val normalized1 = LocationNormalizer.normalize(coord1)
    val normalized2 = LocationNormalizer.normalize(coord2)
    
    assertEquals(37.12345, normalized1, 0.000001)
    assertEquals(37.12345, normalized2, 0.000001)
    assertEquals(normalized1, normalized2, 0.000001)
}
```

---

## MapViewModelTest 실패 원인 분석 및 해결

**User**: MapViewModelTest에 여러개 실패하는데, 실패 원인이랑 알려줘, 그리고 해결 방법을 제시하고 실행하면 좋겠어

**Assistant**: MapViewModelTest의 실패 원인을 분석하고 해결하겠습니다.

### 실패 원인:

**핵심 문제: Coroutine 테스트에서 `advanceUntilIdle()` 누락**

MapViewModel의 `init` 블록에서 `viewModelScope.launch`로 비동기 작업을 시작하지만, 테스트에서 이를 기다리지 않아 상태가 예상과 다르게 동작합니다.

```kotlin
class MapViewModel @Inject constructor(...) : ViewModel() {
    init {
        observeLocations()  // viewModelScope.launch { ... }
    }
    
    private fun observeLocations() {
        viewModelScope.launch {  // 비동기 실행!
            observeLastLocationUseCase()
                .collect { ... }
        }
    }
}
```

### 해결 방법:

1. **setup()에서 init 블록 완료 대기**:
```kotlin
@Before
fun setup() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeLocationRepository()
    observeLastLocationUseCase = ObserveLastLocationUseCase(fakeRepository)
    requestLocationUpdateUseCase = RequestLocationUpdateUseCase(fakeRepository)
    viewModel = MapViewModel(observeLastLocationUseCase, requestLocationUpdateUseCase)
    
    // ✅ init 블록의 observeLocations() 완료 대기
    testDispatcher.scheduler.advanceUntilIdle()
}
```

2. **각 테스트에서 advanceUntilIdle() 호출**:
```kotlin
@Test
fun `toggleBottomSheet should change visibility`() = runTest {
    advanceUntilIdle()  // ✅ 초기 상태 안정화
    assertFalse(viewModel.uiState.value.isBottomSheetVisible)
    
    viewModel.toggleBottomSheet()
    advanceUntilIdle()  // ✅ 상태 업데이트 대기
    
    assertTrue(viewModel.uiState.value.isBottomSheetVisible)
}
```

**핵심 원칙**:
- `StandardTestDispatcher`는 명시적으로 `advanceUntilIdle()`을 호출해야 대기 중인 코루틴을 실행
- 모든 상태 변경 후 `advanceUntilIdle()` 호출
- init 블록도 테스트 대상이므로 ViewModel 생성 직후 대기 필요

수정된 테스트 10개:
1. setup() - init 블록 완료 대기 추가
2. initial state should be empty and not loading
3. toggleBottomSheet should change visibility
4. hideBottomSheet should close bottom sheet
5. onLocationSelected should set selected location and close bottom sheet
6. clearSelectedLocation should reset selected location
7. clearError should remove error message

---

## Domain 모듈의 Testing 의존성 문제

**User**: 궁금한게 도메인 레이어의 UseCase를 상위 모듈에서 테스트를 해도 괜찮을려나?

**Assistant**: 좋은 질문입니다! 도메인 레이어의 UseCase를 상위 모듈(Presentation)에서 테스트하는 것에 대해 분석해드리겠습니다.

### 결론: **각 레이어는 자신의 테스트를 가져야 합니다** ⭐

### UseCase를 Presentation 테스트에서만 테스트하는 경우 (❌)

**문제점**:
1. **책임 불명확**: UseCase의 버그인지 ViewModel의 버그인지 구분 어려움
2. **테스트 범위 과다**: ViewModel 테스트가 UseCase + ViewModel 둘 다 테스트
3. **피드백 느림**: Presentation 테스트 실패 시 어디서 문제인지 찾기 어려움
4. **재사용성 낮음**: 다른 곳에서 같은 UseCase 사용 시 중복 테스트 필요
5. **의존성 증가**: Domain 레이어 변경 시 Presentation 테스트도 영향

### 각 레이어에서 독립적으로 테스트하는 경우 (✅)

**장점**:
1. **빠른 피드백**: Domain 테스트 실패 → UseCase 문제 즉시 파악
2. **명확한 책임**: 각 레이어의 테스트가 명확히 분리
3. **독립적 개발**: Domain 개발자가 Presentation 걱정 없이 테스트 작성
4. **리팩토링 안전**: UseCase 변경 시 Domain 테스트만 수정
5. **테스트 속도**: Domain 테스트가 Presentation보다 훨씬 빠름

### 현재 프로젝트 구조 (올바름):
```
MapViewModelTest
  ↓ 테스트
MapViewModel (실제)
  ↓ 사용
ObserveLastLocationUseCase (실제) ← Domain과 동일한 UseCase
  ↓ 사용
FakeLocationRepository
```

**이것이 올바른 구조입니다!**

**이유**:
1. **Domain 테스트**: UseCase 자체의 로직 검증
2. **Presentation 테스트**: ViewModel + UseCase 통합 동작 검증
3. **중복이 아닌 계층별 책임**: 각각 다른 관점에서 테스트

### 현재 문제: Domain 테스트에서 Testing 모듈 사용 불가

```
domain/ [Kotlin JVM]
  ↓ (의존 시도)
testing/ [Android Library]  ❌ 불가능!
```

**해결 옵션 제시**:

**옵션 1: Testing 모듈을 두 개로 분리** ⭐ (추천)
```
testing-core/     [Kotlin JVM] - Domain/순수 Kotlin용
testing-android/  [Android Library] - Data/Presentation용
```

**옵션 2: Domain 테스트에서 Fake를 직접 작성**
```
domain/src/test/
  ├── FakeLocationRepository.kt (testing 모듈과 동일한 코드 복사)
  └── ObserveLastLocationUseCaseTest.kt
```

**옵션 3: Testing 모듈을 Kotlin JVM으로 되돌리기**
(인터페이스 직접 정의)

**옵션 4: DataSource 인터페이스를 Domain으로 이동** (Clean Architecture 원칙)
(대규모 리팩토링 필요)

**추천 순서**:
1. 옵션 1 (Testing 모듈 분리) - 가장 깔끔하고 확장 가능
2. 옵션 2 (Domain에 Fake 직접 작성) - 가장 빠르고 간단
3. 옵션 4 (인터페이스 Domain으로 이동) - 가장 올바르지만 작업량 많음
4. 옵션 3 (인터페이스 복사) - 비추천 (중복 관리)

---

## Domain 모듈 독립적 테스트 구현

**User**: 응 도메인의 경우엔 바로 Fake를 사용하는 방식으로 해줘

**Assistant**: 알겠습니다! Domain 모듈에 Fake를 직접 작성하여 testing 모듈 의존성을 제거하겠습니다.

**User**: Start implementation

**Assistant**: 구현을 시작했습니다.

### 구현 완료:

1. **FakeLocationRepository.kt 생성**
   - 경로: `domain/src/test/kotlin/.../fake/FakeLocationRepository.kt`
   - LocationRepository 인터페이스 구현
   - 메모리 기반, Flow 지원, 테스트 헬퍼 제공

2. **TestLocations.kt 생성**
   - 경로: `domain/src/test/kotlin/.../fake/TestLocations.kt`
   - Domain 테스트용 최소 데이터 (SEOUL, BUSAN, JEJU)

3. **테스트 파일 수정**
   - ObserveLastLocationUseCaseTest.kt - import를 `domain.fake.*`로 변경
   - RequestLocationUpdateUseCaseTest.kt - import를 `domain.fake.*`로 변경

4. **domain/build.gradle.kts 수정**
   - `testImplementation(project(":testing"))` 제거
   - Android 의존성 완전 제거

### 최종 구조:
```
Domain [Kotlin JVM] ← 독립적!
  └── domain.fake.* (자체 Fake)

Data [Android Library]
  └── testing.* (공유 모듈)

Presentation [Android Library]
  └── testing.* (공유 모듈)
```

### 이점:
- ✅ 완전한 독립성: Domain이 Android에 의존하지 않음
- ✅ 빠른 테스트: 순수 JVM, Android 환경 불필요
- ✅ 간단한 구조: 필요한 Fake만 유지
- ✅ 모듈 원칙 준수: Kotlin JVM ← Kotlin JVM

---

## README.md 작성

**User**: 앱 사용 방법에 대한 README.md 만 작성해줘

**Assistant**: 앱 사용 방법에 대한 README.md를 작성하겠습니다.

### README.md 주요 내용:

1. **앱 소개**: 프로젝트 개요, 배지, 주요 기능 6가지
2. **사용 방법**: 
   - 초기 실행
   - 위치 권한 허용
   - 현재 위치 조회
   - 위치 히스토리 확인
   - 특정 위치로 이동
   - 마커 클러스터링
3. **기술 스택**: UI, 아키텍처, 비동기 처리, 로컬 저장소, 의존성 주입, 위치 서비스
4. **프로젝트 구조**: 전체 모듈 구조 트리
5. **빌드 및 실행**: 사전 요구사항, Google Maps API Key 설정, 빌드 명령어
6. **테스트**: Unit Test 74개, 모듈별 실행 방법, 커버리지
7. **주요 기능 상세**: WorkManager, 위치 정규화, Room + Flow, Street View
8. **권한**: 앱에서 사용하는 모든 권한 목록

---

## 최종 요약

### 완성된 테스트 코드 통계:
- **총 테스트 케이스**: 74개
- **Domain Layer**: 12개 테스트
- **Data Layer**: 37개 테스트
- **Presentation Layer**: 25개 테스트

### 생성된 주요 파일:
- testing 모듈 (Android Library)
- domain/src/test/fake/ (독립적 Fake)
- 8개 테스트 클래스
- 상세 문서 7개
- README.md

### 핵심 원칙:
1. **Fake 객체 사용**: Mock 대신 실제 동작 구현
2. **UseCase는 실제 구현체**: 의존성만 Fake로 주입
3. **각 레이어 독립적 테스트**: Domain은 자체 Fake 사용
4. **엣지 케이스 커버**: 경계값, 음수, 중복 등
5. **간단한 Flow 테스트**: first(), toList() 사용
6. **Coroutine 테스트**: advanceUntilIdle() 적절히 사용

### 생성된 문서:
1. TEST_SUMMARY.md - 전체 요약 보고서
2. TEST_IMPLEMENTATION.md - 상세 구현 내용
3. TEST_STRATEGY_IMPROVEMENT.md - UseCase 전략 개선
4. TESTING_MODULE_ARCHITECTURE.md - 모듈 아키텍처 결정
5. MAPVIEWMODEL_TEST_FIX.md - ViewModel 테스트 수정
6. DOMAIN_FAKE_IMPLEMENTATION.md - Domain Fake 구현
7. DOMAIN_TEST_COMPLETE.md - Domain 테스트 완료
8. README.md - 앱 사용 방법

모든 테스트 코드가 성공적으로 작성 완료되었습니다! 🎉

