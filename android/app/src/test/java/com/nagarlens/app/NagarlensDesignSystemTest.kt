package com.nagarlens.app

import com.nagarlens.app.core.designsystem.theme.NagarlensSpacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NagarlensDesignSystemTest {

    @Test
    fun `spacing tokens follow touch target ergonomics`() {
        // Field minimum touch target must be at least 56dp for outdoor operations
        assertTrue(NagarlensSpacing.minTouchTarget.value >= 56f)
        assertEquals(4f, NagarlensSpacing.extraSmall.value)
        assertEquals(8f, NagarlensSpacing.small.value)
        assertEquals(16f, NagarlensSpacing.medium.value)
        assertEquals(24f, NagarlensSpacing.large.value)
    }
}
