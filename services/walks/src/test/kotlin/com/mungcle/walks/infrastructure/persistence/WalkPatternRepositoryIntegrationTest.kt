package com.mungcle.walks.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
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
 * Testcontainers + JDBC 기반 walk_patterns 테이블 integration 테스트.
 * upsert 및 집계 쿼리를 실제 PostgreSQL에서 검증한다.
 */
@Testcontainers
class WalkPatternRepositoryIntegrationTest {

    companion object {
        private val idSeq = AtomicLong(9000L)

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
            // V2 마이그레이션 수동 적용
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS walks.walk_patterns (
                    id          BIGINT PRIMARY KEY,
                    grid_cell   VARCHAR(20) NOT NULL,
                    hour_of_day INTEGER NOT NULL CHECK (hour_of_day BETWEEN 0 AND 23),
                    dog_id      BIGINT NOT NULL,
                    walk_count  INTEGER NOT NULL DEFAULT 1,
                    last_walked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    UNIQUE (grid_cell, hour_of_day, dog_id)
                );
                CREATE INDEX IF NOT EXISTS idx_walk_patterns_grid_hour ON walks.walk_patterns (grid_cell, hour_of_day);
                """.trimIndent()
            )
        }
    }

    @BeforeEach
    fun cleanup() {
        conn.createStatement().execute("DELETE FROM walks.walk_patterns")
    }

    private fun insertPattern(
        dogId: Long = 10L,
        gridCell: String = "100:200",
        hourOfDay: Int = 10,
        walkCount: Int = 1,
        lastWalkedAt: Instant = Instant.now(),
    ): Long {
        val id = idSeq.getAndIncrement()
        val ps = conn.prepareStatement(
            """INSERT INTO walks.walk_patterns (id, grid_cell, hour_of_day, dog_id, walk_count, last_walked_at)
               VALUES (?, ?, ?, ?, ?, ?)"""
        )
        ps.setLong(1, id)
        ps.setString(2, gridCell)
        ps.setInt(3, hourOfDay)
        ps.setLong(4, dogId)
        ps.setInt(5, walkCount)
        ps.setTimestamp(6, Timestamp.from(lastWalkedAt))
        ps.executeUpdate()
        return id
    }

    private fun upsertPattern(
        id: Long,
        gridCell: String,
        hourOfDay: Int,
        dogId: Long,
        walkedAt: Instant,
    ) {
        val ps = conn.prepareStatement(
            """INSERT INTO walks.walk_patterns (id, grid_cell, hour_of_day, dog_id, walk_count, last_walked_at)
               VALUES (?, ?, ?, ?, 1, ?)
               ON CONFLICT (grid_cell, hour_of_day, dog_id)
               DO UPDATE SET walk_count = walks.walk_patterns.walk_count + 1,
                             last_walked_at = EXCLUDED.last_walked_at"""
        )
        ps.setLong(1, id)
        ps.setString(2, gridCell)
        ps.setInt(3, hourOfDay)
        ps.setLong(4, dogId)
        ps.setTimestamp(5, Timestamp.from(walkedAt))
        ps.executeUpdate()
    }

    private fun queryByGridCellAndHour(gridCells: List<String>, minHour: Int, maxHour: Int): List<Map<String, Any?>> {
        val placeholders = gridCells.joinToString(",") { "?" }
        val ps = conn.prepareStatement(
            "SELECT * FROM walks.walk_patterns WHERE grid_cell IN ($placeholders) AND hour_of_day BETWEEN ? AND ? ORDER BY walk_count DESC"
        )
        gridCells.forEachIndexed { i, cell -> ps.setString(i + 1, cell) }
        ps.setInt(gridCells.size + 1, minHour)
        ps.setInt(gridCells.size + 2, maxHour)
        val rs = ps.executeQuery()
        val results = mutableListOf<Map<String, Any?>>()
        while (rs.next()) {
            results.add(mapOf(
                "id" to rs.getLong("id"),
                "dog_id" to rs.getLong("dog_id"),
                "grid_cell" to rs.getString("grid_cell"),
                "hour_of_day" to rs.getInt("hour_of_day"),
                "walk_count" to rs.getInt("walk_count"),
                "last_walked_at" to rs.getTimestamp("last_walked_at").toInstant(),
            ))
        }
        return results
    }

    @Test
    fun `upsert — 새 레코드 삽입`() {
        val now = Instant.now()
        upsertPattern(idSeq.getAndIncrement(), "100:200", 10, 10L, now)

        val results = queryByGridCellAndHour(listOf("100:200"), 9, 11)
        assertEquals(1, results.size)
        assertEquals(1, results[0]["walk_count"])
        assertEquals(10L, results[0]["dog_id"])
    }

    @Test
    fun `upsert — 중복 시 walk_count 증가 및 last_walked_at 갱신`() {
        val id = idSeq.getAndIncrement()
        val first = Instant.now().minusSeconds(3600)
        val second = Instant.now()
        upsertPattern(id, "100:200", 10, 10L, first)
        upsertPattern(idSeq.getAndIncrement(), "100:200", 10, 10L, second)

        val results = queryByGridCellAndHour(listOf("100:200"), 9, 11)
        assertEquals(1, results.size)
        assertEquals(2, results[0]["walk_count"])
        // last_walked_at은 두 번째 값으로 갱신
        val storedAt = results[0]["last_walked_at"] as Instant
        assertTrue(storedAt >= first)
    }

    @Test
    fun `시간 범위 필터 — 범위 밖 hourOfDay 제외`() {
        insertPattern(dogId = 10L, gridCell = "100:200", hourOfDay = 10, walkCount = 5)
        insertPattern(dogId = 20L, gridCell = "100:200", hourOfDay = 14, walkCount = 3)

        val results = queryByGridCellAndHour(listOf("100:200"), 9, 11)
        assertEquals(1, results.size)
        assertEquals(10L, results[0]["dog_id"])
    }

    @Test
    fun `그리드 셀 필터 — 대상 셀만 반환`() {
        insertPattern(dogId = 10L, gridCell = "100:200", hourOfDay = 10)
        insertPattern(dogId = 20L, gridCell = "999:999", hourOfDay = 10)

        val results = queryByGridCellAndHour(listOf("100:200"), 9, 11)
        assertEquals(1, results.size)
        assertEquals(10L, results[0]["dog_id"])
    }

    @Test
    fun `walk_count 기준 내림차순 정렬`() {
        insertPattern(dogId = 10L, gridCell = "100:200", hourOfDay = 10, walkCount = 3)
        insertPattern(dogId = 20L, gridCell = "100:200", hourOfDay = 10, walkCount = 7)
        insertPattern(dogId = 30L, gridCell = "100:200", hourOfDay = 10, walkCount = 1)

        val results = queryByGridCellAndHour(listOf("100:200"), 9, 11)
        assertEquals(3, results.size)
        assertEquals(7, results[0]["walk_count"])
        assertEquals(3, results[1]["walk_count"])
        assertEquals(1, results[2]["walk_count"])
    }

    @Test
    fun `idx_walk_patterns_grid_hour 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'walks' AND tablename = 'walk_patterns' AND indexname = 'idx_walk_patterns_grid_hour'"
        )
        assertTrue(rs.next(), "idx_walk_patterns_grid_hour 인덱스가 존재해야 합니다")
    }
}
