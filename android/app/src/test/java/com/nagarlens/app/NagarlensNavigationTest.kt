package com.nagarlens.app

import com.nagarlens.app.core.navigation.NagarlensDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NagarlensNavigationTest {

    @Test
    fun `navigation destinations contain top level destinations`() {
        val topLevel = NagarlensDestination.topLevelDestinations
        assertTrue(topLevel.contains(NagarlensDestination.HOME))
        assertTrue(topLevel.contains(NagarlensDestination.MAP))
        assertTrue(topLevel.contains(NagarlensDestination.ASSETS))
        assertTrue(topLevel.contains(NagarlensDestination.CAPTURE))
        assertTrue(topLevel.contains(NagarlensDestination.INCIDENTS))

        assertEquals(NagarlensDestination.HOME, NagarlensDestination.fromRoute("home"))
        assertEquals(NagarlensDestination.MAP, NagarlensDestination.fromRoute("map"))
        assertEquals(NagarlensDestination.ASSETS, NagarlensDestination.fromRoute("assets"))
        assertEquals(NagarlensDestination.CAPTURE, NagarlensDestination.fromRoute("capture"))
        assertEquals(NagarlensDestination.INCIDENTS, NagarlensDestination.fromRoute("incidents"))
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
