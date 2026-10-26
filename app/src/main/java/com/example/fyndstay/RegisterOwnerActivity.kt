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

class RegisterOwnerActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_owner)
        val inputs = mapOf("inputOwnerName" to findViewById<EditText>(R.id.inputOwnerName), "inputPGName" to findViewById<EditText>(R.id.inputPGName), "inputCity" to findViewById<EditText>(R.id.inputCity), "inputOwnerEmail" to findViewById<EditText>(R.id.inputOwnerEmail), "inputOwnerPassword" to findViewById<EditText>(R.id.inputOwnerPassword))
        findViewById<Button>(R.id.btnRegisterOwner).setOnClickListener { register(inputs) }
        findViewById<TextView>(R.id.textOwnerLoginRedirect).setOnClickListener {
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
        val email = values["inputOwnerEmail"]!!
        val password = values["inputOwnerPassword"]!!
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { result ->
            if (!result.isSuccessful) {
                toast("Auth Error: ${result.exception?.message}")
                return@addOnCompleteListener
            }
            val user = auth.currentUser ?: return@addOnCompleteListener
            val profile = mapOf("uid" to user.uid, "name" to values["inputOwnerName"],
                "pgName" to values["inputPGName"], "city" to values["inputCity"], "email" to email, "role" to "owner")
            db.collection("users").document(user.uid).set(profile)
                .addOnSuccessListener {
                    toast("Owner registered successfully!")
                    startActivity(Intent(this, OwnerDashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener { toast("Firestore Error: ${it.message}") }
        }
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
