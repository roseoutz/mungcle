package com.mungcle.notification.domain.model

/**
 * NotificationType별 FCM 푸시 알림 제목/본문 템플릿.
 */
object NotificationPushTemplate {

    data class PushContent(val title: String, val body: String)

    fun of(type: NotificationType): PushContent = when (type) {
        NotificationType.GREETING_RECEIVED -> PushContent(
            title = "새 인사가 왔어요!",
            body = "누군가 산책에 합류하고 싶어해요 🐕",
        )
        NotificationType.GREETING_ACCEPTED -> PushContent(
            title = "인사가 수락됐어요!",
            body = "상대방이 함께 산책하기로 했어요",
        )
        NotificationType.MESSAGE_RECEIVED -> PushContent(
            title = "새 메시지가 도착했어요",
            body = "산책 친구가 메시지를 보냈어요",
        )
        NotificationType.WALK_EXPIRED -> PushContent(
            title = "산책이 종료됐어요",
            body = "60분 산책 시간이 끝났어요",
        )
    }
}
