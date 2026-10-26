package com.example.fyndstay

/**
 * Curated sample PG listings across Mumbai areas.
 *
 * These appear on the student dashboard marked as "Suggested" so the app
 * never looks empty and students get ideas of what to search for. Real
 * listings from Firestore always appear first; samples fill in after them.
 * Sample ids start with [ID_PREFIX] so the app can treat them differently
 * (for example, no tour requests on a demo listing).
 */
object SamplePGs {
    const val ID_PREFIX = "sample_"

    fun isSample(pg: PG): Boolean = pg.pgId?.startsWith(ID_PREFIX) == true

    fun find(id: String?): PG? = list.firstOrNull { it.pgId == id }

    private val photos = listOf(
        "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1493809842364-78817add7ffb?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1536376072261-38c75010e6c9?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1560185007-cde436f6a4d0?auto=format&fit=crop&w=800&q=70",
        "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=800&q=70"
    )
    private var photoIndex = 0

    val list: List<PG> = listOf(
        sample("sample_andheri_w", "Sunrise Residency PG", "Andheri West",
            "203, Veera Desai Road, Near Metro Station", "12500", "Mixed", "2nd Floor",
            breakfast = true, lunch = true, kitchen = true),
        sample("sample_andheri_e", "Metro View PG", "Andheri East",
            "Near JB Nagar Metro, Chakala", "11000", "Only Male", "1st Floor",
            breakfast = true),
        sample("sample_bandra_w", "Bandra Nest Girls PG", "Bandra West",
            "Linking Road, Near National College", "18500", "Only Female", "3rd Floor",
            breakfast = true, lunch = true, dinner = true, veg = true, iron = true),
        sample("sample_juhu", "Seaside Stay PG", "Juhu",
            "JVPD Scheme, Near Juhu Beach", "15500", "Mixed", "1st Floor",
            breakfast = true, overnight = true),
        sample("sample_powai", "Lakeview PG", "Powai",
            "Hiranandani Gardens, Near IIT Main Gate", "14500", "Only Male", "4th Floor or Above",
            breakfast = true, lunch = true, inTime = true, inTimeValue = "23:00"),
        sample("sample_vileparle_w", "Parle Comfort PG", "Vile Parle West",
            "Near NMIMS, Vile Parle West", "11500", "Only Female", "2nd Floor",
            breakfast = true, dinner = true, iron = true),
        sample("sample_santacruz_w", "Cruz Homes PG", "Santacruz West",
            "S V Road, Near Santacruz Station", "12000", "Mixed", "1st Floor",
            breakfast = true, kitchen = true),
        sample("sample_dadar_w", "Shivaji Park PG", "Dadar West",
            "Keluskar Road, Near Shivaji Park", "10500", "Only Male", "Ground Floor",
            breakfast = true, lunch = true, veg = true),
        sample("sample_kurla_w", "Phoenix Stay PG", "Kurla West",
            "LBS Marg, Near Phoenix Marketcity", "9200", "Only Male", "2nd Floor",
            breakfast = true),
        sample("sample_chembur", "Green Acres PG", "Chembur",
            "Near Chembur Gymkhana", "9800", "Mixed", "1st Floor",
            breakfast = true, lunch = true, dinner = true),
        sample("sample_goregaon_w", "Film City PG", "Goregaon West",
            "Near Film City Road", "10200", "Mixed", "2nd Floor",
            breakfast = true, overnight = true),
        sample("sample_malad_w", "Inorbit Stay PG", "Malad West",
            "Link Road, Near Inorbit Mall", "10800", "Only Female", "3rd Floor",
            breakfast = true, dinner = true, iron = true),
        sample("sample_borivali_w", "National Park View PG", "Borivali West",
            "Near Sanjay Gandhi National Park Gate", "8500", "Mixed", "Ground Floor",
            breakfast = true, kitchen = true),
        sample("sample_colaba", "Gateway Stay PG", "Colaba",
            "Colaba Causeway, Near Gateway of India", "20000", "Only Female", "2nd Floor",
            breakfast = true, lunch = true, dinner = true, inTime = true, inTimeValue = "22:30"),
        sample("sample_fort", "Heritage PG", "Fort",
            "Near CST Station, Fort", "17500", "Only Male", "1st Floor",
            breakfast = true, lunch = true),
        sample("sample_lowerparel", "Skyline PG", "Lower Parel",
            "Senapati Bapat Marg, Near Phoenix Mills", "16000", "Mixed", "4th Floor or Above",
            breakfast = true, lunch = true, overnight = true),
        sample("sample_worli", "Sea Face PG", "Worli",
            "Worli Sea Face, Near Sea Link", "15800", "Mixed", "3rd Floor",
            breakfast = true, dinner = true),
        sample("sample_vashi", "Palm Beach PG", "Vashi (Navi Mumbai)",
            "Sector 17, Near Vashi Station", "8200", "Mixed", "1st Floor",
            breakfast = true, kitchen = true),
        sample("sample_thane_w", "Upvan Lake PG", "Thane West",
            "Pokhran Road, Near Upvan Lake", "7800", "Mixed", "Ground Floor",
            breakfast = true, veg = true)
    )

    private fun sample(
        id: String, name: String, locality: String, address: String, rent: String,
        gender: String, floor: String,
        breakfast: Boolean = false, lunch: Boolean = false, dinner: Boolean = false,
        veg: Boolean = false, kitchen: Boolean = false, iron: Boolean = false,
        overnight: Boolean = false, inTime: Boolean = false, inTimeValue: String = ""
    ): PG = PG().apply {
        pgId = id
        pgName = name
        this.locality = locality
        this.address = address
        this.rent = rent
        genderRestriction = gender
        this.floor = floor
        city = "Mumbai"
        imageUrl = photos[photoIndex++ % photos.size]
        contactNumber = "9876543210"
        breakfastProvided = breakfast
        lunchProvided = lunch
        dinnerProvided = dinner
        vegMeals = veg
        kitchenAccess = kitchen
        ironAvailable = iron
        overnightPassAvailable = overnight
        hasInTime = inTime
        this.inTime = inTimeValue
    }
}
