package com.mungcle.social.infrastructure.persistence

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
 * Testcontainers + JDBC 기반 Message 테이블 integration 테스트.
 * V1 + V2 마이그레이션을 적용하여 save, findByGreetingId를 검증한다.
 */
@Testcontainers
class MessageRepositoryIntegrationTest {

    companion object {
        private val idSeq = AtomicLong(2000L)

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
            // V2 마이그레이션 수동 적용
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS social.messages (
                    id              BIGINT PRIMARY KEY,
                    greeting_id     BIGINT NOT NULL REFERENCES social.greetings(id),
                    sender_user_id  BIGINT NOT NULL,
                    body            VARCHAR(140) NOT NULL,
                    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
                );
                CREATE INDEX IF NOT EXISTS idx_messages_greeting ON social.messages (greeting_id, created_at);
                """.trimIndent()
            )
        }
    }

    @BeforeEach
    fun cleanup() {
        conn.createStatement().execute("DELETE FROM social.messages")
        conn.createStatement().execute("DELETE FROM social.greetings")
    }

    private fun insertGreeting(
        id: Long = idSeq.getAndIncrement(),
        senderUserId: Long = 10L,
        receiverUserId: Long = 20L,
        receiverWalkId: Long = 300L,
    ): Long {
        val ps = conn.prepareStatement(
            """INSERT INTO social.greetings
               (id, sender_user_id, sender_dog_id, receiver_user_id, receiver_dog_id, receiver_walk_id,
                status, created_at, expires_at)
               VALUES (?, ?, ?, ?, ?, ?, 'ACCEPTED', NOW(), NOW() + INTERVAL '30 minutes')"""
        )
        ps.setLong(1, id)
        ps.setLong(2, senderUserId)
        ps.setLong(3, 100L)
        ps.setLong(4, receiverUserId)
        ps.setLong(5, 200L)
        ps.setLong(6, receiverWalkId)
        ps.executeUpdate()
        return id
    }

    private fun insertMessage(
        id: Long = idSeq.getAndIncrement(),
        greetingId: Long,
        senderUserId: Long = 10L,
        body: String = "안녕하세요",
        createdAt: Instant = Instant.now(),
    ): Long {
        val ps = conn.prepareStatement(
            "INSERT INTO social.messages (id, greeting_id, sender_user_id, body, created_at) VALUES (?, ?, ?, ?, ?)"
        )
        ps.setLong(1, id)
        ps.setLong(2, greetingId)
        ps.setLong(3, senderUserId)
        ps.setString(4, body)
        ps.setTimestamp(5, Timestamp.from(createdAt))
        ps.executeUpdate()
        return id
    }

    @Test
    fun `save — 메시지 저장 후 조회 가능`() {
        val greetingId = insertGreeting()
        val messageId = insertMessage(greetingId = greetingId, body = "반갑습니다")

        val ps = conn.prepareStatement("SELECT * FROM social.messages WHERE id = ?")
        ps.setLong(1, messageId)
        val rs = ps.executeQuery()
        assertTrue(rs.next())
        assertEquals(greetingId, rs.getLong("greeting_id"))
        assertEquals("반갑습니다", rs.getString("body"))
        assertEquals(10L, rs.getLong("sender_user_id"))
    }

    @Test
    fun `findByGreetingId — 동일 greeting의 메시지 목록 반환`() {
        val greetingId = insertGreeting()
        insertMessage(greetingId = greetingId, body = "첫 번째 메시지")
        insertMessage(greetingId = greetingId, body = "두 번째 메시지")
        insertMessage(greetingId = insertGreeting(receiverWalkId = 301L), body = "다른 greeting 메시지")

        val ps = conn.prepareStatement(
            "SELECT * FROM social.messages WHERE greeting_id = ? ORDER BY created_at"
        )
        ps.setLong(1, greetingId)
        val rs = ps.executeQuery()
        var count = 0
        while (rs.next()) count++
        assertEquals(2, count)
    }

    @Test
    fun `idx_messages_greeting 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'social' AND tablename = 'messages' AND indexname = 'idx_messages_greeting'"
        )
        assertTrue(rs.next(), "idx_messages_greeting 인덱스가 존재해야 합니다")
    }
}
