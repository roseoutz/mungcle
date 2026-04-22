package com.mungcle.petprofile.domain.model

import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.exception.InvalidTemperamentCountException
import java.time.Instant

/**
 * 반려견 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
class Dog(
    val id: Long = 0,
    val ownerId: Long,
    name: String,
    breed: String,
    size: DogSize,
    temperaments: List<Temperament>,
    sociability: Int,
    photoPath: String? = null,
    vaccinationPhotoPath: String? = null,
    deletedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
) {
    var name: String = name
        private set

    var breed: String = breed
        private set

    var size: DogSize = size
        private set

    var temperaments: List<Temperament> = temperaments
        private set

    var sociability: Int = sociability
        private set

    var photoPath: String? = photoPath
        private set

    var vaccinationPhotoPath: String? = vaccinationPhotoPath
        private set

    var deletedAt: Instant? = deletedAt
        private set

    init {
        validateTemperaments(this.temperaments)
        require(this.sociability in 1..5) { "사교성 점수는 1~5 사이여야 합니다" }
    }

    /** 예방접종 사진 등록 여부 */
    fun isVaccinationRegistered(): Boolean = !vaccinationPhotoPath.isNullOrBlank()

    /** 소유권 확인. 불일치 시 [DogNotOwnedException] 발생 */
    fun verifyOwnership(requesterId: Long) {
        if (ownerId != requesterId) throw DogNotOwnedException(id, requesterId)
    }

    /**
     * 부분 업데이트. null 인 파라미터는 기존 값 유지.
     * temperaments 제공 시 1~3개 검증, sociability 제공 시 1~5 검증.
     */
    fun update(
        name: String? = null,
        breed: String? = null,
        size: DogSize? = null,
        temperaments: List<Temperament>? = null,
        sociability: Int? = null,
        photoPath: String? = null,
        vaccinationPhotoPath: String? = null,
    ) {
        if (temperaments != null) validateTemperaments(temperaments)
        if (sociability != null) require(sociability in 1..5) { "사교성 점수는 1~5 사이여야 합니다" }

        if (name != null) this.name = name
        if (breed != null) this.breed = breed
        if (size != null) this.size = size
        if (temperaments != null) this.temperaments = temperaments
        if (sociability != null) this.sociability = sociability
        if (photoPath != null) this.photoPath = photoPath
        if (vaccinationPhotoPath != null) this.vaccinationPhotoPath = vaccinationPhotoPath
    }

    /** 사진 경로를 null 로 초기화 */
    fun clearPhotoPath() { this.photoPath = null }

    /** 예방접종 사진 경로를 null 로 초기화 */
    fun clearVaccinationPhotoPath() { this.vaccinationPhotoPath = null }

    /** 소프트 삭제 */
    fun softDelete(): Dog {
        this.deletedAt = Instant.now()
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Dog) return false
        // 미저장 엔티티(id=0)는 참조 동일성만 허용
        if (id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)

    override fun toString(): String =
        "Dog(id=$id, ownerId=$ownerId, name=$name, breed=$breed, size=$size)"

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
