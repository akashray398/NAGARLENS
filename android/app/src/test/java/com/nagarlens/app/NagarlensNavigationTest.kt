package com.nagarlens.app

import com.nagarlens.app.core.navigation.NagarlensDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NagarlensNavigationTest {

    @Test
    fun `navigation destinations contain all five primary screens`() {
        val topLevel = NagarlensDestination.topLevelDestinations
        assertEquals(5, topLevel.size)

        assertEquals(NagarlensDestination.HOME, NagarlensDestination.fromRoute("home"))
        assertEquals(NagarlensDestination.MAP, NagarlensDestination.fromRoute("map"))
        assertEquals(NagarlensDestination.CAPTURE, NagarlensDestination.fromRoute("capture"))
        assertEquals(NagarlensDestination.ACTIVITY, NagarlensDestination.fromRoute("activity"))
        assertEquals(NagarlensDestination.PROFILE, NagarlensDestination.fromRoute("profile"))
    }

    @Test
    fun `unknown route defaults to home destination`() {
        val destination = NagarlensDestination.fromRoute("unknown_route_xyz")
        assertEquals(NagarlensDestination.HOME, destination)
    }

    @Test
    fun `destinations have icons and non-empty titles`() {
        NagarlensDestination.entries.forEach { destination ->
            assertNotNull(destination.icon)
            assert(destination.title.isNotEmpty())
            assert(destination.route.isNotEmpty())
        }
    }
}
