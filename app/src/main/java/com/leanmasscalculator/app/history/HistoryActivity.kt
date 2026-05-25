package com.leanmasscalculator.app.history

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.leanmasscalculator.app.R
import com.leanmasscalculator.app.databinding.ActivityHistoryBinding
import com.leanmasscalculator.app.db.DatabaseHelper
import com.leanmasscalculator.app.db.FirestoreHelper
import com.leanmasscalculator.app.model.Calculation

/**
 * History screen — uses VIEWBINDING (modern approach).
 * Displays all past calculations with delete support.
 * Loads from SQLite by default, can refresh from Firestore.
 */
class HistoryActivity : AppCompatActivity() {

    // =============================================
    // WITH ViewBinding
    // =============================================
    private lateinit var binding: ActivityHistoryBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var localDb: DatabaseHelper
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var adapter: HistoryAdapter
    private var calculationList = mutableListOf<Calculation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- ViewBinding ---
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Initialize ---
        mAuth           = FirebaseAuth.getInstance()
        localDb         = DatabaseHelper(this)
        firestoreHelper = FirestoreHelper()

        // --- Setup RecyclerView ---
        // The adapter needs: the data list + what to do on delete
        adapter = HistoryAdapter(calculationList) { calc, position ->
            onItemDelete(calc, position)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        // --- Back button ---
        binding.btnBack.setOnClickListener {
            finish()   // just close this screen, go back to calculator
        }

        // --- Refresh from Cloud button ---
        binding.btnRefreshCloud.setOnClickListener {
            loadFromFirestore()
        }

        // --- Load local data first (instant, works offline) ---
        loadFromSQLite()
    }

    override fun onResume() {
        super.onResume()
        loadFromSQLite()   // refresh list when returning from calculator
    }

    /**
     * Load from SQLite — instant, always works.
     */
    private fun loadFromSQLite() {
        val userId = mAuth.currentUser?.uid ?: return
        calculationList.clear()
        calculationList.addAll(localDb.getCalculationsForUser(userId))
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    /**
     * Load from Firestore — needs internet, but syncs across devices.
     */
    private fun loadFromFirestore() {
        val userId = mAuth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        firestoreHelper.getCalculationsForUser(userId,
            onSuccess = { calculations ->
                binding.progressBar.visibility = View.GONE
                calculationList.clear()
                calculationList.addAll(calculations)
                adapter.notifyDataSetChanged()
                updateEmptyState()
                Toast.makeText(
                    this,
                    "${calculations.size} calculs charges depuis le cloud",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onFailure = { error ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Erreur: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    /**
     * Show empty state or the list, never both.
     */
    private fun updateEmptyState() {
        if (calculationList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvHistory.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvHistory.visibility = View.VISIBLE
        }
    }

    /**
     * Delete a calculation with confirmation dialog.
     * Deletes from BOTH SQLite and Firestore.
     */
    private fun onItemDelete(calc: Calculation, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer")
            .setMessage("Supprimer ce calcul ?")
            .setPositiveButton("Oui") { _, _ ->
                // --- Delete from SQLite ---
                localDb.deleteCalculation(calc.id)

                // --- Delete from Firestore ---
                // Firestore IDs are long strings like "abc123xyz456"
                // SQLite IDs are numbers like "1", "2", "3"
                // If the ID is long, it's probably a Firestore ID
                if (calc.id.length > 5) {
                    firestoreHelper.deleteCalculation(calc.id,
                        onSuccess = { /* cloud delete worked */ },
                        onFailure = { error ->
                            Toast.makeText(
                                this,
                                "Erreur cloud: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                // --- Remove from list + animate ---
                adapter.removeItem(position)
                updateEmptyState()
                Toast.makeText(this, "Calcul supprime", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Non", null)
            .show()
    }
}