package com.mungcle.walks.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Timestamp
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/**
 * Testcontainers + JDBC 기반 walks 서비스 integration 테스트.
 * nearby 쿼리, 상태 필터, 인덱스를 실제 PostgreSQL에서 검증한다.
 */
@Testcontainers
class WalkRepositoryIntegrationTest {

    companion object {
        private val idSeq = AtomicLong(1000L)

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("mungcle_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__init_walks_schema.sql")

        private lateinit var conn: Connection

        @BeforeAll
        @JvmStatic
        fun setup() {
            conn = DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
            conn.autoCommit = true
        }
    }

    @BeforeEach
    fun cleanup() {
        conn.createStatement().execute("DELETE FROM walks.walks")
    }

    private fun insertWalk(
        dogId: Long = 10L,
        userId: Long = 1L,
        type: String = "OPEN",
        gridCell: String = "100:200",
        status: String = "ACTIVE",
        endsAt: Instant = Instant.now().plusSeconds(3600),
        endedAt: Instant? = null,
    ): Long {
        val id = idSeq.getAndIncrement()
        val ps = conn.prepareStatement(
            """INSERT INTO walks.walks (id, dog_id, user_id, type, grid_cell, status, started_at, ends_at, ended_at, created_at)
               VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, NOW())"""
        )
        ps.setLong(1, id)
        ps.setLong(2, dogId)
        ps.setLong(3, userId)
        ps.setString(4, type)
        ps.setString(5, gridCell)
        ps.setString(6, status)
        ps.setTimestamp(7, Timestamp.from(endsAt))
        ps.setTimestamp(8, endedAt?.let { Timestamp.from(it) })
        ps.executeUpdate()
        return id
    }

    private fun queryActiveOpenByGridCells(gridCells: List<String>): List<Map<String, Any?>> {
        val placeholders = gridCells.joinToString(",") { "?" }
        val ps = conn.prepareStatement(
            "SELECT * FROM walks.walks WHERE grid_cell IN ($placeholders) AND status = 'ACTIVE' AND type = 'OPEN'"
        )
        gridCells.forEachIndexed { i, cell -> ps.setString(i + 1, cell) }
        val rs = ps.executeQuery()
        val results = mutableListOf<Map<String, Any?>>()
        while (rs.next()) {
            results.add(mapOf(
                "id" to rs.getLong("id"),
                "dog_id" to rs.getLong("dog_id"),
                "user_id" to rs.getLong("user_id"),
                "grid_cell" to rs.getString("grid_cell"),
                "status" to rs.getString("status"),
                "type" to rs.getString("type"),
            ))
        }
        return results
    }

    private fun queryActiveByDogId(dogId: Long): Map<String, Any?>? {
        val ps = conn.prepareStatement(
            "SELECT * FROM walks.walks WHERE dog_id = ? AND status = 'ACTIVE'"
        )
        ps.setLong(1, dogId)
        val rs = ps.executeQuery()
        return if (rs.next()) mapOf(
            "id" to rs.getLong("id"),
            "status" to rs.getString("status"),
        ) else null
    }

    private fun queryActiveByUserId(userId: Long): List<Map<String, Any?>> {
        val ps = conn.prepareStatement(
            "SELECT * FROM walks.walks WHERE user_id = ? AND status = 'ACTIVE'"
        )
        ps.setLong(1, userId)
        val rs = ps.executeQuery()
        val results = mutableListOf<Map<String, Any?>>()
        while (rs.next()) {
            results.add(mapOf("id" to rs.getLong("id"), "dog_id" to rs.getLong("dog_id")))
        }
        return results
    }

    @Test
    fun `save and read — 기본 필드 검증`() {
        val id = insertWalk(dogId = 10L, userId = 1L, gridCell = "100:200", type = "OPEN")

        val ps = conn.prepareStatement("SELECT * FROM walks.walks WHERE id = ?")
        ps.setLong(1, id)
        val rs = ps.executeQuery()
        assertTrue(rs.next())
        assertEquals(10L, rs.getLong("dog_id"))
        assertEquals(1L, rs.getLong("user_id"))
        assertEquals("100:200", rs.getString("grid_cell"))
        assertEquals("OPEN", rs.getString("type"))
        assertEquals("ACTIVE", rs.getString("status"))
    }

    @Test
    fun `findActiveOpenByGridCells — 인접 셀의 ACTIVE OPEN 산책만 반환`() {
        insertWalk(dogId = 10L, gridCell = "100:200", type = "OPEN", status = "ACTIVE")
        insertWalk(dogId = 11L, gridCell = "100:201", type = "OPEN", status = "ACTIVE")
        insertWalk(dogId = 12L, gridCell = "100:202", type = "OPEN", status = "ENDED")
        insertWalk(dogId = 13L, gridCell = "100:200", type = "SOLO", status = "ACTIVE")
        insertWalk(dogId = 14L, gridCell = "999:999", type = "OPEN", status = "ACTIVE")

        val result = queryActiveOpenByGridCells(listOf("100:200", "100:201", "100:202"))
        assertEquals(2, result.size)
        assertTrue(result.all { it["status"] == "ACTIVE" })
        assertTrue(result.all { it["type"] == "OPEN" })
    }

    @Test
    fun `findActiveByDogId — 활성 산책이 있으면 반환`() {
        insertWalk(dogId = 10L, status = "ACTIVE")
        insertWalk(dogId = 10L, status = "ENDED")

        val active = queryActiveByDogId(10L)
        assertNotNull(active)
        assertEquals("ACTIVE", active!!["status"])
    }

    @Test
    fun `findActiveByDogId — 활성 산책이 없으면 null`() {
        insertWalk(dogId = 10L, status = "ENDED")

        val result = queryActiveByDogId(10L)
        assertEquals(null, result)
    }

    @Test
    fun `findActiveByUserId — 사용자의 활성 산책 목록`() {
        insertWalk(dogId = 10L, userId = 1L, status = "ACTIVE")
        insertWalk(dogId = 11L, userId = 1L, status = "ACTIVE")
        insertWalk(dogId = 12L, userId = 1L, status = "ENDED")
        insertWalk(dogId = 20L, userId = 2L, status = "ACTIVE")

        val result = queryActiveByUserId(1L)
        assertEquals(2, result.size)
    }

    @Test
    fun `종료된 산책의 ended_at 저장 검증`() {
        val endedAt = Instant.now()
        val id = insertWalk(status = "ENDED", endedAt = endedAt)

        val ps = conn.prepareStatement("SELECT ended_at FROM walks.walks WHERE id = ?")
        ps.setLong(1, id)
        val rs = ps.executeQuery()
        assertTrue(rs.next())
        assertNotNull(rs.getTimestamp("ended_at"))
    }

    @Test
    fun `idx_walks_nearby 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'walks' AND tablename = 'walks' AND indexname = 'idx_walks_nearby'"
        )
        assertTrue(rs.next(), "idx_walks_nearby 인덱스가 존재해야 합니다")
    }

    @Test
    fun `idx_walks_dog_status 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'walks' AND tablename = 'walks' AND indexname = 'idx_walks_dog_status'"
        )
        assertTrue(rs.next(), "idx_walks_dog_status 인덱스가 존재해야 합니다")
    }
}
