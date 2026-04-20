package com.mungcle.walks.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class GridCellTest {

    @Test
    fun `적도 (0, 0) 좌표를 그리드 셀로 변환`() {
        val cell = GridCell.fromCoordinates(0.0, 0.0)
        assertEquals("0:0", cell.value)
    }

    @Test
    fun `양수 좌표를 그리드 셀로 변환`() {
        val cell = GridCell.fromCoordinates(37.5665, 126.978)
        val latBucket = kotlin.math.floor(37.5665 / 0.002).toInt()
        val lngBucket = kotlin.math.floor(126.978 / 0.002).toInt()
        assertEquals("$latBucket:$lngBucket", cell.value)
    }

    @Test
    fun `음수 좌표를 그리드 셀로 변환`() {
        val cell = GridCell.fromCoordinates(-33.8688, 151.2093)
        val latBucket = kotlin.math.floor(-33.8688 / 0.002).toInt()
        val lngBucket = kotlin.math.floor(151.2093 / 0.002).toInt()
        assertEquals("$latBucket:$lngBucket", cell.value)
    }

    @Test
    fun `음수 경도 좌표를 그리드 셀로 변환`() {
        val cell = GridCell.fromCoordinates(40.7128, -74.006)
        val latBucket = kotlin.math.floor(40.7128 / 0.002).toInt()
        val lngBucket = kotlin.math.floor(-74.006 / 0.002).toInt()
        assertEquals("$latBucket:$lngBucket", cell.value)
    }

    @Test
    fun `0점002도 차이는 같은 셀에 속함`() {
        val cell1 = GridCell.fromCoordinates(37.566, 126.978)
        val cell2 = GridCell.fromCoordinates(37.567, 126.978)
        assertEquals(cell1, cell2)
    }

    @Test
    fun `0점002도 이상 차이는 다른 셀에 속함`() {
        val cell1 = GridCell.fromCoordinates(37.566, 126.978)
        val cell2 = GridCell.fromCoordinates(37.570, 126.978)
        assertNotEquals(cell1, cell2)
    }

    @Test
    fun `adjacentCells는 9개 셀을 반환`() {
        val center = GridCell("100:200")
        val adjacent = GridCell.adjacentCells(center)
        assertEquals(9, adjacent.size)
    }

    @Test
    fun `adjacentCells에 자기 자신 포함`() {
        val center = GridCell("100:200")
        val adjacent = GridCell.adjacentCells(center)
        assert(center in adjacent)
    }

    @Test
    fun `adjacentCells 음수 버킷도 정상 처리`() {
        val center = GridCell("0:0")
        val adjacent = GridCell.adjacentCells(center)
        assertEquals(9, adjacent.size)
        assert(GridCell("-1:-1") in adjacent)
        assert(GridCell("-1:0") in adjacent)
        assert(GridCell("0:-1") in adjacent)
    }

    @Test
    fun `gridDistance — 같은 셀은 0`() {
        val a = GridCell("100:200")
        assertEquals(0, GridCell.gridDistance(a, a))
    }

    @Test
    fun `gridDistance — 인접 셀은 1`() {
        val a = GridCell("100:200")
        val b = GridCell("101:201")
        assertEquals(1, GridCell.gridDistance(a, b))
    }

    @Test
    fun `gridDistance — 대각 2칸은 2`() {
        val a = GridCell("100:200")
        val b = GridCell("102:202")
        assertEquals(2, GridCell.gridDistance(a, b))
    }

    @Test
    fun `gridDistance — 3칸 이상은 3 이상`() {
        val a = GridCell("100:200")
        val b = GridCell("103:200")
        assertEquals(3, GridCell.gridDistance(a, b))
    }
}
