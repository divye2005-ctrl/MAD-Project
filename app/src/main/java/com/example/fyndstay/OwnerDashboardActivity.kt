package com.example.fyndstay

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OwnerDashboardActivity : AppCompatActivity(), PGAdapter.Listener {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val pgs = mutableListOf<PG>()
    private lateinit var adapter: PGAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var empty: TextView
    private var listening = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)
        recycler = findViewById(R.id.rvOwnerPGs)
        empty = findViewById(R.id.tvEmptyOwner)
        adapter = PGAdapter(this, pgs, this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        findViewById<Button>(R.id.btnAddPg).setOnClickListener {
            startActivity(Intent(this, AddPGActivity::class.java))
        }
        findViewById<BottomNavigationView>(R.id.bottomNav).apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> true
                    R.id.nav_requests -> {
                        startActivity(Intent(this@OwnerDashboardActivity, OwnerRequestsActivity::class.java)); true
                    }
                    R.id.nav_profile -> { toast("Profile (coming soon)"); true }
                    else -> false
                }
            }
            selectedItemId = R.id.nav_home
        }
        loadOwnerPGs()
    }
    private fun loadOwnerPGs() {
        if (listening) return
        val uid = auth.currentUser?.uid ?: run { toast("Please login again"); return }
        listening = true
        db.collection("PGs").whereEqualTo("ownerId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null) { toast("Error: ${error.message}"); return@addSnapshotListener }
                pgs.clear()
                snapshot?.documents?.forEach { doc ->
                    doc.toObject(PG::class.java)?.let { it.pgId = doc.id; pgs.add(it) }
                }
                adapter.notifyDataSetChanged()
                empty.visibility = if (pgs.isEmpty()) View.VISIBLE else View.GONE
                recycler.visibility = if (pgs.isEmpty()) View.GONE else View.VISIBLE
            }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logout, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_logout) return super.onOptionsItemSelected(item)
        Utils.showLogoutDialog(this, Runnable {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        })
        return true
    }
    override fun onPGClicked(pg: PG) {
        val id = pg.pgId ?: run { toast("Invalid PG selected"); return }
        startActivity(Intent(this, EditPGActivity::class.java).putExtra("pgId", id))
    }
    override fun onWishlistToggle(pg: PG, newState: Boolean, position: Int) = Unit
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
