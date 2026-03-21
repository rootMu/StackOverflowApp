package com.example.stackoverflowapp.ui.navigation

import com.example.stackoverflowapp.ui.details.UserDetailsDestination
import com.example.stackoverflowapp.ui.home.HomeDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class DestinationsTest {

    @Test
    fun homeDestination_hasCorrectRoute() {
        assertEquals("home", HomeDestination.route)
    }

    @Test
    fun userDetailsDestination_hasCorrectRoutePattern() {
        assertEquals("details/{userId}", UserDetailsDestination.route)
    }

    @Test
    fun userDetailsDestination_createsCorrectRouteWithId() {
        val route = UserDetailsDestination.createRoute(42)
        assertEquals("details/42", route)
    }
}
