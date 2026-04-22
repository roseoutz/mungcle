package com.mungcle.social.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
 * Testcontainers + JDBC 기반 social 서비스 integration 테스트.
 * save, findById, findBySenderAndWalk unique 제약, findByUserId 필터를 실제 PostgreSQL에서 검증한다.
 */
@Testcontainers
class GreetingRepositoryIntegrationTest {

    companion object {
        private val idSeq = AtomicLong(1000L)

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("mungcle_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__init_social_schema.sql")

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
        conn.createStatement().execute("DELETE FROM social.greetings")
    }

    private fun insertGreeting(
        id: Long = idSeq.getAndIncrement(),
        senderUserId: Long = 10L,
        senderDogId: Long = 100L,
        receiverUserId: Long = 20L,
        receiverDogId: Long = 200L,
        receiverWalkId: Long = 300L,
        status: String = "PENDING",
        expiresAt: Instant = Instant.now().plusSeconds(300),
        respondedAt: Instant? = null,
    ): Long {
        val ps = conn.prepareStatement(
            """INSERT INTO social.greetings
               (id, sender_user_id, sender_dog_id, receiver_user_id, receiver_dog_id, receiver_walk_id,
                status, created_at, responded_at, expires_at)
               VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)"""
        )
        ps.setLong(1, id)
        ps.setLong(2, senderUserId)
        ps.setLong(3, senderDogId)
        ps.setLong(4, receiverUserId)
        ps.setLong(5, receiverDogId)
        ps.setLong(6, receiverWalkId)
        ps.setString(7, status)
        ps.setTimestamp(8, respondedAt?.let { Timestamp.from(it) })
        ps.setTimestamp(9, Timestamp.from(expiresAt))
        ps.executeUpdate()
        return id
    }

    @Test
    fun `save and findById — 기본 필드 검증`() {
        val id = insertGreeting(senderUserId = 10L, receiverUserId = 20L, status = "PENDING")

        val ps = conn.prepareStatement("SELECT * FROM social.greetings WHERE id = ?")
        ps.setLong(1, id)
        val rs = ps.executeQuery()
        assertTrue(rs.next())
        assertEquals(10L, rs.getLong("sender_user_id"))
        assertEquals(20L, rs.getLong("receiver_user_id"))
        assertEquals("PENDING", rs.getString("status"))
    }

    @Test
    fun `findBySenderAndWalk — 동일 조합 존재하면 반환`() {
        insertGreeting(senderUserId = 10L, receiverWalkId = 300L)

        val ps = conn.prepareStatement(
            "SELECT * FROM social.greetings WHERE sender_user_id = ? AND receiver_walk_id = ?"
        )
        ps.setLong(1, 10L)
        ps.setLong(2, 300L)
        val rs = ps.executeQuery()
        assertTrue(rs.next())
    }

    @Test
    fun `unique 제약 — 동일 senderUserId + receiverWalkId 중복 삽입 실패`() {
        insertGreeting(id = idSeq.getAndIncrement(), senderUserId = 10L, receiverWalkId = 300L)

        var thrown = false
        try {
            insertGreeting(id = idSeq.getAndIncrement(), senderUserId = 10L, receiverWalkId = 300L)
        } catch (e: Exception) {
            thrown = true
        }
        assertTrue(thrown, "unique 제약 위반이 발생해야 합니다")
    }

    @Test
    fun `findByUserId — 발신자 필터`() {
        insertGreeting(senderUserId = 10L, receiverUserId = 20L, status = "PENDING")
        insertGreeting(senderUserId = 10L, receiverUserId = 21L, status = "ACCEPTED", receiverWalkId = 301L)
        insertGreeting(senderUserId = 30L, receiverUserId = 10L, status = "PENDING", receiverWalkId = 302L)

        val ps = conn.prepareStatement(
            "SELECT * FROM social.greetings WHERE sender_user_id = ?"
        )
        ps.setLong(1, 10L)
        val rs = ps.executeQuery()
        var count = 0
        while (rs.next()) count++
        assertEquals(2, count)
    }

    @Test
    fun `findByUserId — 수신자 필터`() {
        insertGreeting(senderUserId = 10L, receiverUserId = 20L)
        insertGreeting(senderUserId = 11L, receiverUserId = 20L, receiverWalkId = 301L)
        insertGreeting(senderUserId = 20L, receiverUserId = 30L, receiverWalkId = 302L)

        val ps = conn.prepareStatement(
            "SELECT * FROM social.greetings WHERE receiver_user_id = ?"
        )
        ps.setLong(1, 20L)
        val rs = ps.executeQuery()
        var count = 0
        while (rs.next()) count++
        assertEquals(2, count)
    }

    @Test
    fun `idx_greetings_receiver 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'social' AND tablename = 'greetings' AND indexname = 'idx_greetings_receiver'"
        )
        assertTrue(rs.next(), "idx_greetings_receiver 인덱스가 존재해야 합니다")
    }

    @Test
    fun `idx_greetings_sender 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'social' AND tablename = 'greetings' AND indexname = 'idx_greetings_sender'"
        )
        assertTrue(rs.next(), "idx_greetings_sender 인덱스가 존재해야 합니다")
    }
}
