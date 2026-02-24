package com.example.stackoverflowapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class UserValidationParameterizedTest(
    @Suppress("UNUSED_PARAMETER")
    private val caseName: String,
    private val user: User
) {

    companion object {
        private val jeff = User(1, "Jeff Atwood", 9001, null)

        @JvmStatic
        @Parameters(name = "{0}")
        fun data(): List<Array<Any>> = listOf(
            arrayOf("blank display name", jeff.copy(displayName = "")),
            arrayOf("negative reputation", jeff.copy(reputation = -1)),
            arrayOf("negative id", jeff.copy(id = -1)),
            arrayOf("zero id", jeff.copy(id = 0)),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid user throws`() {
        user.validate()
    }
}

class UserValidationTest {

    @Test
    fun `valid user passes validation`() {
        val user = User(1, "Joel Spolsky", 9001, null)
        assertEquals(user, user.validate())
    }
}