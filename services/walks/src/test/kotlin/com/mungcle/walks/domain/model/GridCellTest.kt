package com.mungcle.walks.domain.model

import com.mungcle.common.domain.GridCell
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GridCellTest {

    @Test
    fun `적도 위 좌표 (lat=0, lng=0) 그리드 스냅`() {
        val cell = GridCell.fromCoordinates(0.0, 0.0)
        assertEquals("0:0", cell.value)
    }

    @Test
    fun `양수 좌표 그리드 스냅`() {
        val cell = GridCell.fromCoordinates(37.5665, 126.978)
        val (latBucket, lngBucket) = cell.value.split(":").map { it.toInt() }
        // floor(37.5665 / 0.002) = 18783, floor(126.978 / 0.002) = 63489 or 63488 (부동소수점)
        assertEquals(18783, latBucket)
        assertEquals(kotlin.math.floor(126.978 / 0.002).toInt(), lngBucket)
    }

    @Test
    fun `음수 좌표 그리드 스냅`() {
        val cell = GridCell.fromCoordinates(-33.8688, 151.2093)
        val (latBucket, _) = cell.value.split(":").map { it.toInt() }
        // 음수 위도는 음수 버킷
        assert(latBucket < 0) { "음수 위도는 음수 버킷이어야 함: $latBucket" }
    }

    @Test
    fun `아주 작은 양수 좌표는 0 버킷`() {
        val cell = GridCell.fromCoordinates(0.001, 0.001)
        assertEquals("0:0", cell.value)
    }

    @Test
    fun `아주 작은 음수 좌표는 -1 버킷`() {
        val cell = GridCell.fromCoordinates(-0.001, -0.001)
        assertEquals("-1:-1", cell.value)
    }

    @Test
    fun `adjacentCells는 9개 셀 반환`() {
        val center = GridCell("10:20")
        val adjacent = GridCell.adjacentCells(center)
        assertEquals(9, adjacent.size)
    }

    @Test
    fun `adjacentCells는 자기 자신 포함`() {
        val center = GridCell("10:20")
        val adjacent = GridCell.adjacentCells(center)
        assert(center in adjacent) { "인접 셀 목록에 자기 자신이 포함되어야 함" }
    }

    @Test
    fun `adjacentCells 경계값 확인`() {
        val center = GridCell("0:0")
        val adjacent = GridCell.adjacentCells(center)
        assert(GridCell("-1:-1") in adjacent)
        assert(GridCell("-1:0") in adjacent)
        assert(GridCell("-1:1") in adjacent)
        assert(GridCell("0:-1") in adjacent)
        assert(GridCell("0:0") in adjacent)
        assert(GridCell("0:1") in adjacent)
        assert(GridCell("1:-1") in adjacent)
        assert(GridCell("1:0") in adjacent)
        assert(GridCell("1:1") in adjacent)
    }

    @Test
    fun `gridDistance 같은 셀은 0`() {
        val cell = GridCell("10:20")
        assertEquals(0, GridCell.gridDistance(cell, cell))
    }

    @Test
    fun `gridDistance 인접 셀은 1`() {
        val a = GridCell("10:20")
        val b = GridCell("11:21")
        assertEquals(1, GridCell.gridDistance(a, b))
    }

    @Test
    fun `gridDistance 대각선 1칸은 1 (체비셰프)`() {
        val a = GridCell("10:20")
        val b = GridCell("11:21")
        assertEquals(1, GridCell.gridDistance(a, b))
    }

    @Test
    fun `gridDistance 2칸 떨어진 셀`() {
        val a = GridCell("10:20")
        val b = GridCell("12:20")
        assertEquals(2, GridCell.gridDistance(a, b))
    }

    @Test
    fun `gridDistance 음수 좌표 간 거리`() {
        val a = GridCell("-5:-10")
        val b = GridCell("-3:-8")
        assertEquals(2, GridCell.gridDistance(a, b))
    }
}
