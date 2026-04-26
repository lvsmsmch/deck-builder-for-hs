package com.lvsmsmch.deckbuilder.domain.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ResultTest {

    @Test
    fun `runCatchingResult wraps success`() {
        val r = runCatchingResult { 42 }
        assertTrue(r is Result.Success)
        assertEquals(42, (r as Result.Success).data)
    }

    @Test
    fun `runCatchingResult converts non-cancellation throwables to Error`() {
        val boom = IOException("offline")
        val r = runCatchingResult<Int> { throw boom }
        assertTrue(r is Result.Error)
        assertEquals(boom, (r as Result.Error).throwable)
    }

    @Test
    fun `runCatchingResult rethrows CancellationException instead of swallowing it`() {
        val ex = CancellationException("cancelled")
        var captured: Throwable? = null
        try {
            runCatchingResult<Int> { throw ex }
        } catch (t: Throwable) {
            captured = t
        }
        assertEquals(ex, captured)
    }

    @Test
    fun `cancelling a job propagates instead of producing Result Error`() = runTest {
        // Mirrors CardLibraryViewModel.runSearch: an in-flight coroutine gets cancelled
        // by a newer launch. The cancellation must NOT show up as Result.Error.
        var resultObserved: Result<Int>? = null
        val job = async {
            resultObserved = runCatchingResult<Int> { awaitCancellation() }
        }
        job.cancel()
        // Joining a cancelled async swallows the cancellation here intentionally.
        runCatching { job.await() }
        assertFalse("Cancelled job must not yield Result.Error", resultObserved is Result.Error)
    }
}
