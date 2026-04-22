package com.mungcle.notification.application.query

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.ListNotificationsUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ListNotificationsQueryHandlerTest {

    private val notificationRepository: NotificationRepositoryPort = mockk()
    private val handler = ListNotificationsQueryHandler(notificationRepository)

    private fun makeNotification(id: Long, userId: Long = 1L): Notification = Notification(
        id = id,
        userId = userId,
        type = NotificationType.GREETING_RECEIVED,
        payloadJson = "{}",
        read = false,
        createdAt = Instant.now(),
    )

    @Test
    fun `첫 페이지 조회 — cursor null, limit 2, 결과 2개`() {
        val notifications = listOf(makeNotification(3L), makeNotification(2L))
        every { notificationRepository.findByUserId(1L, null, 3) } returns notifications

        val result = handler.execute(ListNotificationsUseCase.Query(userId = 1L, cursor = null, limit = 2))

        assertEquals(2, result.notifications.size)
        assertNull(result.nextCursor)
    }

    @Test
    fun `다음 페이지 있을 때 nextCursor 반환`() {
        // limit=2 이므로 3개 요청, 3개 반환되면 hasNext=true
        val notifications = listOf(makeNotification(5L), makeNotification(4L), makeNotification(3L))
        every { notificationRepository.findByUserId(1L, null, 3) } returns notifications

        val result = handler.execute(ListNotificationsUseCase.Query(userId = 1L, cursor = null, limit = 2))

        assertEquals(2, result.notifications.size)
        assertEquals(4L, result.nextCursor)
    }

    @Test
    fun `빈 결과 — 알림 없음`() {
        every { notificationRepository.findByUserId(1L, null, 21) } returns emptyList()

        val result = handler.execute(ListNotificationsUseCase.Query(userId = 1L, cursor = null, limit = 20))

        assertEquals(0, result.notifications.size)
        assertNull(result.nextCursor)
    }

    @Test
    fun `cursor가 있을 때 cursor 이후 알림 조회`() {
        val notifications = listOf(makeNotification(2L), makeNotification(1L))
        every { notificationRepository.findByUserId(1L, 3L, 3) } returns notifications

        val result = handler.execute(ListNotificationsUseCase.Query(userId = 1L, cursor = 3L, limit = 2))

        assertEquals(2, result.notifications.size)
        assertNull(result.nextCursor)
    }
}
