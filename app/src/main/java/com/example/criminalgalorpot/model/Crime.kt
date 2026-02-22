package com.example.criminalgalorpot.model

import java.util.Date
import java.util.UUID

// Basic data model for an "office crime"
data class Crime(
    val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String? = null,
    var photoPath: String? = null
)

// Simple in-memory repository for now
object CrimeRepository {
    private val crimes: MutableList<Crime> = MutableList(5) { index ->
        Crime(
            title = "Crime #$index",
            isSolved = index % 2 == 0
        )
    }

    fun getCrimes(): List<Crime> = crimes

    fun getCrime(id: UUID): Crime? = crimes.find { it.id == id }

    fun addCrime(crime: Crime) {
        crimes.add(crime)
    }
}
