package com.leanmasscalculator.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.leanmasscalculator.app.R
import com.leanmasscalculator.app.calculator.MainActivity

//LoginScreen no viewbinding
class LoginActivity : AppCompatActivity() {

    // WITHOUT ViewBinding — using findViewById
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        //Find views using findViewById
        etEmail       = findViewById(R.id.et_email)
        etPassword    = findViewById(R.id.et_password)
        btnLogin      = findViewById(R.id.btn_login)
        tvGoToRegister = findViewById(R.id.tv_go_to_register)
        progressBar   = findViewById(R.id.progress_bar)

        //If user is already logged in, skip login
        if (mAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //Login button click
        btnLogin.setOnClickListener {
            loginUser()
        }

        //  Go to register
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Input validation
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

        // Show loading
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        // Firebase Auth sign in
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Connexion reussie", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Erreur: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}