# 구현 단계별 계획

## 단계 1: 프로젝트 초기 설정

### 1.1 프로젝트 구조 생성
- [ ] Android Studio에서 Empty Activity 프로젝트 생성
- [ ] 모듈 생성
  - [ ] presentation 모듈 (Android Library)
  - [ ] domain 모듈 (Kotlin Library)
  - [ ] data 모듈 (Android Library)

### 1.2 Version Catalog 설정
- [ ] `gradle/libs.versions.toml` 파일 생성
- [ ] 모든 의존성 및 플러그인 버전 정의

### 1.3 Root 및 각 모듈 build.gradle.kts 설정
- [ ] settings.gradle.kts 설정
- [ ] app/build.gradle.kts
- [ ] presentation/build.gradle.kts
- [ ] domain/build.gradle.kts
- [ ] data/build.gradle.kts

### 1.4 AndroidManifest.xml 설정
- [ ] 위치 권한 추가
- [ ] Google Maps API Key 추가
- [ ] 인터넷 권한 추가

---

## 단계 2: Domain Layer 구현

### 2.1 Domain Model 생성
- [ ] `domain/model/Location.kt` 생성

### 2.2 Repository Interface 정의
- [ ] `domain/repository/LocationRepository.kt` 생성

### 2.3 Use Cases 구현
- [ ] `domain/usecase/GetLocationsUseCase.kt`
- [ ] `domain/usecase/RequestLocationUpdateUseCase.kt`

---

## 단계 3: Data Layer 구현

### 3.1 Room Database 설정
- [ ] `data/local/entity/LocationEntity.kt` 생성
- [ ] `data/local/dao/LocationDao.kt` 생성
- [ ] `data/local/db/LocationDatabase.kt` 생성

### 3.2 Mapper 구현
- [ ] `data/mapper/LocationMapper.kt` 생성

### 3.3 WorkManager Worker 구현
- [ ] `data/worker/LocationWorker.kt` 생성

### 3.4 Repository 구현
- [ ] `data/repository/LocationRepositoryImpl.kt` 생성

### 3.5 Dependency Injection (Data Module)
- [ ] `data/di/DatabaseModule.kt`
  - LocationDatabase 제공
  - LocationDao 제공
- [ ] `data/di/RepositoryModule.kt`
  - LocationRepository 바인딩
- [ ] `data/di/LocationModule.kt`
  - FusedLocationProviderClient 제공
  - WorkManager 제공

---

## 단계 4: Presentation Layer 구현

### 4.1 UI State 정의
- [ ] `presentation/ui/map/MapUiState.kt` 생성

### 4.2 ViewModel 구현
- [ ] `presentation/ui/map/MapViewModel.kt` 생성

### 4.3 Compose UI 구현
- [ ] `presentation/ui/map/MapScreen.kt` 생성
  - GoogleMap 컴포저블
  - 마커 표시
  - FloatingActionButton (현재 위치 버튼)
  - 권한 처리

### 4.4 리소스 파일
- [ ] drawable 리소스 (현재 위치 아이콘)
- [ ] strings.xml

---

## 단계 5: App Module 구성

### 5.1 Application 클래스
- [ ] `app/MyApplication.kt` 생성
  - @HiltAndroidApp 어노테이션

### 5.2 MainActivity
- [ ] `app/MainActivity.kt` 수정
  - @AndroidEntryPoint
  - setContent로 MapScreen 호출

### 5.3 Hilt 설정
- [ ] @HiltAndroidApp
- [ ] @HiltWorker

---

## 단계 6: 통합 및 테스트

### 6.1 기본 기능 테스트
- [ ] 앱 빌드 및 실행
- [ ] 지도 화면 표시 확인
- [ ] "현재 위치" 버튼 클릭
- [ ] 권한 요청 다이얼로그 확인
- [ ] 권한 허용 후 위치 조회
- [ ] 마커가 지도에 표시되는지 확인

### 6.2 백그라운드 동작 테스트
- [ ] 앱을 백그라운드로 전환
- [ ] WorkManager가 계속 실행되는지 확인
- [ ] 다시 앱으로 돌아와 마커 확인

### 6.3 DB 저장 확인
- [ ] 여러 번 위치 조회
- [ ] Room DB에 여러 위치가 저장되는지 확인
- [ ] 앱 재시작 후 마커 유지 확인

---

## 단계 7: 최적화 및 마무리

### 7.1 코드 정리
- [ ] Lint 경고 해결
- [ ] 불필요한 코드 제거
- [ ] 코드 포맷팅

### 7.2 에러 처리 개선
- [ ] 위치 조회 실패 시 UI 피드백
- [ ] 네트워크 오류 처리
- [ ] 권한 거부 시 안내 메시지

### 7.3 UI/UX 개선
- [ ] 로딩 인디케이터 추가
- [ ] 지도 카메라 위치 자동 이동
- [ ] 마커 스타일링

---

## 구현 순서 요약

```
1. 프로젝트 초기 설정
   ├─ 모듈 생성
   ├─ Version Catalog
   └─ build.gradle.kts 설정

2. Domain Layer (순수 Kotlin)
   ├─ Model
   ├─ Repository Interface
   └─ Use Cases

3. Data Layer
   ├─ Room (Entity, Dao, Database)
   ├─ Mapper
   ├─ Worker
   ├─ Repository Implementation
   └─ DI Modules

4. Presentation Layer
   ├─ UiState
   ├─ ViewModel
   └─ Compose UI

5. App Module
   ├─ Application
   ├─ MainActivity
   └─ Hilt 설정

6. 통합 테스트

7. 최적화
```

---

## 주요 체크포인트

### Google Maps API Key 발급
1. Google Cloud Console 접속
2. 프로젝트 생성
3. Maps SDK for Android 활성화
4. API Key 생성
5. AndroidManifest.xml에 추가

### 권한 설정 확인
- AndroidManifest.xml:
  ```xml
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  <uses-permission android:name="android.permission.INTERNET" />
  ```

### Hilt 설정 확인
- Application 클래스에 `@HiltAndroidApp`
- MainActivity에 `@AndroidEntryPoint`
- ViewModel에 `@HiltViewModel`
- Worker에 `@HiltWorker`

### Room 마이그레이션
- 초기 버전이므로 마이그레이션 불필요
- `exportSchema = false` 설정

---

## 예상 파일 구조

```
AsyncLocationMap/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/asynclocationmap/
│   │   │   ├── MyApplication.kt
│   │   │   └── MainActivity.kt
│   │   ├── AndroidManifest.xml
│   │   └── res/
│   └── build.gradle.kts
│
├── presentation/
│   ├── src/main/java/com/example/asynclocationmap/presentation/
│   │   └── ui/map/
│   │       ├── MapScreen.kt
│   │       ├── MapViewModel.kt
│   │       └── MapUiState.kt
│   └── build.gradle.kts
│
├── domain/
│   ├── src/main/java/com/example/asynclocationmap/domain/
│   │   ├── model/
│   │   │   └── Location.kt
│   │   ├── repository/
│   │   │   └── LocationRepository.kt
│   │   └── usecase/
│   │       ├── GetLocationsUseCase.kt
│   │       └── RequestLocationUpdateUseCase.kt
│   └── build.gradle.kts
│
├── data/
│   ├── src/main/java/com/example/asynclocationmap/data/
│   │   ├── local/
│   │   │   ├── entity/
│   │   │   │   └── LocationEntity.kt
│   │   │   ├── dao/
│   │   │   │   └── LocationDao.kt
│   │   │   └── db/
│   │   │       └── LocationDatabase.kt
│   │   ├── mapper/
│   │   │   └── LocationMapper.kt
│   │   ├── repository/
│   │   │   └── LocationRepositoryImpl.kt
│   │   ├── worker/
│   │   │   └── LocationWorker.kt
│   │   └── di/
│   │       ├── DatabaseModule.kt
│   │       ├── RepositoryModule.kt
│   │       └── LocationModule.kt
│   └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts
```

---

## 추가 고려사항

### 1. 위치 정확도
- FusedLocationProviderClient는 최적의 위치 제공자 자동 선택
- GPS, 네트워크, Wi-Fi 등 활용

### 2. 배터리 최적화
- WorkManager는 시스템이 최적 시점에 실행
- Doze 모드 고려

### 3. 지도 카메라 설정
- 첫 마커 추가 시 카메라 이동
- CameraPosition 및 CameraUpdate 활용

### 4. 확장 가능성
- 위치 기록 삭제 기능
- 위치 기록 내보내기
- 실시간 위치 추적 (주기적 업데이트)