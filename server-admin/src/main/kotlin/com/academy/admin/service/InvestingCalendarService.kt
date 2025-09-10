package com.academy.admin.service

import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

@Service
class InvestingCalendarService {
    private val logger = LoggerFactory.getLogger(InvestingCalendarService::class.java)
    
    data class CalendarEvent(
        val time: String,
        val currency: String,
        val event: String,
        val actual: String?,
        val forecast: String?,
        val previous: String?,
        val impact: String
    )
    
    data class EarningsEvent(
        val time: String,
        val company: String,
        val symbol: String,
        val market: String,
        val epsForecast: String?,
        val revenueForecast: String?
    )
    
    // 경제 캘린더 데이터 크롤링
    fun getEconomicCalendar(date: LocalDate = LocalDate.now()): List<CalendarEvent> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateStr = date.format(formatter)
        
        val url = "https://kr.investing.com/economic-calendar/?date=$dateStr"
        
        return try {
            logger.info("경제 캘린더 URL: $url")
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            logger.info("HTML 문서 크기: ${doc.html().length} bytes")
            val events = mutableListOf<CalendarEvent>()
            
            // 실제 HTML 구조에 맞게 파싱
            val rows = doc.select("tr[data-event-datetime]")
            logger.info("발견된 이벤트 행 수: ${rows.size}")
            
            rows.forEach { row ->
                val cells = row.select("td")
                if (cells.size >= 6) {
                    val time = cells[0].text().trim()
                    val currency = cells[1].text().trim()
                    val event = cells[2].text().trim()
                    val actual = cells[3].text().trim().takeIf { it.isNotEmpty() && it != "-" }
                    val forecast = cells[4].text().trim().takeIf { it.isNotEmpty() && it != "-" }
                    val previous = cells[5].text().trim().takeIf { it.isNotEmpty() && it != "-" }
                    
                    // Impact는 보통 아이콘 클래스나 색상으로 구분됨
                    val impact = when {
                        cells[6].select("i[title*='High']").isNotEmpty() -> "High"
                        cells[6].select("i[title*='Medium']").isNotEmpty() -> "Medium"
                        cells[6].select("i[title*='Low']").isNotEmpty() -> "Low"
                        else -> "Unknown"
                    }
                    
                    if (event.isNotEmpty()) {
                        events.add(CalendarEvent(time, currency, event, actual, forecast, previous, impact))
                    }
                }
            }
            
            events
        } catch (e: Exception) {
            logger.error("경제 캘린더 크롤링 실패: ${e.message}", e)
            emptyList()
        }
    }
    
    // 실적 캘린더 데이터 크롤링
    fun getEarningsCalendar(date: LocalDate = LocalDate.now()): List<EarningsEvent> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateStr = date.format(formatter)
        
        val url = "https://kr.investing.com/earnings-calendar/?date=$dateStr"
        
        return try {
            logger.info("실적 캘린더 URL: $url")
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            logger.info("실적 HTML 문서 크기: ${doc.html().length} bytes")
            val events = mutableListOf<EarningsEvent>()
            
            // 실제 HTML 구조에 맞게 파싱
            val rows = doc.select("tr[data-event-datetime]")
            logger.info("발견된 실적 이벤트 행 수: ${rows.size}")
            
            rows.forEach { row ->
                val cells = row.select("td")
                if (cells.size >= 5) {
                    val time = cells[0].text().trim()
                    val company = cells[1].text().trim()
                    val symbol = cells[2].text().trim()
                    val market = cells[3].text().trim()
                    val epsForecast = cells[4].text().trim().takeIf { it.isNotEmpty() && it != "-" }
                    val revenueForecast = if (cells.size > 5) cells[5].text().trim().takeIf { it.isNotEmpty() && it != "-" } else null
                    
                    if (company.isNotEmpty() && symbol.isNotEmpty()) {
                        events.add(EarningsEvent(time, company, symbol, market, epsForecast, revenueForecast))
                    }
                }
            }
            
            events
        } catch (e: Exception) {
            logger.error("실적 캘린더 크롤링 실패: ${e.message}", e)
            emptyList()
        }
    }
}
