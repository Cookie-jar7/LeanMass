package com.leanmasscalculator.app.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.leanmasscalculator.app.R


//Registration screen —WITHOUT ViewBinding

class RegisterActivity : AppCompatActivity() {

    // ========================================
    // WITHOUT ViewBinding — using findViewById
    // ========================================
    private lateinit var etDisplayName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        //  Find views (NO ViewBinding)
        etDisplayName    = findViewById(R.id.et_display_name)
        etEmail          = findViewById(R.id.et_email)
        etPassword       = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnRegister      = findViewById(R.id.btn_register)
        progressBar      = findViewById(R.id.progress_bar)

        //Register button
        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val displayName     = etDisplayName.text.toString().trim()
        val email           = etEmail.text.toString().trim()
        val password        = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Input validation
        if (displayName.isEmpty()) {
            etDisplayName.error = "Nom requis"
            etDisplayName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email requis"
            etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Mot de passe requis"
            etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            etPassword.error = "6 caracteres minimum"
            etPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Les mots de passe ne correspondent pas"
            etConfirmPassword.requestFocus()
            return
        }

        // Show loading
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        // Step 1: Create account in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Account created! Now save user profile to Firestore
                    val firebaseUser = mAuth.currentUser
                    if (firebaseUser != null) {
                        saveUserToFirestore(firebaseUser.uid, email, displayName)
                    }
                } else {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    Toast.makeText(
                        this,
                        "Erreur: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }


     //Step 2: Save extra user info to Firestore Firebase Auth only stores email + password

    private fun saveUserToFirestore(uid: String, email: String, displayName: String) {
        val userMap = hashMapOf(
            "uid"         to uid,
            "email"       to email,
            "displayName" to displayName,
            "createdAt"   to System.currentTimeMillis()
        )

        mFirestore.collection("users")
            .document(uid)                   // use the Firebase UID as the document ID
            .set(userMap)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Inscription reussie!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
                Toast.makeText(
                    this,
                    "Erreur Firestore: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}