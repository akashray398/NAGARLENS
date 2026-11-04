package com.nagarlens.app

import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.TAXONOMY_VERSION
import org.junit.Assert.assertEquals
import org.junit.Test

class DefectTaxonomyTest {

    @Test
    fun `defect taxonomy defines nine distinct categories`() {
        assertEquals(9, DefectCategory.entries.size)
        assertEquals("v1.0.0", TAXONOMY_VERSION)
    }

    @Test
    fun `category names match standardized specification`() {
        val categories = DefectCategory.entries.map { it.name }
        assertTrue(categories.contains("DRAIN_BLOCKAGE"))
        assertTrue(categories.contains("SOLID_WASTE"))
        assertTrue(categories.contains("SILT_SEDIMENT"))
        assertTrue(categories.contains("WATERLOGGING"))
        assertTrue(categories.contains("STRUCTURAL_DAMAGE"))
        assertTrue(categories.contains("COVER_DAMAGE_MISSING"))
        assertTrue(categories.contains("DRAIN_OVERFLOW"))
        assertTrue(categories.contains("VEGETATION_INTRUSION"))
        assertTrue(categories.contains("UNKNOWN_INSUFFICIENT"))
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
