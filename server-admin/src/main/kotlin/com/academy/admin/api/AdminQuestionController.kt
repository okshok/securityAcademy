package com.academy.admin.api

import com.academy.admin.domain.*
import com.academy.admin.repo.*
import org.springframework.web.bind.annotation.*
import java.time.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/questions")
class AdminQuestionController(
    private val candidateRepo: CandidateRepo,
    private val questionRepo: QuestionRepo
) {

    // 오늘 생성된 문제 후보군 목록 조회 (AI가 생성한 문제 후보들)
    @GetMapping("/drafts")
    fun drafts(): List<QuestionCandidate> =
        candidateRepo.findByCandidateDate(LocalDate.now())

    // 생성된 문제 목록 조회 (관리자용)
    @GetMapping
    fun getQuestions(): List<Question> =
        questionRepo.findAll()

    data class CreateQuestionReq(val candidateId: Long?, val prompt: String, val seasonId: Long, val ticker: String?, val pros: String?, val cons: String?, val importance: String?, val impact: String?, val closesAt: LocalDateTime)
    
    // 문제 생성 (후보군에서 선택하거나 새로 생성)
    @PostMapping
    fun createQuestion(@RequestBody req: CreateQuestionReq): Question {
        val q = Question(
            season_id = req.seasonId,
            prompt = req.prompt,
            ticker = req.ticker,
            pros = req.pros,
            cons = req.cons,
            importance = req.importance,
            impact = req.impact,
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

    // 문제 상세 조회
    @GetMapping("/{id}")
    fun getQuestion(@PathVariable id: Long): ResponseEntity<Question> {
        return try {
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            ResponseEntity.ok(question)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    // 문제 확정 (DRAFT → OPEN 상태로 변경하여 고객에게 공개)
    @PatchMapping("/{id}/confirm")
    fun confirm(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val q = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val updated = q.copy(status = QuestionStatus.OPEN)
            questionRepo.save(updated)
            ResponseEntity.ok(mapOf("success" to true, "message" to "문제가 확정되었습니다") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }

    // 문제 수정
    data class UpdateQuestionReq(val prompt: String?, val pros: String?, val cons: String?, val ticker: String?, val importance: String?, val impact: String?, val closesAt: LocalDateTime?)
    
    @PatchMapping("/{id}")
    fun updateQuestion(@PathVariable id: Long, @RequestBody req: UpdateQuestionReq): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val updated = question.copy(
                prompt = req.prompt ?: question.prompt,
                pros = req.pros ?: question.pros,
                cons = req.cons ?: question.cons,
                ticker = req.ticker ?: question.ticker,
                importance = req.importance ?: question.importance,
                impact = req.impact ?: question.impact,
                closes_at = req.closesAt ?: question.closes_at
            )
            questionRepo.save(updated)
            ResponseEntity.ok(mapOf("success" to true, "message" to "문제 수정 완료") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }

    // AI를 통한 문제 수정 요청
    data class AIUpdateReq(val instruction: String)
    
    @PostMapping("/{id}/ai-update")
    fun aiUpdateQuestion(@PathVariable id: Long, @RequestBody req: AIUpdateReq): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val aiService = com.academy.admin.service.GeminiAIService(org.springframework.web.reactive.function.client.WebClient.builder())

            val prompt = """
                다음 문제를 수정해주세요:

                현재 문제: ${question.prompt}
                현재 찬성 근거: ${question.pros}
                현재 반대 근거: ${question.cons}
                현재 티커: ${question.ticker ?: "없음"}

                수정 요청: ${req.instruction}

                JSON 형태로 다음 필드들을 반환해주세요:
                {
                    "prompt": "수정된 문제",
                    "pros": "수정된 찬성 근거 (JSON 배열)",
                    "cons": "수정된 반대 근거 (JSON 배열)",
                    "ticker": "수정된 티커 (선택사항)"
                }
            """.trimIndent()

            val aiResponse = aiService.callGeminiAPI(prompt)
            ResponseEntity.ok(mapOf("success" to true, "aiResponse" to aiResponse) as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }

    // 문제 강제 마감
    @PatchMapping("/{id}/force-close")
    fun forceCloseQuestion(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val updated = question.copy(status = QuestionStatus.CLOSED)
            questionRepo.save(updated)
            ResponseEntity.ok(mapOf("success" to true, "message" to "문제가 강제 마감되었습니다") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }
}
