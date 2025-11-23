package io.github.sangcomz.asynclocationmap.presentation.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.sangcomz.asynclocationmap.presentation.map.LocationUiModel

/**
 * Location History Bottom Sheet
 *
 * 최근 위치 기록을 Horizontal Scroll로 표시하는 BottomSheet입니다.
 *
 * @param locations 표시할 위치 리스트
 * @param apiKey Google Maps API Key
 * @param onLocationClick 위치 카드 클릭 시 호출되는 콜백
 * @param onDismiss BottomSheet를 닫을 때 호출되는 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationHistoryBottomSheet(
    locations: List<LocationUiModel>,
    apiKey: String,
    onLocationClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최근 위치 기록",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기"
                    )
                }
            }

            // Horizontal Scrollable Cards
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(locations, key = { it.id }) { location ->
                    LocationCard(
                        location = location,
                        apiKey = apiKey,
                        onClick = { onLocationClick(location.id) }
                    )
                }
            }
        }
    }
}
