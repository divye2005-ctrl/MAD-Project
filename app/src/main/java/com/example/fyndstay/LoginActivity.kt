package com.example.fyndstay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailField = findViewById(R.id.loginEmail)
        passwordField = findViewById(R.id.loginPassword)
        findViewById<Button>(R.id.btnLogin).setOnClickListener { loginUser() }
        findViewById<TextView>(R.id.textRegisterRedirect).setOnClickListener {
            startActivity(Intent(this, ChooseRoleActivity::class.java))
        }
        findViewById<TextView>(R.id.forgotPassword).setOnClickListener {
            val email = emailField.text.toString().trim()
            if (email.isEmpty()) {
                toast("Enter your email first!")
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) toast("Reset email sent.")
                    else toast("Error: ${task.exception?.message ?: "Unknown error"}")
                }
            }
        }
    }

    private fun loginUser() {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            toast("Please enter all fields")
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                toast("Login failed: ${task.exception?.message ?: "Unknown error"}")
                return@addOnCompleteListener
            }
            val user = auth.currentUser ?: return@addOnCompleteListener
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        toast("User record not found in Firestore.")
                        return@addOnSuccessListener
                    }
                    when (document.getString("role")?.lowercase()) {
                        "student" -> {
                            toast("Welcome back, Student!")
                            startActivity(Intent(this, StudentDashboardActivity::class.java))
                            finish()
                        }
                        "owner" -> {
                            toast("Welcome back, Owner!")
                            startActivity(Intent(this, OwnerDashboardActivity::class.java))
                            finish()
                        }
                        else -> toast("Unknown role. Please register again.")
                    }
                }
                .addOnFailureListener { toast("Error fetching user role: ${it.message}") }
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
