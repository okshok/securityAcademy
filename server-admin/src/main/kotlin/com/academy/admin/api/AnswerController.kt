package com.academy.admin.api

import com.academy.admin.domain.*
import com.academy.admin.repo.*
import com.academy.admin.service.GeminiAIService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/answers")
class AnswerController(
    private val questionRepo: QuestionRepo,
    private val resolutionRepo: ResolutionRepo,
    private val geminiAIService: GeminiAIService
) {
    
    // 정답이 필요한 문제 목록 조회 (확정된 문제들)
    @GetMapping("/questions")
    fun getQuestionsNeedingAnswers(): List<Question> {
        return questionRepo.findAll().filter { it.status == QuestionStatus.OPEN || it.status == QuestionStatus.CLOSED }
    }
    
    // 특정 문제의 정답 조회
    @GetMapping("/questions/{questionId}")
    fun getQuestionAnswer(@PathVariable questionId: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(questionId).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val resolution = resolutionRepo.findById(questionId).orElse(null)
            
            val result = mapOf(
                "question" to question,
                "resolution" to resolution
            ) as Map<String, Any>
            
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
    
    // 정답 생성
    data class CreateAnswerReq(val outcome: Outcome, val proofUrl: String? = null, val explanation: String? = null)
    @PostMapping("/questions/{questionId}")
    fun createAnswer(@PathVariable questionId: Long, @RequestBody req: CreateAnswerReq): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(questionId).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            
            val resolution = Resolution(
                question_id = questionId,
                outcome = req.outcome,
                proof_url = req.proofUrl,
                resolved_at = LocalDateTime.now()
            )
            resolutionRepo.save(resolution)
            
            // 문제 상태를 RESOLVED로 변경
            val updatedQuestion = question.copy(status = QuestionStatus.RESOLVED)
            questionRepo.save(updatedQuestion)
            
            ResponseEntity.ok(mapOf("success" to true, "message" to "정답이 생성되었습니다") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }
    
    // 정답 수정
    data class UpdateAnswerReq(val outcome: Outcome?, val proofUrl: String?, val explanation: String?)
    @PatchMapping("/questions/{questionId}")
    fun updateAnswer(@PathVariable questionId: Long, @RequestBody req: UpdateAnswerReq): ResponseEntity<Map<String, Any>> {
        return try {
            val resolution = resolutionRepo.findById(questionId).orElseThrow { RuntimeException("정답을 찾을 수 없습니다") }
            
            val updated = resolution.copy(
                outcome = req.outcome ?: resolution.outcome,
                proof_url = req.proofUrl ?: resolution.proof_url,
                resolved_at = LocalDateTime.now()
            )
            resolutionRepo.save(updated)
            
            ResponseEntity.ok(mapOf("success" to true, "message" to "정답이 수정되었습니다") as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }
    
    // AI를 통한 정답 해설 생성
    data class AIExplanationReq(val instruction: String)
    @PostMapping("/questions/{questionId}/ai-explanation")
    fun generateAIExplanation(@PathVariable questionId: Long, @RequestBody req: AIExplanationReq): ResponseEntity<Map<String, Any>> {
        return try {
            val question = questionRepo.findById(questionId).orElseThrow { RuntimeException("문제를 찾을 수 없습니다") }
            val resolution = resolutionRepo.findById(questionId).orElse(null)
            
            val prompt = """
                다음 문제에 대한 정답 해설을 작성해주세요:
                
                문제: ${question.prompt}
                정답: ${resolution?.outcome ?: "미정"}
                증명 URL: ${resolution?.proof_url ?: "없음"}
                
                추가 요청: ${req.instruction}
                
                해설에는 다음 내용을 포함해주세요:
                1. 정답 근거
                2. 관련 뉴스나 시장 상황
                3. 투자 관점에서의 분석
                
                한국어로 작성해주세요.
            """.trimIndent()
            
            val aiResponse = geminiAIService.callGeminiAPI(prompt)
            ResponseEntity.ok(mapOf("success" to true, "explanation" to aiResponse) as Map<String, Any>)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("success" to false, "message" to (e.message ?: "알 수 없는 오류")) as Map<String, Any>)
        }
    }
    
    // 정답 목록 조회 (모든 정답)
    @GetMapping
    fun getAllAnswers(): List<Resolution> {
        return resolutionRepo.findAll()
    }
}
