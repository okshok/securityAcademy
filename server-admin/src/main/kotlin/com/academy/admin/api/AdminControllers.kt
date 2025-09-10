package com.academy.admin.api

import com.academy.admin.domain.*
import com.academy.admin.repo.*
import org.springframework.web.bind.annotation.*
import java.time.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api")
class AdminController(
    private val candidateRepo: CandidateRepo,
    private val questionRepo: QuestionRepo,
    private val resolutionRepo: ResolutionRepo,
    private val scoreRepo: ScoreRepo,
    private val predictionRepo: PredictionRepo,
    private val questionCandidateBatchService: com.academy.admin.service.QuestionCandidateBatchService
) {


    data class ResolveReq(val outcome: Outcome, val proofUrl: String? = null)
    // 정답 확정 및 채점 (정답자에게 +10점 부여)
    @PatchMapping("/questions/{id}/resolve")
    fun resolve(@PathVariable id: Long, @RequestBody req: ResolveReq): ResponseEntity<Any> {
        // set resolution
        resolutionRepo.save(Resolution(question_id = id, outcome = req.outcome, proof_url = req.proofUrl))
        // close question
        val q = questionRepo.findById(id).orElseThrow()
        questionRepo.save(q.copy(status = QuestionStatus.RESOLVED))
        // scoring: +10 for correct predictions of demo season 1
        val preds = predictionRepo.findByQuestionId(id)
        val correctUserIds: List<Long> = preds.filter { it.choice == req.outcome.name }.map { it.user_id }
        correctUserIds.forEach { uid: Long ->
            val existing = scoreRepo.findAll().find { it.user_id == uid && it.season_id == q.season_id }
            if (existing == null) {
                scoreRepo.save(Score(user_id = uid, season_id = q.season_id, total_points = 10))
            } else {
                scoreRepo.save(existing.copy(total_points = existing.total_points + 10))
            }
        }
        return ResponseEntity.ok(mapOf("ok" to true, "scored" to correctUserIds.size))
    }

    // 채점 완료된 문제 목록 조회 (정답 및 해설 확인용)
    @GetMapping("/questions/resolved")
    fun resolved(): List<Resolution> = resolutionRepo.findAll()
    
    // 수동으로 문제 후보 생성 배치 실행
    @PostMapping("/batch/generate-candidates")
    fun generateCandidates(): ResponseEntity<Map<String, Any>> {
        return try {
            questionCandidateBatchService.generateQuestionCandidates()
            ResponseEntity.ok(mapOf("success" to true, "message" to "문제 후보 생성 완료") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }
    

}