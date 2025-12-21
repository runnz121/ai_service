package com.ai.batchapp.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "user_profiles")
data class UserProfileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: UserEntity,

    @Column(name = "full_name")
    val fullName: String? = null,

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    @Column(length = 500)
    val address: String? = null,

    val city: String? = null,
    val country: String? = null,

    @Column(name = "postal_code")
    val postalCode: String? = null,

    @Column(name = "birth_date")
    val birthDate: LocalDate? = null,

    val gender: String? = null,

    @Column(name = "profile_image_url")
    val profileImageUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    val bio: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
