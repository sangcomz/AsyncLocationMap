package io.github.sangcomz.asynclocationmap.presentation.map.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.sangcomz.asynclocationmap.presentation.map.LocationUiModel
import io.github.sangcomz.asynclocationmap.presentation.utils.StreetViewUrlBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Location Card Component
 *
 * Street View 이미지와 위치 정보를 표시하는 카드입니다.
 *
 * @param location 표시할 위치 정보
 * @param apiKey Google Maps API Key
 * @param onClick 카드 클릭 시 호출되는 콜백
 * @param modifier Modifier
 */
@Composable
fun LocationCard(
    location: LocationUiModel,
    apiKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Street View Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        StreetViewUrlBuilder.buildUrl(
                            latitude = location.latLng.latitude,
                            longitude = location.latLng.longitude,
                            apiKey = apiKey,
                            width = 160,
                            height = 80
                        )
                    )
                    .crossfade(true)
                    .build(),
                contentDescription = "Street View for location ${location.id}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f), // 정사각형 (200 x 100)
                contentScale = ContentScale.Crop
            )

            // Location Info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 위경도
                Text(
                    text = "${String.format(Locale.KOREA, "%.5f", location.latLng.latitude)}\n${String.format(Locale.US, "%.5f", location.latLng.longitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
