package com.example.fyndstay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FiltersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)
        val locality = findViewById<Spinner>(R.id.spinnerLocality)
        val floor = findViewById<Spinner>(R.id.spinnerFloor)
        val gender = findViewById<Spinner>(R.id.spinnerGender)
        val breakfast = findViewById<CheckBox>(R.id.checkBreakfast)
        val lunch = findViewById<CheckBox>(R.id.checkLunch)
        val dinner = findViewById<CheckBox>(R.id.checkDinner)
        val inTime = findViewById<CheckBox>(R.id.checkHasInTime)
        val minRent = findViewById<EditText>(R.id.editMinRent)
        val maxRent = findViewById<EditText>(R.id.editMaxRent)
        spinner(locality, (listOf("Any Locality") + MumbaiAreas.all).toTypedArray())
        spinner(floor, arrayOf("Any", "Ground Floor", "1st Floor", "2nd Floor", "3rd Floor", "3rd Floor or Above"))
        spinner(gender, arrayOf("Any", "Only Male", "Only Female", "Mixed"))
        findViewById<Button>(R.id.btnApplyFilters).setOnClickListener {
            val low = minRent.text.toString().trim().toFloatOrNull() ?: 0f
            val high = maxRent.text.toString().trim().toFloatOrNull() ?: 1_000_000f
            if (low > high) {
                Toast.makeText(this, "Minimum rent cannot exceed maximum rent", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = Intent().apply {
                putExtra("locality", locality.selectedItem.toString())
                putExtra("floor", floor.selectedItem.toString())
                putExtra("gender", gender.selectedItem.toString())
                putExtra("minRent", low)
                putExtra("maxRent", high)
                putExtra("hasInTime", inTime.isChecked)
                putExtra("breakfast", breakfast.isChecked)
                putExtra("lunch", lunch.isChecked)
                putExtra("dinner", dinner.isChecked)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
        findViewById<Button>(R.id.btnClearFilters).setOnClickListener {
            locality.setSelection(0); floor.setSelection(0); gender.setSelection(0)
            listOf(breakfast, lunch, dinner, inTime).forEach { it.isChecked = false }
            minRent.setText(""); maxRent.setText("")
            // Return the cleared state to the dashboard rather than silently cancelling.
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("locality", "Any Locality"); putExtra("floor", "Any"); putExtra("gender", "Any")
                putExtra("minRent", 0f); putExtra("maxRent", 1_000_000f)
            })
            finish()
        }
    }
    private fun spinner(view: Spinner, choices: Array<String>) {
        view.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, choices).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
}
