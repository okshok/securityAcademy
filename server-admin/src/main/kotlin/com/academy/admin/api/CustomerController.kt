package com.academy.admin.api

import com.academy.admin.domain.*
import com.academy.admin.dto.*
import com.academy.admin.repo.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@RestController
@RequestMapping("/api/customer")
class CustomerController(
    private val questionRepo: QuestionRepo,
    private val predictionRepo: PredictionRepo,
    private val scoreRepo: ScoreRepo
) {
    private val mapper = jacksonObjectMapper()

    // 오늘의 문제 목록 조회 (현재 진행 중인 문제들)
    @GetMapping("/questions/today")
    fun questionsToday(): List<QuestionItem> {
        val now = LocalDateTime.now()
        return questionRepo.findByStatusAndCloses_atAfter(QuestionStatus.OPEN, now).map { q ->
            QuestionItem(
                id = q.id!!,
                prompt = q.prompt,
                pros = q.pros?.let { mapper.readValue(it) } ?: emptyList(),
                cons = q.cons?.let { mapper.readValue(it) } ?: emptyList(),
                closesAt = q.closes_at
            )
        }
    }

    data class PredictionReq(val questionId: Long, val choice: String)
    // O/X 예측 제출 (한 문제당 한 번만 제출 가능)
    @PostMapping("/predictions")
    fun submit(@RequestBody body: PredictionReq): ResponseEntity<Any> {
        val userId = 1L // demo user
        if (predictionRepo.existsByUser_idAndQuestion_id(userId, body.questionId)) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Already submitted"))
        }
        val p = Prediction(user_id = userId, question_id = body.questionId, choice = body.choice)
        predictionRepo.save(p)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    // 시즌 리더보드 조회 (동점자는 같은 순위로 처리)
    @GetMapping("/leaderboard")
    fun leaderboard(): List<Map<String, Any>> {
        val seasonId = 1L
        val scores = scoreRepo.findBySeason_id(seasonId).sortedByDescending { it.total_points }
        var rank = 1
        var lastPoints: Int? = null
        var lastRank = 1
        return scores.map {
            if (lastPoints == null || it.total_points != lastPoints) {
                lastPoints = it.total_points
                lastRank = rank
            }
            val row = mapOf(
                "userId" to it.user_id,
                "seasonId" to it.season_id,
                "totalPoints" to it.total_points,
                "rank" to lastRank
            )
            rank += 1
            row
        }
    }

    // 내 점수 및 제출 이력 조회 (개인 성적표)
    @GetMapping("/me/scores")
    fun myScores(): Map<String, Any> {
        val userId = 1L
        val seasonId = 1L
        val score = scoreRepo.findByUser_idAndSeason_id(userId, seasonId)?.total_points ?: 0
        val predictions = predictionRepo.findByUser_id(userId)
        return mapOf("totalPoints" to score, "submissions" to predictions.size)
    }
}
