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
    private val predictionRepo: PredictionRepo
) {

    // 오늘 생성된 문제 후보군 목록 조회 (AI가 생성한 문제 후보들)
    @GetMapping("/questions/drafts")
    fun drafts(): List<QuestionCandidate> =
        candidateRepo.findByCandidateDate(LocalDate.now())

    data class CreateQuestionReq(val candidateId: Long?, val prompt: String, val seasonId: Long, val ticker: String?, val pros: String?, val cons: String?, val closesAt: LocalDateTime)
    // 문제 생성 (후보군에서 선택하거나 새로 생성)
    @PostMapping("/questions")
    fun createQuestion(@RequestBody req: CreateQuestionReq): Question {
        val q = Question(
            season_id = req.seasonId,
            prompt = req.prompt,
            ticker = req.ticker,
            pros = req.pros,
            cons = req.cons,
            closes_at = req.closesAt,
            status = QuestionStatus.DRAFT
        )
        val saved = questionRepo.save(q)
        req.candidateId?.let {
            candidateRepo.findById(it).ifPresent { c ->
                candidateRepo.save(c.copy(status = CandidateStatus.SELECTED))
            }
        }
        return saved
    }

    // 문제 확정 (DRAFT → OPEN 상태로 변경하여 고객에게 공개)
    @PatchMapping("/questions/{id}/confirm")
    fun confirm(@PathVariable id: Long): ResponseEntity<Any> {
        val q = questionRepo.findById(id).orElseThrow()
        val upd = q.copy(status = QuestionStatus.OPEN)
        questionRepo.save(upd)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

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
}