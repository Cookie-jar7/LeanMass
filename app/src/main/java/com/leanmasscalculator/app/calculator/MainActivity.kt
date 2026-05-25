package com.leanmasscalculator.app.calculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.leanmasscalculator.app.R
import com.leanmasscalculator.app.auth.LoginActivity
import com.leanmasscalculator.app.calculator.MainActivity
import com.leanmasscalculator.app.databinding.ActivityMainBinding
import com.leanmasscalculator.app.db.DatabaseHelper
import com.leanmasscalculator.app.db.FirestoreHelper
import com.leanmasscalculator.app.history.HistoryActivity
//import com.leanmasscalculator.app.history.HistoryActivity
import com.leanmasscalculator.app.model.Calculation
import com.leanmasscalculator.app.utils.LBMConfig


//Calculator screen — uses VIEWBINDING (modern approach).

class MainActivity : AppCompatActivity() {

    // =============================================
    // WITH ViewBinding
    // =============================================
    private lateinit var binding: ActivityMainBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var localDb: DatabaseHelper
    private lateinit var firestoreHelper: FirestoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Initialize ---
        mAuth   = FirebaseAuth.getInstance()
        localDb = DatabaseHelper(this)
        firestoreHelper = FirestoreHelper()
        // --- Check authentication ---
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // --- Welcome message ---
        binding.tvWelcome.text = "Bienvenue, ${currentUser.email}"

        // --- Calculate button ---
        binding.btnCalculate.setOnClickListener {
            calculateLBM()
        }

        // --- History button ---
        binding.btnHistory.setOnClickListener {
          startActivity(Intent(this, HistoryActivity::class.java))
        }

        // --- Logout button ---
        binding.btnLogout.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun calculateLBM() {
        // --- Read inputs ---
        val weightStr = binding.etWeight.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()
        val genderId  = binding.rgGender.checkedRadioButtonId

        // --- Validation ---
        if (weightStr.isEmpty()) {
            binding.etWeight.error = "Poids requis"
            binding.etWeight.requestFocus()
            return
        }
        if (heightStr.isEmpty()) {
            binding.etHeight.error = "Taille requise"
            binding.etHeight.requestFocus()
            return
        }
        if (genderId == -1) {
            Toast.makeText(this, "Veuillez selectionner le sexe", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDouble()
        val height = heightStr.toDouble()
        val gender = if (genderId == R.id.rb_male)
            LBMConfig.GENDER_MALE
        else
            LBMConfig.GENDER_FEMALE

        // --- Calculate using our config file ---
        val lbmValue      = LBMConfig.calculateLBM(weight, height, gender)
        val satisfactory  = LBMConfig.isSatisfactory(lbmValue, gender)

        // --- Display result ---
        binding.tvLbmResult.text = String.format("LBM: %.2f kg", lbmValue)
        binding.tvLbmResult.visibility = View.VISIBLE

        if (satisfactory) {
            binding.ivResultIcon.setImageResource(R.drawable.ic_check_circle)
            binding.ivResultIcon.visibility = View.VISIBLE
            binding.tvResultLabel.text = "Resultat satisfaisant"
            binding.tvResultLabel.setTextColor(
                ContextCompat.getColor(this, R.color.green_satisfactory)
            )
            binding.tvResultLabel.visibility = View.VISIBLE
        } else {
            binding.ivResultIcon.setImageResource(R.drawable.ic_warning)
            binding.ivResultIcon.visibility = View.VISIBLE
            binding.tvResultLabel.text = "Resultat a surveiller"
            binding.tvResultLabel.setTextColor(
                ContextCompat.getColor(this, R.color.orange_warning)
            )
            binding.tvResultLabel.visibility = View.VISIBLE
        }

        binding.cardResult.visibility = View.VISIBLE

        // --- Save to SQLite (local) ---
        val userId = mAuth.currentUser!!.uid
        val calculation = Calculation(
            userId      = userId,
            weight      = weight,
            height      = height,
            gender      = gender,
            lbmValue    = lbmValue,
            satisfactory = satisfactory,
            timestamp   = System.currentTimeMillis()
        )

        val rowId = localDb.insertCalculation(calculation)
        if (rowId == -1L) {
            Toast.makeText(this, "Erreur sauvegarde locale", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Calcul sauvegarde!", Toast.LENGTH_SHORT).show()
        }

        // --- Save to Firestore (cloud) ---
        firestoreHelper.saveCalculation(calculation,
            onSuccess = { id ->
                // Cloud save worked — no need to tell user, local already confirmed
            },
            onFailure = { error ->
                Toast.makeText(this, "Erreur cloud: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
}