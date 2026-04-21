package com.mungcle.common.domain

import kotlin.math.floor

/**
 * 200m 그리드 셀 Value Object.
 * GPS 좌표를 200m 격자 중심점으로 스냅한다.
 * 정확한 GPS 좌표는 절대 저장/전송하지 않는다 (프라이버시 불변 규칙).
 */
data class GridCell(val value: String) {

    companion object {
        private const val GRID_SIZE = 0.002 // 약 200m

        /**
         * GPS 좌표를 GridCell로 변환.
         * 경계값(적도, 음수, 0/0) 테스트 필수.
         */
        fun fromCoordinates(lat: Double, lng: Double): GridCell {
            val latBucket = floor(lat / GRID_SIZE).toInt()
            val lngBucket = floor(lng / GRID_SIZE).toInt()
            return GridCell("$latBucket:$lngBucket")
        }

        /**
         * 3x3 인접 셀 목록 (자신 포함 9개).
         * nearby 쿼리에 사용. 약 600m 반경 근사.
         */
        fun adjacentCells(cell: GridCell): List<GridCell> {
            val (latBucket, lngBucket) = cell.value.split(":").map { it.toInt() }
            return (-1..1).flatMap { dLat ->
                (-1..1).map { dLng ->
                    GridCell("${latBucket + dLat}:${lngBucket + dLng}")
                }
            }
        }

        /**
         * 두 셀 간 그리드 거리 (0=같은 셀, 1=인접, 2=대각선).
         * 정확한 거리(m)는 절대 노출하지 않는다.
         */
        fun gridDistance(a: GridCell, b: GridCell): Int {
            val (aLat, aLng) = a.value.split(":").map { it.toInt() }
            val (bLat, bLng) = b.value.split(":").map { it.toInt() }
            return maxOf(
                kotlin.math.abs(aLat - bLat),
                kotlin.math.abs(aLng - bLng)
            )
        }
    }
}
