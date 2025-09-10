package com.academy.admin.api

import com.academy.admin.domain.*
import com.academy.admin.repo.*
import com.academy.admin.service.AnswerGenerationService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import java.time.*

@RestController
@RequestMapping("/api/admin/answers")
class AdminAnswerController(
    private val questionRepo: QuestionRepo,
    private val resolutionRepo: ResolutionRepo,
    private val answerGenerationService: AnswerGenerationService
) {

    // 정답이 필요한 문제 목록 조회 (마감된 문제들)
    @GetMapping("/questions")
    fun getQuestionsNeedingAnswers(): List<Question> =
        questionRepo.findByStatus(QuestionStatus.CLOSED)

    // 문제 상세 조회 (정답 포함)
    @GetMapping("/questions/{id}")
    fun getQuestionWithAnswer(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val resolution = resolutionRepo.findByQuestionId(id)
            
            val result = mapOf(
                "question" to question,
                "resolution" to resolution
            ) as Map<String, Any>
            
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    // 정답지 자동 생성
    @PostMapping("/questions/{id}/generate-answer")
    fun generateAnswer(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val answer = answerGenerationService.generateAnswer(question)
            
            // 생성된 정답을 데이터베이스에 저장
            val resolution = Resolution(
                question_id = id,
                outcome = answer.outcome,
                proof_url = answer.proofUrl,
                explanation = answer.explanation,
                resolved_at = LocalDateTime.now()
            )
            resolutionRepo.save(resolution)
            
            // 문제 상태를 RESOLVED로 변경
            questionRepo.save(question.copy(status = QuestionStatus.RESOLVED))
            
            ResponseEntity.ok(mapOf("success" to true, "answer" to answer) as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }

    // 정답 생성
    data class CreateAnswerReq(val outcome: Outcome, val proofUrl: String?, val explanation: String?)
    
    @PostMapping("/questions/{id}")
    fun createAnswer(@PathVariable id: Long, @RequestBody req: CreateAnswerReq): ResponseEntity<Map<String, Any>> {
        return try {
            val resolution = Resolution(
                question_id = id,
                outcome = req.outcome,
                proof_url = req.proofUrl,
                explanation = req.explanation,
                resolved_at = LocalDateTime.now()
            )
            resolutionRepo.save(resolution)
            
            // 문제 상태를 RESOLVED로 변경
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            questionRepo.save(question.copy(status = QuestionStatus.RESOLVED))
            
            ResponseEntity.ok(mapOf("success" to true, "message" to "정답이 생성되었습니다") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }

    // 정답 수정
    data class UpdateAnswerReq(val outcome: Outcome?, val proofUrl: String?, val explanation: String?)
    
    @PatchMapping("/questions/{id}")
    fun updateAnswer(@PathVariable id: Long, @RequestBody req: UpdateAnswerReq): ResponseEntity<Map<String, Any>> {
        return try {
            val resolution = resolutionRepo.findByQuestionId(id) ?: throw RuntimeException("정답을 찾을 수 없습니다")
            val updated = resolution.copy(
                outcome = req.outcome ?: resolution.outcome,
                proof_url = req.proofUrl ?: resolution.proof_url,
                explanation = req.explanation ?: resolution.explanation
            )
            resolutionRepo.save(updated)
            
            ResponseEntity.ok(mapOf("success" to true, "message" to "정답이 수정되었습니다") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }

    // AI 해설 생성
    data class AIExplanationReq(val instruction: String)
    
    @PostMapping("/questions/{id}/ai-explanation")
    fun generateAIExplanation(@PathVariable id: Long, @RequestBody req: AIExplanationReq): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(id).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val resolution = resolutionRepo.findByQuestionId(id) ?: throw RuntimeException("정답을 찾을 수 없습니다")
            
            val explanation = answerGenerationService.generateExplanation(question, resolution, req.instruction)
            
            ResponseEntity.ok(mapOf("success" to true, "explanation" to explanation) as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }

    // 만료된 문제들을 자동으로 CLOSED 상태로 변경
    @PostMapping("/close-expired")
    fun closeExpiredQuestions(): ResponseEntity<Map<String, Any>> {
        return try {
            val now = LocalDateTime.now()
            val expiredQuestions = questionRepo.findExpiredOpenQuestions(now)
            
            expiredQuestions.forEach { question ->
                questionRepo.save(question.copy(status = QuestionStatus.CLOSED))
            }
            
            ResponseEntity.ok(mapOf("success" to true, "closedCount" to expiredQuestions.size) as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }
}
