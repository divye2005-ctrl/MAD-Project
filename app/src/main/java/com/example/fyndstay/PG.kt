package com.example.fyndstay

import com.google.firebase.firestore.Exclude

class PG {
    @get:Exclude @set:Exclude var pgId: String? = null
    var pgName: String? = null
    var address: String? = null
    var rent: String? = null
    var contactNumber: String? = null
    var locality: String? = null
    var floor: String? = null
    var genderRestriction: String? = null
    var city: String? = null
    var imageUrl: String? = null
    var inTime: String? = null
    var ownerId: String? = null
    @get:JvmName("isHasInTime") var hasInTime: Boolean = false
    @get:JvmName("isBreakfastProvided") var breakfastProvided: Boolean = false
    @get:JvmName("isLunchProvided") var lunchProvided: Boolean = false
    @get:JvmName("isDinnerProvided") var dinnerProvided: Boolean = false
    @get:JvmName("isVegMeals") var vegMeals: Boolean = false
    @get:JvmName("isNonVegAllowed") var nonVegAllowed: Boolean = false
    @get:JvmName("isKitchenAccess") var kitchenAccess: Boolean = false
    @get:JvmName("isInductionProvided") var inductionProvided: Boolean = false
    @get:JvmName("isIronAvailable") var ironAvailable: Boolean = false
    @get:JvmName("isOvernightPassAvailable") var overnightPassAvailable: Boolean = false
    @get:Exclude @set:Exclude @get:JvmName("isInWishlist") var inWishlist: Boolean = false
}
