package com.bol.katalog.testing.store

import com.bol.katalog.store.inmemory.InMemoryBlobStore
import org.junit.jupiter.api.Test

class InMemoryBlobStoreTest : AbstractBlobStoreTest() {
    @Test
    fun `Can roundtrip blobs`() {
        canRoundtripBlobs(InMemoryBlobStore())
    }

    @Test
    fun `Can check if blob exists`() {
        canCheckIfBlobExists(InMemoryBlobStore())
    }
}