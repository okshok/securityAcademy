package com.academy.admin.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class GeminiAIService(
    private val webClient: WebClient.Builder
) {
    private val objectMapper = ObjectMapper()
    private val geminiApiKey = "AIzaSyCfk5odAmYItK7Av2ee6WGcg6zogvu57yA"
    private val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    
    data class GeminiRequest(
        val contents: List<Content>
    )
    
    data class Content(
        val parts: List<Part>
    )
    
    data class Part(
        val text: String
    )
    
    data class GeminiResponse(
        val candidates: List<Candidate>
    )
    
    data class Candidate(
        val content: Content
    )
    
    // 경제 이벤트를 기반으로 문제 생성
    fun generateEconomicQuestion(event: InvestingCalendarService.CalendarEvent): String {
        val prompt = """
            다음 경제 이벤트를 바탕으로 O/X 예측 문제를 생성해주세요:
            
            이벤트: ${event.event}
            통화: ${event.currency}
            예상치: ${event.forecast ?: "N/A"}
            이전치: ${event.previous ?: "N/A"}
            영향도: ${event.impact}
            
            문제 형식: "[통화/지수]가 [시간]에 [조건]할 것인가? (O/X)"
            
            예시:
            - "USD/JPY가 오늘 15:30에 상승할 것인가? (O/X)"
            - "S&P500이 오늘 장 마감에 상승할 것인가? (O/X)"
            
            간단하고 명확한 문제 하나만 생성해주세요.
        """.trimIndent()
        
        return callGeminiAPI(prompt)
    }
    
    // 실적 이벤트를 기반으로 문제 생성
    fun generateEarningsQuestion(event: InvestingCalendarService.EarningsEvent): String {
        val prompt = """
            다음 실적 발표를 바탕으로 O/X 예측 문제를 생성해주세요:
            
            회사: ${event.company} (${event.symbol})
            시장: ${event.market}
            EPS 예상: ${event.epsForecast ?: "N/A"}
            매출 예상: ${event.revenueForecast ?: "N/A"}
            
            문제 형식: "[회사명]이 [시간]에 [조건]할 것인가? (O/X)"
            
            예시:
            - "Apple이 오늘 장후에 EPS 컨센서스를 상회할 것인가? (O/X)"
            - "Tesla가 오늘 장후에 매출 컨센서스를 상회할 것인가? (O/X)"
            
            간단하고 명확한 문제 하나만 생성해주세요.
        """.trimIndent()
        
        return callGeminiAPI(prompt)
    }
    
    // 일반적인 시황 문제 생성
    fun generateMarketQuestion(): String {
        val prompt = """
            오늘의 주식 시장 상황을 바탕으로 O/X 예측 문제를 생성해주세요.
            
            문제 형식: "[지수/종목]이 [시간]에 [조건]할 것인가? (O/X)"
            
            예시:
            - "S&P500이 오늘 장 마감에 상승할 것인가? (O/X)"
            - "NASDAQ이 오늘 장 마감에 상승할 것인가? (O/X)"
            - "KOSPI가 내일 오전에 상승할 것인가? (O/X)"
            
            간단하고 명확한 문제 하나만 생성해주세요.
        """.trimIndent()
        
        return callGeminiAPI(prompt)
    }
    
    fun callGeminiAPI(prompt: String): String {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            val response = webClient.build()
                .post()
                .uri("$geminiUrl?key=$geminiApiKey")
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse::class.java)
                .block()
            
            response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "문제 생성 실패"
        } catch (e: Exception) {
            println("Gemini API 호출 실패: ${e.message}")
            "문제 생성 실패"
        }
    }
}
