package com.example.fyndstay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PGTest {
    @Test fun modelStoresListingAndWishlistState() {
        val pg = PG()
        pg.pgId = "listing-1"
        pg.pgName = "Test PG"
        pg.rent = "12000"
        pg.hasInTime = true
        pg.inWishlist = true
        assertEquals("listing-1", pg.pgId)
        assertEquals("Test PG", pg.pgName)
        assertEquals("12000", pg.rent)
        assertTrue(pg.hasInTime)
        assertTrue(pg.inWishlist)
        assertFalse(pg.breakfastProvided)
    }
}
