package com.mungcle.notification.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedEventSpringDataRepository : JpaRepository<ProcessedEventEntity, String>
