package com.academy.admin.service

import com.academy.admin.domain.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.*

@Service
class AnswerGenerationService(
    private val webClient: WebClient.Builder,
    private val investingCalendarService: InvestingCalendarService,
    private val bingNewsService: BingNewsService
) {
    private val objectMapper = ObjectMapper()
    private val geminiApiKey = "AIzaSyCfk5odAmYItK7Av2ee6WGcg6zogvu57yA"
    private val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    data class AnswerResult(
        val outcome: Outcome,
        val explanation: String,
        val proofUrl: String?,
        val reasoning: String
    )

    fun generateAnswer(question: Question): AnswerResult {
        // 1. Investing.com에서 관련 정보 수집
        val today = LocalDate.now()
        val economicEvents = investingCalendarService.getEconomicCalendar(today)
        val earningsEvents = investingCalendarService.getEarningsCalendar(today)
        
        // 2. 뉴스 정보 수집
        val news = bingNewsService.searchNews(question.ticker ?: "주식시장")
        
        // 3. AI를 통한 정답 분석
        val prompt = """
            다음 문제에 대한 정답을 분석해주세요:
            
            문제: ${question.prompt}
            티커: ${question.ticker ?: "없음"}
            문제의 중요성: ${question.importance ?: "없음"}
            예상 파급효과: ${question.impact ?: "없음"}
            
            관련 경제 이벤트:
            ${economicEvents.joinToString("\n") { "- ${it.event} (${it.currency}): ${it.actual ?: "N/A"}" }}
            
            관련 실적 이벤트:
            ${earningsEvents.joinToString("\n") { "- ${it.company} (${it.symbol}): EPS ${it.epsForecast ?: "N/A"}" }}
            
            관련 뉴스:
            ${news.take(3).joinToString("\n") { "- ${it.name}" }}
            
            JSON 형태로 다음 필드들을 반환해주세요:
            {
                "outcome": "O 또는 X",
                "explanation": "정답에 대한 상세한 해설 (3-4문장)",
                "proofUrl": "증명할 수 있는 뉴스나 데이터 URL (선택사항)",
                "reasoning": "정답을 결정한 근거와 분석 과정 (2-3문장)"
            }
        """.trimIndent()

        val aiResponse = callGeminiAPI(prompt)
        return parseAnswerResponse(aiResponse)
    }

    fun generateExplanation(question: Question, resolution: Resolution, instruction: String): String {
        val prompt = """
            다음 문제와 정답에 대한 해설을 생성해주세요:
            
            문제: ${question.prompt}
            정답: ${resolution.outcome}
            기존 해설: ${resolution.explanation ?: "없음"}
            
            추가 요청사항: ${instruction}
            
            해설에는 다음 내용을 포함해주세요:
            1. 정답이 나온 이유
            2. 시장 상황 분석
            3. 투자자들에게 주는 시사점
            4. 향후 전망
            
            상세하고 이해하기 쉬운 해설을 작성해주세요.
        """.trimIndent()

        return callGeminiAPI(prompt)
    }

    private fun callGeminiAPI(prompt: String): String {
        return try {
            val request = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                )
            )

            val response = webClient.build()
                .post()
                .uri("$geminiUrl?key=$geminiApiKey")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val candidates = (response as? Map<String, Any>)?.get("candidates") as? List<*>
            val content = candidates?.firstOrNull() as? Map<String, Any>
            val parts = content?.get("content") as? Map<String, Any>
            val textParts = parts?.get("parts") as? List<*>
            val text = textParts?.firstOrNull() as? Map<String, Any>
            
            text?.get("text") as? String ?: "AI 응답 생성 실패"
        } catch (e: Exception) {
            "AI 응답 생성 실패: ${e.message}"
        }
    }

    private fun parseAnswerResponse(aiResponse: String): AnswerResult {
        return try {
            // JSON 블록을 찾아서 추출
            val jsonPattern = """```json\s*(\{.*?\})\s*```""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonPattern.find(aiResponse)
            
            val jsonString = if (jsonMatch != null) {
                jsonMatch.groupValues[1]
            } else {
                aiResponse
            }
            
            val responseMap = objectMapper.readValue(jsonString, Map::class.java) as Map<String, Any>
            
            AnswerResult(
                outcome = Outcome.valueOf(responseMap["outcome"] as String),
                explanation = responseMap["explanation"] as String,
                proofUrl = responseMap["proofUrl"] as? String,
                reasoning = responseMap["reasoning"] as String
            )
        } catch (e: Exception) {
            AnswerResult(
                outcome = Outcome.O,
                explanation = "AI 응답 파싱 실패로 인한 기본 정답",
                proofUrl = null,
                reasoning = "AI 응답을 파싱할 수 없어 기본값으로 설정"
            )
        }
    }
}
