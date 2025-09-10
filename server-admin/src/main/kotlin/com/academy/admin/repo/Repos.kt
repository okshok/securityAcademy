package com.academy.admin.repo

import com.academy.admin.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.*

@Repository interface CandidateRepo: JpaRepository<QuestionCandidate, Long> {
    @Query("SELECT c FROM QuestionCandidate c WHERE c.candidate_date = :date")
    fun findByCandidateDate(@Param("date") date: LocalDate): List<QuestionCandidate>
}
@Repository interface QuestionRepo: JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.status = :status AND q.closes_at > :now")
    fun findByStatusAndCloses_atAfter(@Param("status") status: QuestionStatus, @Param("now") now: LocalDateTime): List<Question>
}
@Repository interface ResolutionRepo: JpaRepository<Resolution, Long>
@Repository interface ScoreRepo: JpaRepository<Score, Long> {
    @Query("SELECT s FROM Score s WHERE s.season_id = :seasonId")
    fun findBySeason_id(@Param("seasonId") seasonId: Long): List<Score>
    
    @Query("SELECT s FROM Score s WHERE s.user_id = :userId AND s.season_id = :seasonId")
    fun findByUser_idAndSeason_id(@Param("userId") userId: Long, @Param("seasonId") seasonId: Long): Score?
}
@Repository interface PredictionRepo: JpaRepository<Prediction, Long> {
    @Query("SELECT p FROM Prediction p WHERE p.question_id = :questionId")
    fun findByQuestionId(@Param("questionId") questionId: Long): List<Prediction>
    
    @Query("SELECT COUNT(p) > 0 FROM Prediction p WHERE p.user_id = :userId AND p.question_id = :questionId")
    fun existsByUser_idAndQuestion_id(@Param("userId") userId: Long, @Param("questionId") questionId: Long): Boolean
    
    @Query("SELECT p FROM Prediction p WHERE p.user_id = :userId")
    fun findByUser_id(@Param("userId") userId: Long): List<Prediction>
}