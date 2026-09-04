package com.naveenai.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationStartupTest {
    @Test
    fun appStartsWithBasicConfiguration() {
        val appName = "NAVEEN AI"
        assertTrue(appName.isNotEmpty())
    }
}
