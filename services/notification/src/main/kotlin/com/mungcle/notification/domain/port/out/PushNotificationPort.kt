package com.mungcle.notification.domain.port.out

/**
 * FCM 푸시 알림 발송 아웃바운드 포트.
 * domain 레이어는 구현 기술(Firebase)을 알지 못한다.
 */
interface PushNotificationPort {
    fun send(pushToken: String, title: String, body: String, data: Map<String, String>)
}
