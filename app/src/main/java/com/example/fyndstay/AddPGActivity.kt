package com.example.fyndstay

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class AddPGActivity : AppCompatActivity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var selectedInTime = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pg)
        spinner(R.id.spinnerLocality, MumbaiAreas.all.toTypedArray())
        spinner(R.id.spinnerFloor, arrayOf("Ground Floor", "1st Floor", "2nd Floor", "3rd Floor", "4th Floor or Above"))
        spinner(R.id.spinnerGender, arrayOf("Only Male", "Only Female", "Mixed"))
        val timeButton = findViewById<Button>(R.id.btnSelectInTime)
        timeButton.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                selectedInTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                timeButton.text = "In-time: $selectedInTime"
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }
        findViewById<Button>(R.id.btnSavePG).setOnClickListener { savePG() }
    }
    private fun spinner(id: Int, choices: Array<String>) {
        findViewById<Spinner>(id).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, choices).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
    private fun text(id: Int) = findViewById<EditText>(id).text.toString().trim()
    private fun checked(id: Int) = findViewById<CheckBox>(id).isChecked
    private fun selected(id: Int) = findViewById<Spinner>(id).selectedItem.toString()
    private fun savePG() {
        val name = text(R.id.inputPGName)
        val address = text(R.id.inputAddress)
        val rent = text(R.id.inputRent)
        val contact = text(R.id.inputContact)
        if (listOf(name, address, rent, contact).any(String::isEmpty)) {
            Utils.toast(this, "Please fill all fields"); return
        }
        val uid = auth.currentUser?.uid ?: run { Utils.toast(this, "Login required"); return }
        val data = mapOf(
            "pgName" to name, "address" to address, "rent" to rent,
            "contactNumber" to contact, "ownerId" to uid, "city" to "Mumbai",
            "locality" to selected(R.id.spinnerLocality), "floor" to selected(R.id.spinnerFloor),
            "genderRestriction" to selected(R.id.spinnerGender),
            "hasInTime" to checked(R.id.checkHasInTime), "inTime" to selectedInTime,
            "breakfastProvided" to checked(R.id.checkBreakfast),
            "lunchProvided" to checked(R.id.checkLunch), "dinnerProvided" to checked(R.id.checkDinner),
            "vegMeals" to checked(R.id.checkVeg), "nonVegAllowed" to checked(R.id.checkNonVeg),
            "kitchenAccess" to checked(R.id.checkKitchen),
            "inductionProvided" to checked(R.id.checkInduction),
            "ironAvailable" to checked(R.id.checkIron),
            "overnightPassAvailable" to checked(R.id.checkOvernight),
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("PGs").add(data)
            .addOnSuccessListener { Utils.toast(this, "PG added!"); finish() }
            .addOnFailureListener { Utils.toast(this, "Error: ${it.message}") }
    }
}
