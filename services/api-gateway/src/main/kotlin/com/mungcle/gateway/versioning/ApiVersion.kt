package com.mungcle.gateway.versioning

import java.time.LocalDate

/**
 * 알려진 API 버전 목록.
 * 새 breaking change 날짜를 여기에 추가한다.
 */
enum class ApiVersion(val date: LocalDate) {
    V_2026_04_23(LocalDate.of(2026, 4, 23));

    companion object {
        /** 최신 안정 버전 */
        val LATEST: ApiVersion = entries.last()

        /**
         * 날짜 문자열(YYYY-MM-DD)로 버전을 해석한다.
         * - 정확히 일치하는 버전이 있으면 그것을 반환
         * - 알려진 가장 최근 버전보다 미래 날짜이면 LATEST 반환
         * - 형식 오류이면 null 반환
         */
        fun resolve(dateStr: String): ApiVersion? {
            val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return null
            // 요청 날짜 이하인 버전 중 가장 최신 것을 선택 (Stripe 모델)
            return entries.filter { it.date <= date }.maxByOrNull { it.date }
        }
    }
}
