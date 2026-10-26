package com.example.fyndstay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WishlistActivity : AppCompatActivity(), PGAdapter.Listener {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val user by lazy { FirebaseAuth.getInstance().currentUser }
    private val wishlist = mutableListOf<PG>()
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var adapter: PGAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)
        list = findViewById(R.id.rvWishlist)
        empty = findViewById(R.id.tvEmptyState)
        adapter = PGAdapter(this, wishlist, this)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        if (user == null) { toast("Please log in to view wishlist"); finish(); return }
        loadWishlist()
        findViewById<BottomNavigationView>(R.id.bottomNav).apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this@WishlistActivity, StudentDashboardActivity::class.java)); true }
                    R.id.nav_wishlist -> true
                    R.id.nav_filters -> { startActivity(Intent(this@WishlistActivity, FiltersActivity::class.java)); true }
                    R.id.nav_profile -> { toast("Profile (coming soon)"); true }
                    else -> false
                }
            }
            selectedItemId = R.id.nav_wishlist
        }
    }
    private fun loadWishlist() {
        val uid = user?.uid ?: return
        db.collection("Wishlists").document(uid).collection("UserWishlist")
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null) { toast("Error: ${error.message}"); return@addSnapshotListener }
                val ids = snapshot?.documents?.map { it.getString("pgId")?.takeIf(String::isNotBlank) ?: it.id }?.toSet().orEmpty()
                wishlist.clear()
                if (ids.isEmpty()) { refresh(); return@addSnapshotListener }
                // PGs is a top-level collection, not a nested owner collection.
                db.collection("PGs").get().addOnSuccessListener { all ->
                    wishlist.clear()
                    all.documents.filter { it.id in ids }.forEach { doc ->
                        doc.toObject(PG::class.java)?.let { it.pgId = doc.id; it.inWishlist = true; wishlist.add(it) }
                    }
                    // Wishlisted suggested listings are not in Firestore; load them from samples.
                    ids.filter { id -> wishlist.none { it.pgId == id } }.forEach { id ->
                        SamplePGs.find(id)?.let { it.inWishlist = true; wishlist.add(it) }
                    }
                    refresh()
                }.addOnFailureListener { toast("Wishlist fetch failed: ${it.message}") }
            }
    }
    private fun refresh() {
        adapter.notifyDataSetChanged()
        empty.visibility = if (wishlist.isEmpty()) View.VISIBLE else View.GONE
        list.visibility = if (wishlist.isEmpty()) View.GONE else View.VISIBLE
    }
    override fun onPGClicked(pg: PG) {
        startActivity(Intent(this, PGDetailActivity::class.java).putExtra("pgId", pg.pgId))
    }
    override fun onWishlistToggle(pg: PG, newState: Boolean, position: Int) {
        val uid = user?.uid ?: return
        val id = pg.pgId ?: return
        val ref = db.collection("Wishlists").document(uid).collection("UserWishlist").document(id)
        if (newState) ref.set(mapOf("pgId" to id, "timestamp" to System.currentTimeMillis()))
            .addOnFailureListener { toast("Error: ${it.message}") }
        else ref.delete().addOnFailureListener { toast("Error: ${it.message}") }
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
