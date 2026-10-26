package com.example.fyndstay

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OwnerRequestsActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val requests = mutableListOf<RequestItem>()
    private lateinit var adapter: RequestAdapter
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_requests)
        list = findViewById(R.id.rvRequests)
        empty = findViewById(R.id.tvEmptyState)
        adapter = RequestAdapter(requests)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        loadRequests()
        findViewById<BottomNavigationView>(R.id.bottomNav).apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this@OwnerRequestsActivity, OwnerDashboardActivity::class.java)); true }
                    R.id.nav_requests -> true
                    R.id.nav_profile -> { toast("Profile (coming soon)"); true }
                    else -> false
                }
            }
            selectedItemId = R.id.nav_requests
        }
    }
    private fun loadRequests() {
        val uid = auth.currentUser?.uid ?: run { toast("Please login as owner to see requests"); return }
        db.collection("Requests").document(uid).collection("receivedRequests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(this) { snapshot, error ->
                if (error != null) { toast("Error loading requests: ${error.message}"); return@addSnapshotListener }
                requests.clear()
                snapshot?.documents?.forEach { doc ->
                    doc.toObject(RequestItem::class.java)?.let { requests.add(it) }
                }
                adapter.notifyDataSetChanged()
                empty.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
                list.visibility = if (requests.isEmpty()) View.GONE else View.VISIBLE
            }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean { menuInflater.inflate(R.menu.menu_logout, menu); return true }
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
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
