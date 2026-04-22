package com.mungcle.notification.infrastructure.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
 * Testcontainers + JDBC 기반 notification 서비스 integration 테스트.
 * save, findByUserId, markRead를 실제 PostgreSQL에서 검증한다.
 */
@Testcontainers
class NotificationRepositoryIntegrationTest {

    companion object {
        private val idSeq = AtomicLong(1000L)

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("mungcle_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__init_notification_schema.sql")

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
        conn.createStatement().execute("DELETE FROM notification.notifications")
    }

    private fun insertNotification(
        userId: Long = 1L,
        type: String = "GREETING_RECEIVED",
        payloadJson: String = "{}",
        read: Boolean = false,
    ): Long {
        val id = idSeq.getAndIncrement()
        val ps = conn.prepareStatement(
            """INSERT INTO notification.notifications (id, user_id, type, payload_json, read, created_at)
               VALUES (?, ?, ?, ?, ?, NOW())"""
        )
        ps.setLong(1, id)
        ps.setLong(2, userId)
        ps.setString(3, type)
        ps.setString(4, payloadJson)
        ps.setBoolean(5, read)
        ps.executeUpdate()
        return id
    }

    @Test
    fun `save — 기본 필드 검증`() {
        val id = insertNotification(userId = 1L, type = "GREETING_RECEIVED", payloadJson = """{"greetingId":42}""")

        val ps = conn.prepareStatement("SELECT * FROM notification.notifications WHERE id = ?")
        ps.setLong(1, id)
        val rs = ps.executeQuery()
        assertTrue(rs.next())
        assertEquals(1L, rs.getLong("user_id"))
        assertEquals("GREETING_RECEIVED", rs.getString("type"))
        assertEquals("""{"greetingId":42}""", rs.getString("payload_json"))
        assertFalse(rs.getBoolean("read"))
    }

    @Test
    fun `findByUserId — 사용자의 알림 목록 조회`() {
        insertNotification(userId = 1L)
        insertNotification(userId = 1L)
        insertNotification(userId = 2L)

        val ps = conn.prepareStatement(
            "SELECT * FROM notification.notifications WHERE user_id = ? ORDER BY id DESC"
        )
        ps.setLong(1, 1L)
        val rs = ps.executeQuery()
        var count = 0
        while (rs.next()) count++
        assertEquals(2, count)
    }

    @Test
    fun `markRead — 읽음 처리 후 read true`() {
        val id = insertNotification(userId = 1L, read = false)

        conn.prepareStatement("UPDATE notification.notifications SET read = TRUE WHERE id = ?").apply {
            setLong(1, id)
            executeUpdate()
        }

        val ps = conn.prepareStatement("SELECT read FROM notification.notifications WHERE id = ?")
        ps.setLong(1, id)
        val rs = ps.executeQuery()
        assertTrue(rs.next())
        assertTrue(rs.getBoolean("read"))
    }

    @Test
    fun `markAllRead — 사용자의 모든 알림 읽음 처리`() {
        insertNotification(userId = 1L, read = false)
        insertNotification(userId = 1L, read = false)
        insertNotification(userId = 2L, read = false)

        conn.prepareStatement(
            "UPDATE notification.notifications SET read = TRUE WHERE user_id = ? AND read = FALSE"
        ).apply {
            setLong(1, 1L)
            executeUpdate()
        }

        val ps = conn.prepareStatement(
            "SELECT COUNT(*) FROM notification.notifications WHERE user_id = 1 AND read = FALSE"
        )
        val rs = ps.executeQuery()
        assertTrue(rs.next())
        assertEquals(0, rs.getLong(1))
    }

    @Test
    fun `idx_notifications_user 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'notification' AND tablename = 'notifications' AND indexname = 'idx_notifications_user'"
        )
        assertTrue(rs.next(), "idx_notifications_user 인덱스가 존재해야 합니다")
    }

    @Test
    fun `idx_notifications_user_unread 인덱스 존재 확인`() {
        val rs = conn.createStatement().executeQuery(
            "SELECT indexname FROM pg_indexes WHERE schemaname = 'notification' AND tablename = 'notifications' AND indexname = 'idx_notifications_user_unread'"
        )
        assertTrue(rs.next(), "idx_notifications_user_unread 인덱스가 존재해야 합니다")
    }
}
