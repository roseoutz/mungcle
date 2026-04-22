package com.mungcle.gateway.domain

import com.mungcle.common.domain.GridCell
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GridCellTest {

    @Test
    fun `일반 좌표 변환`() {
        val cell = GridCell.fromCoordinates(37.5005, 127.0005)
        assertEquals("18750:63500", cell.value)
    }

    @Test
    fun `적도 좌표 — 위도 0, 경도 0`() {
        val cell = GridCell.fromCoordinates(0.0, 0.0)
        assertEquals("0:0", cell.value)
    }

    @Test
    fun `음수 위도 — 남반구`() {
        val cell = GridCell.fromCoordinates(-33.8688, 151.2093)
        assertEquals("-16935:75604", cell.value)
    }

    @Test
    fun `음수 경도 — 서반구`() {
        val cell = GridCell.fromCoordinates(40.7128, -74.0060)
        assertEquals("20356:-37003", cell.value)
    }

    @Test
    fun `경계값 최대 위도`() {
        val cell = GridCell.fromCoordinates(90.0, 180.0)
        assertEquals("45000:90000", cell.value)
    }

    @Test
    fun `경계값 최소 위도`() {
        val cell = GridCell.fromCoordinates(-90.0, -180.0)
        assertEquals("-45000:-90000", cell.value)
    }

    @Test
    fun `동일 그리드 셀 내 두 좌표는 같은 셀을 반환`() {
        val cell1 = GridCell.fromCoordinates(37.5000, 127.0000)
        val cell2 = GridCell.fromCoordinates(37.5001, 127.0001)
        assertEquals(cell1, cell2)
    }

    @Test
    fun `인접 셀 목록 — 9개 반환`() {
        val cell = GridCell.fromCoordinates(37.5, 127.0)
        val adjacent = GridCell.adjacentCells(cell)
        assertEquals(9, adjacent.size)
    }
}
