package io.github.sangcomz.asynclocationmap.data.mapper

import io.github.sangcomz.asynclocationmap.data.local.entity.LocationEntity
import io.github.sangcomz.asynclocationmap.domain.model.Location

/**
 * LocationEntity를 Domain Model로 변환합니다.
 *
 * Data Layer의 Entity를 Domain Layer의 Model로 변환하여
 * 레이어 간 의존성을 분리합니다.
 *
 * @return Domain Model인 Location
 */
fun LocationEntity.toDomain(): Location {
    return Location(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}

/**
 * Domain Model을 LocationEntity로 변환합니다.
 *
 * Domain Layer의 Model을 Data Layer의 Entity로 변환하여
 * Room Database에 저장할 수 있도록 합니다.
 *
 * @return Room Entity인 LocationEntity
 */
fun Location.toEntity(): LocationEntity {
    return LocationEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}
