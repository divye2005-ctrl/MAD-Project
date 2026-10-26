package com.example.fyndstay

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class StudentDashboardActivity : AppCompatActivity(), PGAdapter.Listener {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val all = mutableListOf<PG>()
    private val shown = mutableListOf<PG>()
    private val wishlistIds = mutableSetOf<String>()
    private lateinit var adapter: PGAdapter
    private lateinit var search: TextInputEditText
    private var locality = "Any Locality"
    private var floor = "Any"
    private var gender = "Any"
    private var minRent = 0f
    private var maxRent = 1_000_000f
    private var requireInTime = false
    private var breakfast = false
    private var lunch = false
    private var dinner = false
    private val filterCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)
        search = findViewById(R.id.etSearch)
        adapter = PGAdapter(this, shown, this)
        findViewById<RecyclerView>(R.id.rvPGs).apply {
            layoutManager = LinearLayoutManager(this@StudentDashboardActivity)
            adapter = this@StudentDashboardActivity.adapter
        }
        setupAreaChips()
        loadPGs()
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = applyFilters()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        findViewById<ImageButton>(R.id.btnFilter)?.setOnClickListener { openFilters() }
        findViewById<BottomNavigationView>(R.id.bottomNav).apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> true
                    R.id.nav_wishlist -> { startActivity(Intent(this@StudentDashboardActivity, WishlistActivity::class.java)); true }
                    R.id.nav_filters -> { openFilters(); true }
                    R.id.nav_profile -> { toast("Profile (coming soon)"); true }
                    else -> false
                }
            }
            selectedItemId = R.id.nav_home
        }
        findViewById<MaterialToolbar>(R.id.topAppBar).setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) { logout(); true } else false
        }
    }

    // Quick-search chips for popular Mumbai areas: tapping one fills the search box.
    private fun setupAreaChips() {
        val container = findViewById<LinearLayout>(R.id.chipsLayout) ?: return
        val areas = listOf("All") + MumbaiAreas.popular
        val gap = (8 * resources.displayMetrics.density).toInt()
        areas.forEach { area ->
            val chip = Chip(this).apply {
                text = area
                isCheckable = false
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                chipStrokeWidth = resources.displayMetrics.density
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#D8CFC9"))
                setTextColor(Color.parseColor("#3C2F2F"))
                setOnClickListener {
                    search.setText(if (area == "All") "" else area)
                    search.setSelection(search.text?.length ?: 0)
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = gap }
            container.addView(chip, params)
        }
    }

    private fun loadPGs() {
        val user = auth.currentUser
        if (user == null) { listenPGs(); return }
        db.collection("Wishlists").document(user.uid).collection("UserWishlist")
            .addSnapshotListener(this) { snapshot, _ ->
                wishlistIds.clear()
                snapshot?.documents?.forEach {
                    wishlistIds.add(it.getString("pgId")?.takeIf(String::isNotBlank) ?: it.id)
                }
                all.forEach { pg -> pg.inWishlist = pg.pgId in wishlistIds }
                adapter.notifyDataSetChanged()
            }
        listenPGs()
    }
    private fun listenPGs() {
        db.collection("PGs").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null) { toast("Error loading PGs: ${error.message}"); return@addSnapshotListener }
                all.clear()
                snapshot?.documents?.forEach { doc ->
                    doc.toObject(PG::class.java)?.let {
                        it.pgId = doc.id
                        it.inWishlist = doc.id in wishlistIds
                        all.add(it)
                    }
                }
                // Suggested Mumbai PGs after the real listings so the app never looks empty.
                SamplePGs.list.forEach { sample ->
                    if (all.none { it.pgId == sample.pgId }) {
                        sample.inWishlist = sample.pgId in wishlistIds
                        all.add(sample)
                    }
                }
                applyFilters()
            }
    }
    private fun applyFilters() {
        val query = search.text?.toString()?.trim()?.lowercase().orEmpty()
        shown.clear()
        shown.addAll(all.filter { pg ->
            if (locality != "Any Locality" && !pg.locality.equals(locality, true)) return@filter false
            if (floor != "Any") {
                val actual = pg.floor.orEmpty().lowercase()
                if (floor.equals("3rd Floor or Above", true)) {
                    if (!(actual.contains("3") || actual.contains("4") || actual.contains("above"))) return@filter false
                } else if (!actual.contains(floor.substringBefore(" ").lowercase())) return@filter false
            }
            val rentValue = pg.rent?.replace(Regex("[^0-9]"), "")?.toIntOrNull()
            if (rentValue != null && (rentValue < minRent || rentValue > maxRent)) return@filter false
            if (gender != "Any" && !pg.genderRestriction.equals(gender, true)) return@filter false
            if (requireInTime && !pg.hasInTime) return@filter false
            if (breakfast && !pg.breakfastProvided) return@filter false
            if (lunch && !pg.lunchProvided) return@filter false
            if (dinner && !pg.dinnerProvided) return@filter false
            query.isEmpty() || listOf(pg.pgName, pg.locality, pg.address, pg.rent)
                .any { it?.lowercase()?.contains(query) == true }
        })
        adapter.notifyDataSetChanged()
        findViewById<TextView>(R.id.tvEmptyResults)?.visibility =
            if (shown.isEmpty()) View.VISIBLE else View.GONE
    }
    override fun onWishlistToggle(pg: PG, newState: Boolean, position: Int) {
        val id = pg.pgId ?: run { toast("Invalid PG"); return }
        val uid = auth.currentUser?.uid ?: run { toast("Please log in first"); return }
        val root = db.collection("Wishlists").document(uid)
        val item = root.collection("UserWishlist").document(id)
        if (newState) {
            root.set(emptyMap<String, Any>(), SetOptions.merge())
            item.set(mapOf("pgId" to id, "timestamp" to System.currentTimeMillis()))
                .addOnSuccessListener { wishlistIds.add(id); toast("Added to wishlist") }
                .addOnFailureListener { pg.inWishlist = false; adapter.notifyDataSetChanged(); toast("Error: ${it.message}") }
        } else {
            item.delete().addOnSuccessListener { wishlistIds.remove(id); toast("Removed from wishlist") }
                .addOnFailureListener { pg.inWishlist = true; adapter.notifyDataSetChanged(); toast("Error: ${it.message}") }
        }
    }
    override fun onPGClicked(pg: PG) {
        startActivity(Intent(this, PGDetailActivity::class.java).putExtra("pgId", pg.pgId))
    }
    private fun openFilters() {
        @Suppress("DEPRECATION")
        startActivityForResult(Intent(this, FiltersActivity::class.java), filterCode)
    }
    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != filterCode || resultCode != Activity.RESULT_OK || data == null) return
        locality = data.getStringExtra("locality") ?: "Any Locality"
        floor = data.getStringExtra("floor") ?: "Any"
        gender = data.getStringExtra("gender") ?: "Any"
        minRent = data.getFloatExtra("minRent", 0f)
        maxRent = data.getFloatExtra("maxRent", 1_000_000f)
        requireInTime = data.getBooleanExtra("hasInTime", false)
        breakfast = data.getBooleanExtra("breakfast", false)
        lunch = data.getBooleanExtra("lunch", false)
        dinner = data.getBooleanExtra("dinner", false)
        search.setText("")
        applyFilters()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean { menuInflater.inflate(R.menu.menu_logout, menu); return true }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = if (item.itemId == R.id.action_logout) {
        logout(); true
    } else super.onOptionsItemSelected(item)
    private fun logout() = Utils.showLogoutDialog(this, Runnable {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    })
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
