package com.academy.admin.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class BingNewsService(
    private val webClient: WebClient.Builder
) {
    private val objectMapper = ObjectMapper()
    private val bingApiKey = "YOUR_BING_API_KEY" // 실제 API 키로 교체 필요
    private val bingNewsUrl = "https://api.bing.microsoft.com/v7.0/news/search"
    
    data class NewsItem(
        val name: String,
        val url: String,
        val description: String,
        val datePublished: String,
        val provider: List<Provider>
    )
    
    data class Provider(
        val name: String
    )
    
    data class BingNewsResponse(
        val value: List<NewsItem>
    )
    
    data class Reason(
        val id: String,
        val text: String,
        val sourceUrl: String?
    )
    
    // 문제에 대한 찬성 근거 뉴스 수집
    fun getProsReasons(question: String, ticker: String? = null): List<Reason> {
        val keywords = extractKeywords(question, ticker)
        val searchQuery = keywords.joinToString(" ") + " 긍정적 상승 호재"
        
        return searchNews(searchQuery).take(3).mapIndexed { index, news ->
            Reason(
                id = "p${index + 1}",
                text = news.description,
                sourceUrl = news.url
            )
        }
    }
    
    // 문제에 대한 반대 근거 뉴스 수집
    fun getConsReasons(question: String, ticker: String? = null): List<Reason> {
        val keywords = extractKeywords(question, ticker)
        val searchQuery = keywords.joinToString(" ") + " 부정적 하락 악재"
        
        return searchNews(searchQuery).take(3).mapIndexed { index, news ->
            Reason(
                id = "c${index + 1}",
                text = news.description,
                sourceUrl = news.url
            )
        }
    }
    
    private fun extractKeywords(question: String, ticker: String?): List<String> {
        val keywords = mutableListOf<String>()
        
        // 티커가 있으면 추가
        ticker?.let { keywords.add(it) }
        
        // 문제에서 키워드 추출
        val questionLower = question.lowercase()
        
        // 통화 쌍 추출
        val currencyPairs = listOf("usd/jpy", "eur/usd", "gbp/usd", "usd/krw", "eur/krw")
        currencyPairs.forEach { pair ->
            if (questionLower.contains(pair)) {
                keywords.add(pair)
            }
        }
        
        // 지수 추출
        val indices = listOf("s&p500", "nasdaq", "dow", "kospi", "kosdaq", "nikkei")
        indices.forEach { index ->
            if (questionLower.contains(index)) {
                keywords.add(index)
            }
        }
        
        // 회사명 추출 (대문자로)
        val companies = listOf("apple", "microsoft", "google", "amazon", "tesla", "nvidia", "meta")
        companies.forEach { company ->
            if (questionLower.contains(company)) {
                keywords.add(company)
            }
        }
        
        // 기본 키워드 추가
        if (keywords.isEmpty()) {
            keywords.addAll(listOf("주식", "시장", "경제"))
        }
        
        return keywords
    }
    
    private fun searchNews(query: String): List<NewsItem> {
        return try {
            val response = webClient.build()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(bingNewsUrl)
                        .queryParam("q", query)
                        .queryParam("count", 10)
                        .queryParam("mkt", "ko-KR")
                        .queryParam("sortBy", "Date")
                        .build()
                }
                .header("Ocp-Apim-Subscription-Key", bingApiKey)
                .retrieve()
                .bodyToMono(BingNewsResponse::class.java)
                .block()
            
            response?.value ?: emptyList()
        } catch (e: Exception) {
            println("Bing News API 호출 실패: ${e.message}")
            emptyList()
        }
    }
    
    // Mock 데이터 반환 (API 키가 없을 때)
    fun getMockProsReasons(question: String, ticker: String? = null): List<Reason> {
        return listOf(
            Reason("p1", "긍정적인 시장 전망과 강한 실적 발표", "https://example.com/news1"),
            Reason("p2", "기관 투자자들의 매수세 증가", "https://example.com/news2"),
            Reason("p3", "경제 지표 개선과 성장 기대감", "https://example.com/news3")
        )
    }
    
    fun getMockConsReasons(question: String, ticker: String? = null): List<Reason> {
        return listOf(
            Reason("c1", "글로벌 경제 불확실성과 리스크 증가", "https://example.com/news4"),
            Reason("c2", "인플레이션 우려와 금리 상승 압박", "https://example.com/news5"),
            Reason("c3", "기술주 조정과 수익 실현 매물", "https://example.com/news6")
        )
    }
}
