package com.academy.admin.domain

import jakarta.persistence.*
import java.time.*

@Entity @Table(name="users")
data class User(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) val id: Long? = null,
    val email: String,
    val nickname: String,
    val created_at: LocalDateTime = LocalDateTime.now()
)

@Entity @Table(name="seasons")
data class Season(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) val id: Long? = null,
    val name: String,
    val start_at: LocalDateTime,
    val end_at: LocalDateTime,
    val is_active: Boolean = false
)

@Entity @Table(name="question_candidates")
data class QuestionCandidate(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) val id: Long? = null,
    val candidate_date: LocalDate,
    @Enumerated(EnumType.STRING) val type: QuestionType = QuestionType.INDEX,
    val ticker: String? = null,
    @Column(length=500) val prompt: String,
    @Lob val pros: String? = null, // JSON
    @Lob val cons: String? = null, // JSON
    @Enumerated(EnumType.STRING) val status: CandidateStatus = CandidateStatus.CANDIDATE,
    val created_at: LocalDateTime = LocalDateTime.now()
)

@Entity @Table(name="questions")
data class Question(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) val id: Long? = null,
    val season_id: Long,
    val ticker: String? = null,
    @Column(length=500) val prompt: String,
    @Lob val pros: String? = null, // JSON
    @Lob val cons: String? = null, // JSON
    val closes_at: LocalDateTime,
    @Enumerated(EnumType.STRING) val status: QuestionStatus = QuestionStatus.DRAFT
)

@Entity @Table(name="predictions")
data class Prediction(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) val id: Long? = null,
    val user_id: Long,
    val question_id: Long,
    val choice: String, // "O" or "X"
    val created_at: LocalDateTime = LocalDateTime.now()
)

@Entity @Table(name="resolutions")
data class Resolution(
    @Id val question_id: Long,
    @Enumerated(EnumType.STRING) val outcome: Outcome,
    val resolved_at: LocalDateTime = LocalDateTime.now(),
    val proof_url: String? = null
)

@Entity @Table(name="scores")
data class Score(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) val id: Long? = null,
    val user_id: Long,
    val season_id: Long,
    val total_points: Int = 0
)

@Entity @Table(name="news")
data class News(
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) val id: Long? = null,
    val ticker: String,
    @Column(length=500) val headline: String,
    val published_at: LocalDateTime,
    val link: String?
)
