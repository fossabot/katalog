package com.bol.katalog.plugin.gcp

import com.bol.katalog.store.BlobStore
import com.bol.katalog.testing.store.AbstractBlobStoreTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URI

@Disabled("Need to set up a new GCP project for testing purposes")
@ExtendWith(SpringExtension::class)
@SpringBootTest
class GcpBlobStoreIT : AbstractBlobStoreTest() {
    @Autowired
    private lateinit var blobStore: BlobStore

    @AfterEach
    fun after() {
        runBlocking {
            blobStore.deleteIfExists(URI.create("foo/bar"))
            blobStore.deleteIfExists(URI.create("foo/bar/baz"))
            blobStore.deleteIfExists(URI.create("foo"))
        }
    }

    @Test
    fun `Can roundtrip blobs`() {
        canRoundtripBlobs(blobStore)
    }

    @Test
    fun `Can check if blob exists`() {
        canCheckIfBlobExists(blobStore)
    }
}