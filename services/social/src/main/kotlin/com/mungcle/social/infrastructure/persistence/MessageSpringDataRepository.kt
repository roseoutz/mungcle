package com.mungcle.social.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MessageSpringDataRepository : JpaRepository<MessageEntity, Long> {

    @Query("SELECT m FROM MessageEntity m WHERE m.greetingId = :greetingId ORDER BY m.createdAt ASC")
    fun findByGreetingId(@Param("greetingId") greetingId: Long): List<MessageEntity>
}
