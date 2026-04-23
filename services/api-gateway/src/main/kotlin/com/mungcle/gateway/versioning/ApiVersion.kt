package com.mungcle.gateway.versioning

import java.time.LocalDate

enum class ApiVersion(val date: LocalDate) {
    V1(LocalDate.of(2025, 1, 1));

    companion object {
        val LATEST: ApiVersion = entries.last()

        fun fromDate(dateStr: String): ApiVersion {
            return try {
                val requested = LocalDate.parse(dateStr)
                entries
                    .filter { !it.date.isAfter(requested) }
                    .maxByOrNull { it.date }
                    ?: LATEST
            } catch (e: Exception) {
                LATEST
            }
        }
    }
}
