package com.matiasdev.elecapp.features.clients.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientValidatorTest {
    @Test
    fun `returns errors when required fields are blank`() {
        val result = ClientValidator.validate(fullName = "", phone = " ")

        assertEquals("El nombre es obligatorio", result.fullNameError)
        assertEquals("El teléfono es obligatorio", result.phoneError)
    }

    @Test
    fun `accepts a valid client`() {
        val result = ClientValidator.validate(fullName = "Ana Perez", phone = "1122334455")

        assertTrue(result.isValid)
    }
}
