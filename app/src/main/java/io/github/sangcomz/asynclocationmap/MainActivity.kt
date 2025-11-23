package io.github.sangcomz.asynclocationmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.sangcomz.asynclocationmap.presentation.map.MapScreen
import io.github.sangcomz.asynclocationmap.ui.theme.AsyncLocationMapTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity
 *
 * 앱의 메인 액티비티입니다.
 * @AndroidEntryPoint 어노테이션을 사용하여 Hilt DI를 활성화합니다.
 *
 * MapScreen을 표시하고, 위치 권한 요청 및 지도 표시를 처리합니다.
 * Compose를 사용하여 UI를 구성합니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsyncLocationMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen()
                }
            }
        }
    }
}
