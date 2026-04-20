package com.mungcle.walks.domain.model

import kotlin.math.abs
import kotlin.math.floor

/**
 * 200m 그리드 셀 값 객체.
 * GPS 좌표를 직접 저장하지 않고, 그리드 버킷 ID만 보유.
 */
data class GridCell(val value: String) {

    companion object {
        /**
         * GPS 좌표를 200m 그리드 셀로 스냅.
         * 약 0.002도 ≈ 200m (적도 기준).
         */
        fun fromCoordinates(lat: Double, lng: Double): GridCell {
            val latBucket = floor(lat / 0.002).toInt()
            val lngBucket = floor(lng / 0.002).toInt()
            return GridCell("$latBucket:$lngBucket")
        }

        /** 3x3 인접 셀 목록 (자기 자신 포함) */
        fun adjacentCells(cell: GridCell): List<GridCell> {
            val (latBucket, lngBucket) = cell.value.split(":").map { it.toInt() }
            return (-1..1).flatMap { dLat ->
                (-1..1).map { dLng ->
                    GridCell("${latBucket + dLat}:${lngBucket + dLng}")
                }
            }
        }

        /** 두 셀 사이의 체비셰프 거리 (0, 1, 2 …) */
        fun gridDistance(a: GridCell, b: GridCell): Int {
            val (aLat, aLng) = a.value.split(":").map { it.toInt() }
            val (bLat, bLng) = b.value.split(":").map { it.toInt() }
            return maxOf(abs(aLat - bLat), abs(aLng - bLng))
        }
    }
}
