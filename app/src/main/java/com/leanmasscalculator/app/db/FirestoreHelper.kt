package com.leanmasscalculator.app.db

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.leanmasscalculator.app.model.Calculation


// Helper class for Cloud Firestore operations.

class FirestoreHelper {

    companion object {
        private const val COLLECTION_CALCULATIONS = "calculations"
    }

    private val db = FirebaseFirestore.getInstance()

    //save calculation
    fun saveCalculation(
        calculation: Calculation,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val data = hashMapOf(
            "userId"      to calculation.userId,
            "weight"      to calculation.weight,
            "height"      to calculation.height,
            "gender"      to calculation.gender,
            "lbmValue"    to calculation.lbmValue,
            "satisfactory" to calculation.satisfactory,
            "timestamp"   to calculation.timestamp
        )

        db.collection(COLLECTION_CALCULATIONS)
            .add(data)                            // auto-generates a document ID
            .addOnSuccessListener { documentRef ->
                calculation.id = documentRef.id   // save the Firestore ID
                onSuccess(documentRef.id)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Erreur inconnue")
            }
    }

   //getcalculations
    fun getCalculationsForUser(
        userId: String,
        onSuccess: (List<Calculation>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection(COLLECTION_CALCULATIONS)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = mutableListOf<Calculation>()
                for (doc in querySnapshot) {
                    val calc = Calculation(
                        id           = doc.id,
                        userId       = doc.getString("userId") ?: "",
                        weight       = doc.getDouble("weight") ?: 0.0,
                        height       = doc.getDouble("height") ?: 0.0,
                        gender       = doc.getString("gender") ?: "",
                        lbmValue     = doc.getDouble("lbmValue") ?: 0.0,
                        satisfactory = doc.getBoolean("satisfactory") ?: false,
                        timestamp    = doc.getLong("timestamp") ?: 0L
                    )
                    list.add(calc)
                }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Erreur inconnue")
            }
    }

    //delete calc
    fun deleteCalculation(
        calculationId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection(COLLECTION_CALCULATIONS)
            .document(calculationId)
            .delete()
            .addOnSuccessListener {
                onSuccess(calculationId)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Erreur inconnue")
            }
    }
}