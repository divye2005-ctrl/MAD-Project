package com.example.fyndstay

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditPGActivity : AppCompatActivity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance().getReference("pg_images") }
    private lateinit var pgId: String
    private var imageUri: Uri? = null
    private lateinit var preview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pg)
        pgId = intent.getStringExtra("pgId") ?: run { finish(); return }
        preview = findViewById(R.id.imgPreview)
        db.collection("PGs").document(pgId).get().addOnSuccessListener { doc ->
            val pg = doc.toObject(PG::class.java) ?: return@addOnSuccessListener
            field(R.id.etName).setText(pg.pgName)
            field(R.id.etRent).setText(pg.rent)
            field(R.id.etLocality).setText(pg.locality)
            field(R.id.etAddress).setText(pg.address)
            field(R.id.etContact).setText(pg.contactNumber)
            val image = pg.imageUrl
            if (!image.isNullOrBlank()) Glide.with(this).load(image).into(preview)
            else preview.setImageResource(R.drawable.ic_home)
        }.addOnFailureListener { Utils.toast(this, "Failed to load PG: ${it.message}") }
        findViewById<Button>(R.id.btnUploadImage).setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), 101)
        }
        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 102)
        }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveChanges() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { deletePG() }
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return
        if (requestCode == 101) {
            imageUri = data.data
            imageUri?.let { preview.setImageURI(it) }
        } else if (requestCode == 102) {
            val bitmap = data.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                val bytes = ByteArrayOutputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, this)
                }.toByteArray()
                val destination = cacheDir.resolve("pg-photo-${System.currentTimeMillis()}.jpg")
                destination.writeBytes(bytes)
                imageUri = Uri.fromFile(destination)
                preview.setImageBitmap(bitmap)
            } else Utils.toast(this, "Captured image not available")
        }
    }
    private fun field(id: Int) = findViewById<EditText>(id)
    private fun saveChanges() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { Utils.toast(this, "Login required"); return }
        db.collection("PGs").document(pgId).get().addOnSuccessListener { doc ->
            if (doc.getString("ownerId") != uid) { Utils.toast(this, "Not your PG"); return@addOnSuccessListener }
            val changes = mutableMapOf<String, Any>(
                "pgName" to field(R.id.etName).text.toString().trim(),
                "rent" to field(R.id.etRent).text.toString().trim(),
                "locality" to field(R.id.etLocality).text.toString().trim(),
                "address" to field(R.id.etAddress).text.toString().trim(),
                "contactNumber" to field(R.id.etContact).text.toString().trim()
            )
            val photo = imageUri
            if (photo == null) update(changes)
            else {
                val ref = storage.child("${pgId}_${System.currentTimeMillis()}.jpg")
                ref.putFile(photo).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        changes["imageUrl"] = url.toString()
                        update(changes)
                    }.addOnFailureListener { Utils.toast(this, "Failed to get download URL: ${it.message}") }
                }.addOnFailureListener { Utils.toast(this, "Upload failed: ${it.message}") }
            }
        }.addOnFailureListener { Utils.toast(this, "Failed to load PG: ${it.message}") }
    }
    private fun update(changes: Map<String, Any>) {
        db.collection("PGs").document(pgId).update(changes)
            .addOnSuccessListener { Utils.toast(this, "PG updated successfully!"); finish() }
            .addOnFailureListener { Utils.toast(this, "Update failed: ${it.message}") }
    }
    private fun deletePG() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { Utils.toast(this, "Login required"); return }
        db.collection("PGs").document(pgId).get().addOnSuccessListener { doc ->
            if (doc.getString("ownerId") != uid) { Utils.toast(this, "Not your PG"); return@addOnSuccessListener }
            doc.reference.delete()
                .addOnSuccessListener { Utils.toast(this, "PG deleted successfully!"); finish() }
                .addOnFailureListener { Utils.toast(this, "Failed to delete: ${it.message}") }
        }.addOnFailureListener { Utils.toast(this, "Failed to load PG: ${it.message}") }
    }
}
