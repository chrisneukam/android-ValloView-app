package de.codingsolutions.valloview

import de.codingsolutions.valloview.util.ServiceVerifier
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceVerifierTest {
    @Test
    fun testValidIps() {
        assertTrue(ServiceVerifier.isValidIp("192.168.1.1"))
        assertTrue(ServiceVerifier.isValidIp("127.0.0.1"))
        assertTrue(ServiceVerifier.isValidIp("255.255.255.255"))
        assertTrue(ServiceVerifier.isValidIp("0.0.0.0"))
    }

    @Test
    fun testInvalidIps() {
        assertFalse(ServiceVerifier.isValidIp("256.256.256.256"))
        assertFalse(ServiceVerifier.isValidIp("192.168.1"))
        assertFalse(ServiceVerifier.isValidIp("192.168.1.256"))
        assertFalse(ServiceVerifier.isValidIp("abc.def.ghi.jkl"))
        assertFalse(ServiceVerifier.isValidIp(""))
        assertFalse(ServiceVerifier.isValidIp("192.168.1.1.1"))
    }
}
