package com.ai.batchapp.infrastructure.persistence.repository

import com.ai.batchapp.infrastructure.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, Long>
