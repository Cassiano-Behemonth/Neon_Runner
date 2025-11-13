package com.example.geometric_neon_runner.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot


data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val bestScores: Map<String, Int> = emptyMap()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "createdAt" to createdAt,
            "bestScores" to bestScores
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): User {
            val data = doc.data ?: return User()
            return User(
                uid = data["uid"] as? String ?: doc.id,
                username = data["username"] as? String ?: "",
                email = data["email"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                bestScores = (data["bestScores"] as? Map<String, Number>)?.mapValues { it.value.toInt() } ?: emptyMap()
            )
        }
    }
}

@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val username: String = "",
    val score: Int = 0,
    val timeSeconds: Int = 0,
    val mode: String = GameMode.NORMAL.name,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "username" to username,
            "score" to score,
            "timeSeconds" to timeSeconds,
            "mode" to mode,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>, localId: Int = 0, synced: Boolean = true): Score {
            val userId = map["userId"] as? String ?: ""
            val username = map["username"] as? String ?: ""
            val score = (map["score"] as? Number)?.toInt() ?: 0
            val timeSeconds = (map["timeSeconds"] as? Number)?.toInt() ?: 0
            val mode = map["mode"] as? String ?: GameMode.NORMAL.name
            val timestamp = when (val t = map["timestamp"]) {
                is Number -> t.toLong()
                is Long -> t
                else -> System.currentTimeMillis()
            }
            return Score(localId, userId, username, score, timeSeconds, mode, timestamp, synced)
        }

        fun fromDocument(doc: DocumentSnapshot): Score {
            return fromMap(doc.data ?: emptyMap(), localId = 0, synced = true)
        }
    }
}

enum class GameMode(val displayName: String, val baseSpeed: Float, val spawnInterval: Long, val color: String) {
    NORMAL("Normal", 1.0f, 800L, "#00FFAA"),
    HARD("Hard", 1.5f, 600L, "#FFAA00"),
    EXTREME("Extreme", 2.0f, 400L, "#FF0033");

    companion object {
        fun fromName(name: String): GameMode {
            return values().find { it.name == name } ?: NORMAL
        }
    }
}