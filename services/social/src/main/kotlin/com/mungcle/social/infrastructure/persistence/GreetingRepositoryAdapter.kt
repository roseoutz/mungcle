package com.mungcle.social.infrastructure.persistence

import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class GreetingRepositoryAdapter(
    private val springDataRepository: GreetingSpringDataRepository,
) : GreetingRepositoryPort {

    override fun save(greeting: Greeting): Greeting {
        val entity = GreetingMapper.toEntity(greeting)
        val saved = springDataRepository.save(entity)
        return GreetingMapper.toDomain(saved)
    }

    override fun saveAll(greetings: List<Greeting>): List<Greeting> {
        val entities = greetings.map(GreetingMapper::toEntity)
        return springDataRepository.saveAll(entities).map(GreetingMapper::toDomain)
    }

    override fun findById(id: Long): Greeting? =
        springDataRepository.findById(id).orElse(null)?.let(GreetingMapper::toDomain)

    override fun findBySenderAndWalk(senderUserId: Long, receiverWalkId: Long): Greeting? =
        springDataRepository.findBySenderAndWalk(senderUserId, receiverWalkId)
            .orElse(null)?.let(GreetingMapper::toDomain)

    override fun findByUserId(userId: Long, statusFilter: GreetingStatus?, isSender: Boolean?): List<Greeting> =
        springDataRepository.findByUserId(userId, statusFilter, isSender)
            .map(GreetingMapper::toDomain)

    override fun findExpiredPending(now: Instant): List<Greeting> =
        springDataRepository.findExpiredPending(now).map(GreetingMapper::toDomain)

    override fun findExpiredAccepted(now: Instant): List<Greeting> =
        springDataRepository.findExpiredAccepted(now).map(GreetingMapper::toDomain)
}
