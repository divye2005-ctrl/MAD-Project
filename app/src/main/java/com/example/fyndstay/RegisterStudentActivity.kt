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

class RegisterStudentActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_student)
        val inputs = mapOf("inputName" to findViewById<EditText>(R.id.inputName), "inputEmail" to findViewById<EditText>(R.id.inputEmail), "inputPassword" to findViewById<EditText>(R.id.inputPassword))
        findViewById<Button>(R.id.btnRegisterStudent).setOnClickListener { register(inputs) }
        findViewById<TextView>(R.id.textLoginRedirect).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    private fun register(inputs: Map<String, EditText>) {
        val values = inputs.mapValues { it.value.text.toString().trim() }
        if (values.values.any { it.isEmpty() }) {
            toast("All fields are required")
            return
        }
        val email = values["inputEmail"]!!
        val password = values["inputPassword"]!!
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { result ->
            if (!result.isSuccessful) {
                toast("Auth Error: ${result.exception?.message}")
                return@addOnCompleteListener
            }
            val user = auth.currentUser ?: return@addOnCompleteListener
            val profile = mapOf("uid" to user.uid, "name" to values["inputName"],
                "email" to email, "role" to "student")
            db.collection("users").document(user.uid).set(profile)
                .addOnSuccessListener {
                    toast("Registration successful!")
                    startActivity(Intent(this, StudentDashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener { toast("Firestore Error: ${it.message}") }
        }
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
