package com.example.asynclocationmap.domain.model

/**
 * Domain Model for Location
 *
 * 순수한 도메인 모델로, Android나 외부 라이브러리에 의존하지 않습니다.
 *
 * @property id 위치 정보의 고유 ID (Room에서 자동 생성)
 * @property latitude 위도
 * @property longitude 경도
 * @property timestamp 위치 정보가 기록된 시간 (milliseconds)
 */
data class Location(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
