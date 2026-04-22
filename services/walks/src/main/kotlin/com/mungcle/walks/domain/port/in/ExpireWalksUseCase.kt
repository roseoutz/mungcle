package com.mungcle.walks.domain.port.`in`

import java.time.Instant

/**
 * 만료된 산책을 일괄 종료하는 유즈케이스.
 */
interface ExpireWalksUseCase {
    /**
     * endsAt이 now 이전인 ACTIVE 산책을 모두 종료 처리한다.
     * @param now 기준 시각
     * @return 만료 처리된 산책 수
     */
    fun execute(now: Instant): Int
}
