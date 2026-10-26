package com.example.fyndstay

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class PGDetailActivity : AppCompatActivity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val user by lazy { FirebaseAuth.getInstance().currentUser }
    private lateinit var pgId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pg_detail)
        pgId = intent.getStringExtra("pgId") ?: run {
            Utils.toast(this, "Invalid PG"); finish(); return
        }
        db.collection("PGs").document(pgId).get()
            .addOnSuccessListener { doc ->
                when {
                    doc.exists() -> bindPG(doc)
                    else -> {
                        // Suggested (sample) listings are not stored in Firestore.
                        val sample = SamplePGs.find(pgId)
                        if (sample != null) bindSample(sample)
                        else { Utils.toast(this, "Failed to load PG"); finish() }
                    }
                }
            }
            .addOnFailureListener {
                val sample = SamplePGs.find(pgId)
                if (sample != null) bindSample(sample)
                else { Utils.toast(this, "Failed to load PG"); finish() }
            }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean { menuInflater.inflate(R.menu.menu_main, menu); return true }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_logout -> {
            Utils.showLogoutDialog(this, Runnable {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            })
            true
        }
        R.id.menu_requests -> { startActivity(Intent(this, OwnerRequestsActivity::class.java)); true }
        else -> super.onOptionsItemSelected(item)
    }
    private fun label(id: Int, value: String) { findViewById<TextView>(id).text = value }

    private fun bindCommon(pg: PG) {
        label(R.id.tvName, pg.pgName ?: "Unnamed")
        label(R.id.tvRent, pg.rent?.let { "₹ $it/month" } ?: "Rent NA")
        label(R.id.tvLocality, "📍 ${pg.locality ?: "-"}, ${pg.city ?: "Mumbai"}")
        label(R.id.tvAddress, pg.address ?: "-")
        label(R.id.tvGender, "Gender: ${pg.genderRestriction ?: "Any"}")
        label(R.id.tvInTime, if (pg.hasInTime) {
            val time = pg.inTime.orEmpty()
            if (time.isBlank()) "Has In-Time Restriction" else "In-Time: $time"
        } else "No In-Time Restriction")
        label(R.id.tvAmenities, "Amenities: ${amenities(pg)}")
        val image = findViewById<ImageView>(R.id.imgPG)
        if (pg.imageUrl.isNullOrBlank()) {
            val pad = (48 * resources.displayMetrics.density).toInt()
            image.setPadding(pad, pad, pad, pad)
            image.scaleType = ImageView.ScaleType.CENTER_INSIDE
            image.setBackgroundColor(Color.parseColor("#EFE6E0"))
            image.setColorFilter(Color.parseColor("#B79C93"))
            image.setImageResource(R.drawable.ic_home)
        } else {
            Glide.with(this).load(pg.imageUrl).into(image)
        }
    }

    private fun amenities(pg: PG): String {
        val meals = listOfNotNull(
            "Breakfast".takeIf { pg.breakfastProvided },
            "Lunch".takeIf { pg.lunchProvided },
            "Dinner".takeIf { pg.dinnerProvided })
        val extras = listOfNotNull(
            "Veg meals".takeIf { pg.vegMeals },
            "Non-veg allowed".takeIf { pg.nonVegAllowed },
            "Kitchen access".takeIf { pg.kitchenAccess },
            "Induction".takeIf { pg.inductionProvided },
            "Iron".takeIf { pg.ironAvailable },
            "Overnight pass".takeIf { pg.overnightPassAvailable })
        val parts = mutableListOf<String>()
        if (meals.isNotEmpty()) parts.add("Meals: ${meals.joinToString(", ")}")
        parts.addAll(extras)
        return if (parts.isEmpty()) "Ask owner for details" else parts.joinToString("  •  ")
    }

    private fun bindPG(doc: DocumentSnapshot) {
        val pg = doc.toObject(PG::class.java) ?: return
        pg.pgId = doc.id
        bindCommon(pg)
        val request = findViewById<Button>(R.id.btnRequest)
        val contact = findViewById<Button>(R.id.btnContact)
        val edit = findViewById<Button>(R.id.btnEdit)
        if (user?.uid == pg.ownerId) {
            request.visibility = View.GONE; contact.visibility = View.GONE; edit.visibility = View.VISIBLE
            edit.setOnClickListener { startActivity(Intent(this, EditPGActivity::class.java).putExtra("pgId", pgId)) }
        } else {
            edit.visibility = View.GONE; request.visibility = View.VISIBLE; contact.visibility = View.VISIBLE
            request.setOnClickListener { sendRequest(pg) }
            contact.setOnClickListener {
                val phone = pg.contactNumber
                if (phone.isNullOrBlank()) Utils.toast(this, "Contact not available")
                else startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            }
        }
    }

    // Suggested listings are demo data: show details but no request/contact actions.
    private fun bindSample(pg: PG) {
        bindCommon(pg)
        findViewById<Button>(R.id.btnRequest).visibility = View.GONE
        findViewById<Button>(R.id.btnContact).visibility = View.GONE
        findViewById<Button>(R.id.btnEdit).visibility = View.GONE
        findViewById<TextView>(R.id.tvDemoNote).visibility = View.VISIBLE
    }

    private fun sendRequest(pg: PG) {
        val student = user ?: run { Utils.toast(this, "Please login to send request"); return }
        val owner = pg.ownerId ?: run { Utils.toast(this, "Owner info missing"); return }
        val request = mapOf(
            "studentId" to student.uid, "studentName" to (student.displayName ?: "Student"),
            "pgId" to pgId, "pgName" to pg.pgName, "timestamp" to System.currentTimeMillis()
        )
        db.collection("Requests").document(owner).collection("receivedRequests").add(request)
            .addOnSuccessListener { Utils.toast(this, "Tour request sent!") }
            .addOnFailureListener { Utils.toast(this, "Failed to send request: ${it.message}") }
    }
}
