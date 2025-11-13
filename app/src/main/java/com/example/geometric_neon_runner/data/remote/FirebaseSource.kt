package com.example.geometric_neon_runner.data.remote


import com.example.geometric_neon_runner.data.model.*
import com.example.geometric_neon_runner.utils.Result
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class FirebaseAuthSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestoreSource: FirestoreSource
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = currentUser != null

    suspend fun register(email: String, password: String, username: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val task = auth.createUserWithEmailAndPassword(email, password)
                Tasks.await(task)
                if (!task.isSuccessful) throw task.exception ?: Exception("Unknown error creating user")

                val firebaseUser = auth.currentUser ?: throw Exception("User null after create")
                val user = User(
                    uid = firebaseUser.uid,
                    username = username,
                    email = email,
                    createdAt = System.currentTimeMillis(),
                    bestScores = emptyMap()
                )

                val saveResult = firestoreSource.saveUser(user)
                when (saveResult) {
                    is Result.Success -> Result.Success(user)
                    is Result.Error -> Result.Error(saveResult.message, saveResult.exception)
                    else -> Result.Error("Unknown Firestore result")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val task = auth.signInWithEmailAndPassword(email, password)
                Tasks.await(task)
                if (!task.isSuccessful) throw task.exception ?: Exception("Login failed")
                val firebaseUser = auth.currentUser ?: throw Exception("User null after login")
                val userResult = firestoreSource.getUser(firebaseUser.uid)
                when (userResult) {
                    is Result.Success -> Result.Success(userResult.data)
                    is Result.Error -> Result.Error(userResult.message, userResult.exception)
                    else -> Result.Error("Unknown Firestore getUser result")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val t = auth.sendPasswordResetEmail(email)
                Tasks.await(t)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }
}

class FirestoreSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_SCORES = "scores"
    }

    private val usersRef = firestore.collection(COLLECTION_USERS)
    private val scoresRef = firestore.collection(COLLECTION_SCORES)

    suspend fun saveUser(user: User): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val task = usersRef.document(user.uid).set(user.toMap())
                Tasks.await(task)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun getUser(uid: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val task = usersRef.document(uid).get()
                val doc = Tasks.await(task)
                if (!doc.exists()) throw Exception("User not found")
                Result.Success(User.fromDocument(doc))
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun updateBestScore(uid: String, mode: String, score: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val txResult = firestore.runTransaction { transaction ->
                    val docRef = usersRef.document(uid)
                    val snapshot = transaction.get(docRef)
                    val currentBestMap = (snapshot.get("bestScores") as? Map<String, Long>)?.mapValues { it.value.toInt() } ?: emptyMap()
                    val currentBest = currentBestMap[mode] ?: 0
                    if (score > currentBest) {
                        val newMap = currentBestMap.toMutableMap()
                        newMap[mode] = score
                        transaction.update(docRef, "bestScores", newMap)
                    }
                    null
                }
                Tasks.await(txResult)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun saveScore(score: Score): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val task = scoresRef.add(score.toMap())
                val docRef = Tasks.await(task)
                Result.Success(docRef.id)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun getGlobalRanking(mode: String, limit: Int = 50): Result<List<Score>> {
        return withContext(Dispatchers.IO) {
            try {
                val q = scoresRef
                    .whereEqualTo("mode", mode)
                    .orderBy("score", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                val task = q.get()
                val snap = Tasks.await(task)
                val list = snap.documents.map { doc ->
                    Score.fromMap(doc.data ?: emptyMap())
                }
                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun getUserScores(uid: String): Result<List<Score>> {
        return withContext(Dispatchers.IO) {
            try {
                val q = scoresRef.whereEqualTo("userId", uid).orderBy("timestamp", Query.Direction.DESCENDING)
                val task = q.get()
                val snap = Tasks.await(task)
                val list = snap.documents.map { doc -> Score.fromMap(doc.data ?: emptyMap()) }
                Result.Success(list)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    suspend fun getUserRank(uid: String, mode: String, score: Int): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val q = scoresRef
                    .whereEqualTo("mode", mode)
                    .whereGreaterThan("score", score)

                val task = q.get()
                val snap = Tasks.await(task)
                val countHigher = snap.size()
                Result.Success(countHigher + 1)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }
}