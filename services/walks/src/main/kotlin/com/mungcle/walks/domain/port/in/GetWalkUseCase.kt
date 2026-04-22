package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.Walk

interface GetWalkUseCase {
    /** 산책 ID로 산책 정보를 조회한다 */
    fun execute(walkId: Long): Walk
}
