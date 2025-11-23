# AsyncLocationMap UI 개선 대화 전체 기록

## API Key 숨기기

**User**: API Key를 바로 코드에서 호출하는데, 숨겨줘

**Assistant**: API 키를 숨기기 위해 프로젝트 구조를 확인했습니다.

### 문제 상황:
MapScreen.kt에서 API 키가 하드코딩되어 있었습니다:
```kotlin
LocationHistoryBottomSheet(
    locations = uiState.locations,
    apiKey = "AIzaSyBCZQ7-YZtBtY9H5LJo7DTYcZDfYvtVBro",  // ❌ 하드코딩
    onLocationClick = { locationId -> viewModel.onLocationSelected(locationId) },
    onDismiss = { viewModel.hideBottomSheet() }
)
```

### 해결 방법:

1. **local.properties에 API 키 저장** (이미 되어 있음):
```properties
MAPS_API_KEY=AIzaSyBCZQ7-YZtBtY9H5LJo7DTYcZDfYvtVBro
```

2. **presentation/build.gradle.kts에 BuildConfig 설정 추가**:
```kotlin
android {
    defaultConfig {
        // Google Maps API Key
        val mapsApiKey = project.findProperty("MAPS_API_KEY") as String? ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }
    
    buildFeatures {
        compose = true
        buildConfig = true  // BuildConfig 활성화
    }
}
```

3. **MapScreen.kt에서 BuildConfig 사용**:
```kotlin
LocationHistoryBottomSheet(
    locations = uiState.locations,
    apiKey = io.github.sangcomz.asynclocationmap.presentation.BuildConfig.MAPS_API_KEY,  // ✅ BuildConfig 사용
    onLocationClick = { locationId -> viewModel.onLocationSelected(locationId) },
    onDismiss = { viewModel.hideBottomSheet() }
)
```

### 결과:
- ✅ API 키가 코드에서 제거됨
- ✅ local.properties에서 관리 (Git에 포함되지 않음)
- ✅ BuildConfig를 통해 안전하게 접근

---

## 시스템 테마 지원 추가

**User**: 현재 시스템 테마에 맞게 UI가 나오게 해줘

**Assistant**: 시스템 테마(다크 모드)를 지원하도록 Theme.kt를 수정했습니다.

### 기존 코드:
```kotlin
private val LightColorScheme = lightColorScheme()

@Composable
fun AsyncLocationMapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,  // ❌ 항상 라이트 모드
        typography = Typography,
        content = content
    )
}
```

### 수정 후:
```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun AsyncLocationMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // ✅ 시스템 설정 자동 감지
    dynamicColor: Boolean = true,  // ✅ Material You 지원
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### 동작 방식:
- ✅ **Android 12+**: 시스템 테마 색상 사용 (Material You)
- ✅ **Android 11 이하**: 기본 다크/라이트 색상 스킴 사용
- ✅ 시스템 설정에서 다크 모드를 켜면 앱도 자동으로 다크 모드로 전환

---

## Edge-to-Edge 전체 화면 구현

**User**: edgeToEdge인데 왜 전체 화면이 아니지?

**Assistant**: edgeToEdge가 제대로 작동하도록 수정했습니다.

### 문제 원인:
MainActivity에서 Surface로 감싸져 있어서 전체 화면이 되지 않았습니다.

### 수정 내용:

1. **MainActivity.kt - Surface 제거**:
```kotlin
// 수정 전
setContent {
    AsyncLocationMapTheme {
        Surface(  // ❌ Surface가 배경색과 패딩 추가
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MapScreen()
        }
    }
}

// 수정 후
setContent {
    AsyncLocationMapTheme {
        MapScreen()  // ✅ Surface 제거
    }
}
```

2. **MapScreen.kt - Scaffold 설정 변경**:
```kotlin
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = androidx.compose.ui.graphics.Color.Transparent  // ✅ 투명 배경
) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize()) {  // ✅ padding 제거
        // 지도가 전체 화면 사용
    }
}
```

### 결과:
- ✅ 지도가 상태바와 네비게이션 바 뒤까지 확장
- ✅ 시스템 바가 투명하게 오버레이
- ✅ 콘텐츠가 전체 화면 사용
- ✅ FAB 버튼은 여전히 적절한 위치에 표시

---

## 지도 다크 모드 스타일 적용

**User**: 지도 테마도 색이 좀 변하면 좋겠어

**Assistant**: Google Maps에 다크 모드 스타일을 적용했습니다.

### 구현 내용:

1. **다크 모드 지도 스타일 JSON 생성**:
```
/presentation/src/main/res/raw/map_style_dark.json
```
- 도로, 물, 공원, 건물 등을 어두운 색상으로 변경
- Google Maps Style Wizard 기반 커스텀 스타일

2. **MapScreen.kt에 다크 테마 감지 추가**:
```kotlin
// 다크 모드 감지
val isDarkTheme = isSystemInDarkTheme()
val context = LocalContext.current

// 다크 테마일 때 맵 스타일 적용
val mapProperties = remember(isDarkTheme) {
    if (isDarkTheme) {
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                context,
                R.raw.map_style_dark
            )
        )
    } else {
        MapProperties()
    }
}

// Google Map에 적용
GoogleMap(
    modifier = Modifier.fillMaxSize(),
    cameraPositionState = cameraPositionState,
    properties = mapProperties,  // ✅ 다크 모드 스타일 적용
    uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true
        )
    }
)
```

3. **필요한 import 추가**:
```kotlin
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.MapProperties
```

### 동작 방식:
- ✅ **라이트 모드**: 기본 Google Maps 스타일
- ✅ **다크 모드**: 어두운 색상의 커스텀 스타일
- ✅ 시스템 테마 변경 시 지도도 자동으로 색상 변경
- ✅ 도로, 물, 건물, 레이블 등 모든 요소가 다크 모드에 최적화

---

## Scaffold 제거하고 Box로 변경

**User**: Fab과 BottomSheet이 모두 불투명하게 나오면 좋겠어. Scaffold를 안쓰고 하면 되지 않아?

**Assistant**: 네, 맞습니다! Scaffold를 제거하고 Box만 사용하여 더 간단하게 만들었습니다.

### 문제 원인:
Scaffold의 containerColor를 투명하게 만들면서 복잡해졌고, FAB과 BottomSheet이 투명하게 렌더링되는 문제가 발생했습니다.

### 수정 내용:

**Scaffold 제거 전**:
```kotlin
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = androidx.compose.ui.graphics.Color.Transparent,
    content = { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            // GoogleMap
            // FABs
        }
        // BottomSheet
    }
)
```

**Scaffold 제거 후**:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // 다크 모드 감지
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    
    // 지도 스타일
    val mapProperties = remember(isDarkTheme) { ... }
    
    // Google Map
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = remember { ... }
    ) {
        Clustering(items = uiState.locations)
    }
    
    // Snackbar (상단에 표시)
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(16.dp)
    )
    
    // FAB들
    if (!uiState.isBottomSheetVisible) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 위치 목록 FAB
            // 현재 위치 FAB
        }
    }
    
    // Bottom Sheet
    if (uiState.isBottomSheetVisible) {
        LocationHistoryBottomSheet(...)
    }
}
```

### 변경 사항:

1. **Scaffold 완전 제거**
2. **SnackbarHost를 Box 내부로 이동** - `Alignment.TopCenter`로 상단 배치
3. **불필요한 import 제거**:
   - `androidx.compose.material3.Scaffold`
   - `androidx.compose.material3.ExperimentalMaterial3Api`
4. **@OptIn 어노테이션 수정**:
```kotlin
// 수정 전
@OptIn(ExperimentalPermissionsApi::class, MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)

// 수정 후
@OptIn(ExperimentalPermissionsApi::class, MapsComposeExperimentalApi::class)
```

### 개선된 점:
- ✅ **FAB이 불투명하게 표시** - Material3의 기본 스타일 그대로 사용
- ✅ **BottomSheet이 불투명하게 표시** - 배경이 제대로 렌더링
- ✅ **코드가 더 간결함** - Scaffold의 불필요한 복잡성 제거
- ✅ **전체 화면 지도 유지** - edge-to-edge 여전히 작동
- ✅ **Snackbar는 상단에 표시** - 사용자 경험 개선

---

## 최종 요약

### 완성된 UI 개선 사항:

1. **API Key 보안**
   - ✅ BuildConfig 사용
   - ✅ local.properties에서 관리
   - ✅ Git에 포함되지 않음

2. **시스템 테마 지원**
   - ✅ 다크/라이트 모드 자동 전환
   - ✅ Material You (Android 12+) 지원
   - ✅ 커스텀 색상 스킴 정의

3. **전체 화면 (Edge-to-Edge)**
   - ✅ Surface 제거
   - ✅ 시스템 바 뒤까지 콘텐츠 확장
   - ✅ 투명 오버레이

4. **지도 다크 모드**
   - ✅ 커스텀 다크 스타일 JSON
   - ✅ 시스템 테마에 따라 자동 전환
   - ✅ 모든 지도 요소 최적화

5. **레이아웃 개선**
   - ✅ Scaffold 제거
   - ✅ Box 기반 간결한 구조
   - ✅ FAB과 BottomSheet 불투명 렌더링
   - ✅ Snackbar 상단 배치

### 생성/수정된 파일:

1. **presentation/build.gradle.kts** - BuildConfig 설정
2. **MapScreen.kt** - API Key, 다크 모드, Scaffold 제거
3. **Theme.kt** - 시스템 테마 지원
4. **MainActivity.kt** - Surface 제거
5. **map_style_dark.json** - 다크 모드 지도 스타일

모든 UI 개선이 성공적으로 완료되었습니다! 🎉

### 최종 사용자 경험:

- 📱 시스템 설정에 따라 자동으로 라이트/다크 모드 전환
- 🗺️ 지도도 다크 모드 지원으로 눈이 편함
- 🔒 API 키가 안전하게 숨겨짐
- 📐 전체 화면으로 몰입감 있는 지도 경험
- 🎨 FAB과 BottomSheet이 Material3 디자인 가이드에 맞게 표시

