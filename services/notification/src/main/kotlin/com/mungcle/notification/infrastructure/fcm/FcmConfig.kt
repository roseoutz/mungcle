package com.mungcle.notification.infrastructure.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.util.Base64

/**
 * Firebase Admin SDK 초기화 설정.
 * FCM_SERVICE_ACCOUNT_KEY 환경변수에 Base64 인코딩된 서비스 계정 JSON을 기대한다.
 */
@Configuration
class FcmConfig {

    private val log = LoggerFactory.getLogger(FcmConfig::class.java)

    @Value("\${fcm.service-account-key:}")
    private lateinit var serviceAccountKeyBase64: String

    @Bean
    fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isNotEmpty()) {
            return FirebaseApp.getInstance()
        }
        val options = if (serviceAccountKeyBase64.isNotBlank()) {
            val keyBytes = Base64.getDecoder().decode(serviceAccountKeyBase64)
            val credentials = GoogleCredentials.fromStream(ByteArrayInputStream(keyBytes))
            FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()
        } else {
            log.warn("FCM 서비스 계정 키가 설정되지 않음 — ApplicationDefaultCredentials 사용 (개발 환경)")
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
        }
        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseMessaging(firebaseApp: FirebaseApp): FirebaseMessaging =
        FirebaseMessaging.getInstance(firebaseApp)
}
