package com.example.fyndstay

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object Utils {
    @JvmStatic fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    @JvmStatic fun showLogoutDialog(activity: Activity, onConfirm: Runnable) {
        AlertDialog.Builder(activity).setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ -> onConfirm.run() }
            .setNegativeButton("Cancel", null).show()
    }
    @JvmStatic fun <T> chunkList(list: List<T>?, chunkSize: Int): List<List<T>> =
        if (list.isNullOrEmpty() || chunkSize <= 0) emptyList() else list.chunked(chunkSize)
}
