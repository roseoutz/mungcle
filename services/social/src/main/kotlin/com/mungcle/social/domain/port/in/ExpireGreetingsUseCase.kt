package com.mungcle.social.domain.port.`in`

import java.time.Instant

interface ExpireGreetingsUseCase {
    /** 만료 처리하고 만료된 건수를 반환한다. */
    fun execute(now: Instant): Int
}
