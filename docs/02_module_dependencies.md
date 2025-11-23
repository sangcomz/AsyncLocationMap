# 모듈 구조 및 의존성 계획

## 1. 모듈 의존성 그래프

```
app
 ├─> presentation
 ├─> domain
 └─> data

presentation
 └─> domain

data
 └─> domain

domain (독립 모듈)
```

## 2. 모듈별 상세 설정

### app 모듈
**타입**: Android Application

**역할**:
- Application 진입점
- Hilt Application 설정
- 모든 모듈 통합

**주요 의존성**:
- presentation
- domain
- data
- hilt-android
- hilt-compiler (kapt)

**주요 파일**:
- `MyApplication.kt`: @HiltAndroidApp
- `MainActivity.kt`: setContent로 Compose 시작
- `AndroidManifest.xml`: 권한 및 Google Maps API Key 설정

---

### presentation 모듈
**타입**: Android Library

**역할**:
- UI 레이어 (Compose)
- ViewModel
- UI State 관리
- 권한 처리

**주요 의존성**:
- domain (interface만 의존)
- compose-ui
- compose-material3
- maps-compose (Google Maps for Compose)
- hilt-android
- hilt-navigation-compose
- lifecycle-viewmodel-compose
- accompanist-permissions

**주요 컴포넌트**:
```
presentation/
├── ui/
│   └── map/
│       ├── MapScreen.kt
│       ├── MapViewModel.kt
│       └── MapUiState.kt
└── di/
    └── PresentationModule.kt (필요시)
```

---

### domain 모듈
**타입**: Kotlin Library (순수 Kotlin, Android 의존성 없음)

**역할**:
- 비즈니스 로직
- Domain Model 정의
- Repository Interface 정의
- Use Case 정의

**주요 의존성**:
- kotlin-stdlib
- coroutines-core
- javax.inject (Hilt annotations)

**주요 컴포넌트**:
```
domain/
├── model/
│   └── Location.kt
│       data class Location(
│           val id: Long = 0,
│           val latitude: Double,
│           val longitude: Double,
│           val timestamp: Long
│       )
├── repository/
│   └── LocationRepository.kt (interface)
└── usecase/
    ├── GetLocationsUseCase.kt
    ├── SaveLocationUseCase.kt
    └── RequestLocationUpdateUseCase.kt
```

---

### data 모듈
**타입**: Android Library

**역할**:
- Repository 구현
- Room Database
- WorkManager Worker
- 데이터 소스 구현

**주요 의존성**:
- domain
- room-runtime
- room-ktx
- room-compiler (kapt)
- work-runtime-ktx
- hilt-android
- hilt-work
- play-services-location
- coroutines-android

**주요 컴포넌트**:
```
data/
├── repository/
│   └── LocationRepositoryImpl.kt
├── local/
│   ├── db/
│   │   └── LocationDatabase.kt
│   ├── dao/
│   │   └── LocationDao.kt
│   └── entity/
│       └── LocationEntity.kt
├── mapper/
│   └── LocationMapper.kt (Entity <-> Domain Model)
├── worker/
│   └── LocationWorker.kt
└── di/
    ├── DatabaseModule.kt
    ├── RepositoryModule.kt
    └── WorkManagerModule.kt
```

---

## 3. Version Catalog 구조

**gradle/libs.versions.toml**:

```toml
[versions]
agp = "8.13.0"
kotlin = "2.2.21"
compose-bom = "2025.10.01"
hilt = "2.57.1"
room = "2.8.4"
work = "2.11.0"
maps-compose = "6.12.2"
play-services-location = "21.3.0"
accompanist = "0.37.3"
lifecycle = "2.10.0"
coroutines = "1.10.0"

[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.15.0" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-activity = { group = "androidx.activity", name = "activity-compose", version = "1.9.3" }

# Lifecycle
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# WorkManager
work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# Google Maps
maps-compose = { group = "com.google.maps.android", name = "maps-compose", version.ref = "maps-compose" }
play-services-maps = { group = "com.google.android.gms", name = "play-services-maps", version = "19.0.0" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "play-services-location" }

# Accompanist (Permissions)
accompanist-permissions = { group = "com.google.accompanist", name = "accompanist-permissions", version.ref = "accompanist" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Javax Inject
javax-inject = { group = "javax.inject", name = "javax.inject", version = "1" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

---

## 4. Gradle 설정 요약

### settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AsyncLocationMap"
include(":app")
include(":presentation")
include(":domain")
include(":data")
```

### 각 모듈의 build.gradle.kts 플러그인

- **app**: `android-application`, `kotlin-android`, `kotlin-kapt`, `hilt`, `compose-compiler`
- **presentation**: `android-library`, `kotlin-android`, `kotlin-kapt`, `hilt`, `compose-compiler`
- **domain**: `kotlin-jvm`, `kotlin-kapt`
- **data**: `android-library`, `kotlin-android`, `kotlin-kapt`, `hilt`

---

## 5. 모듈 간 데이터 흐름

```
[Presentation Layer]
    MapViewModel
        ↓ (UseCase 호출)

[Domain Layer]
    RequestLocationUpdateUseCase
    GetLocationsUseCase
    SaveLocationUseCase
        ↓ (Repository Interface)

[Data Layer]
    LocationRepositoryImpl
        ↓
    LocationWorker (WorkManager)
    LocationDao (Room)
```

**특징**:
- Domain은 다른 모듈에 의존하지 않음 (순수 Kotlin)
- Presentation과 Data는 Domain에만 의존
- App은 모든 모듈을 통합