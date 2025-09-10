package com.academy.admin.service

import com.academy.admin.domain.*
import com.academy.admin.repo.CandidateRepo
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

@Service
class QuestionCandidateBatchService(
    private val investingCalendarService: InvestingCalendarService,
    private val geminiAIService: GeminiAIService,
    private val bingNewsService: BingNewsService,
    private val candidateRepo: CandidateRepo
) {
    private val objectMapper = ObjectMapper()
    private val logger = LoggerFactory.getLogger(QuestionCandidateBatchService::class.java)
    
    // 매일 오전 6시에 실행 (문제 후보 생성) - 임시 비활성화
    // @Scheduled(cron = "0 0 6 * * *")
    fun generateQuestionCandidates() {
        logger.info("=== 문제 후보 생성 배치 시작: ${LocalDateTime.now()} ===")
        
        val today = LocalDate.now()
        val candidates = mutableListOf<QuestionCandidate>()
        
        try {
            // 1. 경제 캘린더에서 이벤트 수집
            logger.info("1. 경제 캘린더 이벤트 수집 시작...")
            val economicEvents = investingCalendarService.getEconomicCalendar(today)
            logger.info("경제 이벤트 수집 완료: ${economicEvents.size}개")
            if (economicEvents.isEmpty()) {
                logger.warn("  → 경제 이벤트가 없습니다. Investing.com 파싱 결과를 확인하세요.")
            } else {
                economicEvents.forEach { event ->
                    logger.info("  - 경제 이벤트: ${event.event} (${event.impact})")
                }
            }
            
            // 2. 실적 캘린더에서 이벤트 수집
            logger.info("2. 실적 캘린더 이벤트 수집 시작...")
            val earningsEvents = investingCalendarService.getEarningsCalendar(today)
            logger.info("실적 이벤트 수집 완료: ${earningsEvents.size}개")
            if (earningsEvents.isEmpty()) {
                logger.warn("  → 실적 이벤트가 없습니다. Investing.com 파싱 결과를 확인하세요.")
            } else {
                earningsEvents.forEach { event ->
                    logger.info("  - 실적 이벤트: ${event.symbol} (${event.company})")
                }
            }
            
            // 3. 경제 이벤트 기반 문제 생성 (최대 2개)
            logger.info("3. 경제 이벤트 기반 문제 생성 시작...")
            economicEvents.take(2).forEach { event ->
                logger.info("  - 처리 중인 경제 이벤트: ${event.event} (${event.impact})")
                if (event.impact == "High" || event.impact == "Medium") {
                    logger.info("    → AI 문제 생성 요청...")
                    val aiResponse = geminiAIService.generateEconomicQuestion(event)
                    logger.info("    → AI 응답: $aiResponse")
                    if (aiResponse != "문제 생성 실패") {
                        val parsedResponse = parseAIResponse(aiResponse)
                        val prompt = parsedResponse["prompt"] as? String ?: aiResponse
                        val pros = parsedResponse["pros"] as? String ?: "[]"
                        val cons = parsedResponse["cons"] as? String ?: "[]"
                        val importance = parsedResponse["importance"] as? String ?: ""
                        val impact = parsedResponse["impact"] as? String ?: ""
                        
                        val ticker = extractTickerFromQuestion(prompt)
                        logger.info("    → 추출된 티커: $ticker")
                        
                        val candidate = QuestionCandidate(
                            candidate_date = today,
                            type = QuestionType.MACRO,
                            ticker = ticker,
                            prompt = prompt,
                            pros = pros,
                            cons = cons,
                            importance = importance,
                            impact = impact,
                            status = CandidateStatus.CANDIDATE
                        )
                        candidates.add(candidate)
                        logger.info("    → 후보 추가 완료: ${candidate.prompt}")
                    } else {
                        logger.warn("    → 문제 생성 실패")
                    }
                } else {
                    logger.info("    → 영향도가 낮아 건너뜀")
                }
            }
            
            // 4. 실적 이벤트 기반 문제 생성 (최대 2개)
            logger.info("4. 실적 이벤트 기반 문제 생성 시작...")
            earningsEvents.take(2).forEach { event ->
                logger.info("  - 처리 중인 실적 이벤트: ${event.symbol} (${event.company})")
                logger.info("    → AI 문제 생성 요청...")
                val aiResponse = geminiAIService.generateEarningsQuestion(event)
                logger.info("    → AI 응답: $aiResponse")
                if (aiResponse != "문제 생성 실패") {
                    val parsedResponse = parseAIResponse(aiResponse)
                    val prompt = parsedResponse["prompt"] as? String ?: aiResponse
                    val pros = parsedResponse["pros"] as? String ?: "[]"
                    val cons = parsedResponse["cons"] as? String ?: "[]"
                    val importance = parsedResponse["importance"] as? String ?: ""
                    val impact = parsedResponse["impact"] as? String ?: ""
                    
                    val ticker = event.symbol
                    logger.info("    → 티커: $ticker")
                    
                    val candidate = QuestionCandidate(
                        candidate_date = today,
                        type = QuestionType.EARNINGS,
                        ticker = ticker,
                        prompt = prompt,
                        pros = pros,
                        cons = cons,
                        importance = importance,
                        impact = impact,
                        status = CandidateStatus.CANDIDATE
                    )
                    candidates.add(candidate)
                    logger.info("    → 후보 추가 완료: ${candidate.prompt}")
                } else {
                    logger.warn("    → 문제 생성 실패")
                }
            }
            
            // 5. 일반 시황 문제 생성 (1개)
            logger.info("5. 일반 시황 문제 생성 시작...")
            logger.info("    → AI 시황 문제 생성 요청...")
            val aiResponse = geminiAIService.generateMarketQuestion()
            logger.info("    → AI 응답: $aiResponse")
            if (aiResponse != "문제 생성 실패") {
                val parsedResponse = parseAIResponse(aiResponse)
                val prompt = parsedResponse["prompt"] as? String ?: aiResponse
                val pros = parsedResponse["pros"] as? String ?: "[]"
                val cons = parsedResponse["cons"] as? String ?: "[]"
                val importance = parsedResponse["importance"] as? String ?: ""
                val impact = parsedResponse["impact"] as? String ?: ""
                
                val candidate = QuestionCandidate(
                    candidate_date = today,
                    type = QuestionType.INDEX,
                    ticker = null,
                    prompt = prompt,
                    pros = pros,
                    cons = cons,
                    importance = importance,
                    impact = impact,
                    status = CandidateStatus.CANDIDATE
                )
                candidates.add(candidate)
                logger.info("    → 후보 추가 완료: ${candidate.prompt}")
            } else {
                logger.warn("    → 시황 문제 생성 실패")
            }
            
            // 6. 데이터베이스에 저장
            logger.info("6. 데이터베이스 저장 시작...")
            candidates.forEach { candidate ->
                try {
                    candidateRepo.save(candidate)
                    logger.info("    → 문제 후보 저장 완료: ${candidate.prompt}")
                } catch (e: Exception) {
                    logger.error("    → 저장 실패: ${candidate.prompt} - ${e.message}", e)
                }
            }
            
            logger.info("=== 문제 후보 생성 배치 완료: ${candidates.size}개 생성 ===")
            
        } catch (e: Exception) {
            logger.error("문제 후보 생성 배치 실패: ${e.message}", e)
        }
    }
    
    private fun parseAIResponse(aiResponse: String): Map<String, Any> {
        return try {
            // JSON 블록을 찾아서 추출
            val jsonPattern = """```json\s*(\{.*?\})\s*```""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonPattern.find(aiResponse)
            
            val jsonString = if (jsonMatch != null) {
                jsonMatch.groupValues[1]
            } else {
                // JSON 블록이 없으면 전체 응답을 JSON으로 파싱 시도
                aiResponse
            }
            
            val responseMap = objectMapper.readValue(jsonString, Map::class.java) as Map<String, Any>
            responseMap
        } catch (e: Exception) {
            logger.warn("AI 응답 파싱 실패: ${e.message}")
            logger.warn("AI 응답 내용: $aiResponse")
            
            // 파싱 실패 시 기본값 반환
            mapOf(
                "prompt" to "AI 응답 파싱 실패",
                "pros" to "[]",
                "cons" to "[]",
                "importance" to "AI 응답 파싱 실패",
                "impact" to "AI 응답 파싱 실패"
            )
        }
    }
    
    private fun extractTickerFromQuestion(question: String): String? {
        val questionLower = question.lowercase()
        
        // 통화 쌍 추출
        val currencyPairs = listOf("usd/jpy", "eur/usd", "gbp/usd", "usd/krw", "eur/krw")
        currencyPairs.forEach { pair ->
            if (questionLower.contains(pair)) {
                return pair.uppercase()
            }
        }
        
        // 지수 추출
        val indices = mapOf(
            "s&p500" to "SPX",
            "nasdaq" to "NASDAQ",
            "dow" to "DJI",
            "kospi" to "KOSPI",
            "kosdaq" to "KOSDAQ",
            "nikkei" to "NIKKEI"
        )
        
        indices.forEach { (key, value) ->
            if (questionLower.contains(key)) {
                return value
            }
        }
        
        // 회사명 추출
        val companies = mapOf(
            "apple" to "AAPL",
            "microsoft" to "MSFT",
            "google" to "GOOGL",
            "amazon" to "AMZN",
            "tesla" to "TSLA",
            "nvidia" to "NVDA",
            "meta" to "META"
        )
        
        companies.forEach { (key, value) ->
            if (questionLower.contains(key)) {
                return value
            }
        }
        
        return null
    }
}
