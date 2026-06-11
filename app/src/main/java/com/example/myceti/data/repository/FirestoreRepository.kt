package com.example.myceti.data.repository

import com.example.myceti.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ── Auth ──────────────────────────────────────────────────────────────

    suspend fun registrar(nombre: String, correo: String, password: String, noRegistro: String): Result<Usuario> {
        return try {
            val result = auth.createUserWithEmailAndPassword(correo, password).await()
            val uid = result.user!!.uid
            val usuario = Usuario(
                uid = uid,
                nombre = nombre,
                correo = correo,
                noRegistro = noRegistro,
                grupo = "",
                plantel = "Colomos"
            )
            db.collection("usuarios").document(uid).set(usuario).await()
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(correo: String, password: String): Result<Usuario> {
        return try {
            val result = auth.signInWithEmailAndPassword(correo, password).await()
            val uid = result.user!!.uid
            val snap = db.collection("usuarios").document(uid).get().await()
            val usuario = snap.toObject(Usuario::class.java)!!
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsuarioActual(): Usuario? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val snap = db.collection("usuarios").document(uid).get().await()
            snap.toObject(Usuario::class.java)
        } catch (e: Exception) { null }
    }

    fun logout() = auth.signOut()

// ── Horario ───────────────────────────────────────────────────────────

    /**
     * Firestore path: horarios/{grupo}/clases/{claseId}
     * El campo 'diasSemana' en Firestore se lee como List<Long>.
     */
    suspend fun getHorario(grupo: String): Result<List<Clase>> {
        return try {
            if (grupo.isBlank()) return Result.success(emptyList())

            val snap = db.collection("horarios")
                .document(grupo)
                .collection("clases")
                .get().await()

            android.util.Log.d("HORARIO", "Grupo=$grupo | Docs encontrados=${snap.documents.size}")

            val clases = snap.documents.mapNotNull { doc ->
                android.util.Log.d("HORARIO", "Raw doc ${doc.id}: ${doc.data}")
                try {
                    val dias = doc.get("diasSemana") as? List<Long> ?: emptyList()
                    android.util.Log.d("HORARIO", "  diasSemana=$dias (tipo=${doc.get("diasSemana")?.javaClass})")
                    Clase(
                        id        = doc.id,
                        materia   = doc.getString("materia")   ?: "",
                        maestro   = doc.getString("maestro")   ?: "",
                        salon     = doc.getString("salon")     ?: "",
                        edificio  = doc.getString("edificio")  ?: "",
                        horaInicio= doc.getString("horaInicio")?: "",
                        horaFin   = doc.getString("horaFin")   ?: "",
                        diasSemana= dias
                    )
                } catch (e: Exception) {
                    android.util.Log.e("HORARIO", "Error doc ${doc.id}: ${e.message}")
                    null
                }
            }

            android.util.Log.d("HORARIO", "Clases mapeadas: ${clases.size}")
            Result.success(clases)
        } catch (e: Exception) {
            android.util.Log.e("HORARIO", "Excepcion getHorario: ${e.message}")
            Result.failure(e)
        }
    }

}




