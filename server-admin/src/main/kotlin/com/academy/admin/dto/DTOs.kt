package com.academy.admin.dto

import java.time.*

data class QuestionItem(
    val id: Long,
    val prompt: String,
    val pros: List<Reason> = emptyList(),
    val cons: List<Reason> = emptyList(),
    val closesAt: LocalDateTime
)

data class Reason(val id: String, val text: String, val sourceUrl: String? = null)

data class PredictionRequest(
    val questionId: Long,
    val choice: String,
    val pickedProsIds: List<String>? = null,
    val pickedConsIds: List<String>? = null
)
