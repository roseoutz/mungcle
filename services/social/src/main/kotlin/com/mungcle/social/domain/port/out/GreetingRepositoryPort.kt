package com.mungcle.social.domain.port.out

import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus

/**
 * Greeting 영속성 포트.
 * 인프라 레이어에서 구현한다.
 */
interface GreetingRepositoryPort {
    /** Greeting을 저장하고 저장된 Greeting을 반환한다. */
    fun save(greeting: Greeting): Greeting

    /** ID로 Greeting을 조회한다. 없으면 null. */
    fun findById(id: Long): Greeting?

    /** 동일 발신자-산책 조합의 Greeting을 조회한다. 중복 방지에 사용. */
    fun findBySenderAndWalk(senderUserId: Long, receiverWalkId: Long): Greeting?

    /** 사용자 ID와 선택적 필터로 Greeting 목록을 조회한다. */
    fun findByUserId(userId: Long, statusFilter: GreetingStatus?, isSender: Boolean?): List<Greeting>
}
