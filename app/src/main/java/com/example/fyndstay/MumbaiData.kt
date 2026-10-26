package com.example.fyndstay

/**
 * Mumbai areas used across search suggestions, filters, and the Add PG form.
 */
object MumbaiAreas {
    // Short names for the quick-search chips on the student dashboard.
    // These match by substring, so "Andheri" finds both Andheri West and East.
    val popular = listOf("Andheri", "Bandra", "Juhu", "Powai", "Dadar", "Colaba", "Lower Parel", "Thane")

    // Full list for filter spinners and the Add PG form.
    val all = listOf(
        "Andheri West", "Andheri East",
        "Bandra West", "Bandra East",
        "Juhu", "Powai",
        "Vile Parle West", "Vile Parle East",
        "Santacruz West", "Santacruz East",
        "Dadar West", "Dadar East",
        "Kurla West", "Chembur",
        "Goregaon West", "Goregaon East",
        "Malad West", "Malad East",
        "Borivali West", "Borivali East",
        "Colaba", "Fort", "Lower Parel", "Worli",
        "Vashi (Navi Mumbai)", "Thane West"
    )
}
