package com.mungcle.petprofile.domain.model

import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.exception.InvalidTemperamentCountException
import java.time.Instant

/**
 * 반려견 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
data class Dog(
    val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val breed: String,
    val size: DogSize,
    val temperaments: List<Temperament>,
    val sociability: Int,
    val photoPath: String? = null,
    val vaccinationPhotoPath: String? = null,
    val deletedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
) {
    init {
        validateTemperaments(temperaments)
        require(sociability in 1..5) { "사교성 점수는 1~5 사이여야 합니다" }
    }

    /** 예방접종 사진 등록 여부 */
    fun isVaccinationRegistered(): Boolean = vaccinationPhotoPath != null

    /** 소유권 확인. 불일치 시 [DogNotOwnedException] 발생 */
    fun verifyOwnership(requesterId: Long) {
        if (ownerId != requesterId) throw DogNotOwnedException(id, requesterId)
    }

    /** 소프트 삭제 */
    fun softDelete(): Dog = copy(deletedAt = Instant.now())

    companion object {
        private const val MIN_TEMPERAMENTS = 1
        private const val MAX_TEMPERAMENTS = 3

        /** 성향 개수 검증 (1~3개) */
        fun validateTemperaments(temperaments: List<Temperament>) {
            if (temperaments.size !in MIN_TEMPERAMENTS..MAX_TEMPERAMENTS) {
                throw InvalidTemperamentCountException(temperaments.size)
            }
        }
    }
}
