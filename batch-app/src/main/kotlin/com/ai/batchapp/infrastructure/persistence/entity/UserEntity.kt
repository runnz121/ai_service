package com.ai.batchapp.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val status: String = "ACTIVE",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var profile: UserProfileEntity? = null
)
