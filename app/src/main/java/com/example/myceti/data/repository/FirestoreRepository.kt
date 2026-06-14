package com.example.myceti.data.repository

import com.example.myceti.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

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

    suspend fun guardarCodigoBarras(codigo: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
        return try {
            db.collection("usuarios").document(uid)
                .update("codigoBarras", codigo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subirFotoPerfil(imageBytes: ByteArray): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
        return try {
            // Convertir bytes a Base64
            val base64String = android.util.Base64.encodeToString(
                imageBytes,
                android.util.Base64.NO_WRAP
            )

            // Guardar en Firestore (campo fotoBase64)
            db.collection("usuarios").document(uid)
                .update("fotoBase64", base64String)
                .await()

            Result.success(base64String)
        } catch (e: Exception) {
            android.util.Log.e("STORAGE", "Error en subirFotoPerfil: ${e.message}")
            Result.failure(e)
        }
    }

    // ── Apuntes ───────────────────────────────────────────────────────────

    // ── Apuntes (Migrados a Base64) ───────────────────────────────────────

    private fun comprimirImagen(imageBytes: ByteArray): ByteArray {

        val bitmap = BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size
        )

        var ancho = bitmap.width
        var alto = bitmap.height

        // Reducir tamaño máximo a 800px
        if (ancho > 800 || alto > 800) {

            val ratio = ancho.toFloat() / alto.toFloat()

            if (ratio > 1) {
                ancho = 800
                alto = (800 / ratio).toInt()
            } else {
                alto = 800
                ancho = (800 * ratio).toInt()
            }
        }

        val bitmapReducido = Bitmap.createScaledBitmap(
            bitmap,
            ancho,
            alto,
            true
        )

        val baos = ByteArrayOutputStream()

        bitmapReducido.compress(
            Bitmap.CompressFormat.JPEG,
            25,
            baos
        )

        return baos.toByteArray()
    }


    suspend fun subirApunte(
        imageBytes: ByteArray,
        materia: String
    ): Result<Apunte> {

        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("No autenticado"))

        return try {

            // Comprimir imagen
            val imagenComprimida =
                comprimirImagen(imageBytes)

            // Convertir a Base64
            val base64String =
                Base64.encodeToString(
                    imagenComprimida,
                    Base64.NO_WRAP
                )

            // Verificar tamaño
            if (base64String.length > 1000000) {

                return Result.failure(
                    Exception(
                        "La imagen sigue siendo demasiado grande"
                    )
                )
            }

            val apunte = Apunte(
                materia = materia,
                imageBase64 = base64String,
                uid = uid,
                fecha = com.google.firebase.Timestamp.now()
            )

            val docRef = db.collection("apuntes")
                .add(apunte)
                .await()

            Result.success(
                apunte.copy(id = docRef.id)
            )

        } catch (e: Exception) {

            android.util.Log.e(
                "APUNTES",
                "Error al subir apunte: ${e.message}"
            )

            Result.failure(e)
        }
    }

    suspend fun getApuntes(): Result<List<Apunte>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No autenticado"))
        return try {
            val snap = db.collection("apuntes")
                .whereEqualTo("uid", uid)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()

            val lista = snap.documents.mapNotNull { doc ->
                doc.toObject(Apunte::class.java)?.copy(id = doc.id)
            }
            Result.success(lista)
        } catch (e: Exception) {
            android.util.Log.e("APUNTES", "Error al obtener apuntes: ${e.message}")
            Result.failure(e)
        }
    }
    suspend fun eliminarApunte(id: String): Result<Unit> {
        return try {
            db.collection("apuntes").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Comunicados ───────────────────────────────────────────────────────

    suspend fun getComunicados(): Result<List<Comunicado>> {
        return try {
            val snap = db.collection("comunicados")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()
            val lista = snap.documents.mapNotNull { it.toObject(Comunicado::class.java)?.copy(id = it.id) }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}



