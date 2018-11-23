package com.bol.katalog.plugin.gcp

import com.bol.katalog.store.BlobStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.contentEquals
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest
class GcpBlobStoreIT {
    @Autowired
    private lateinit var blobStore: BlobStore

    @After
    fun after() {
        runBlocking {
            blobStore.deleteIfExists(URI.create("foo/bar"))
            blobStore.deleteIfExists(URI.create("foo/bar/baz"))
            blobStore.deleteIfExists(URI.create("foo"))
        }
    }

    @Test
    fun `Can roundtrip blobs`() {
        val bar = byteArrayOf(1, 2, 3)
        val baz = byteArrayOf(4, 5, 6)

        runBlocking {
            blobStore.store(URI.create("foo/bar"), bar)
            blobStore.store(URI.create("foo/bar/baz"), baz)

            expectThat(blobStore.get(URI.create("foo/bar"))).isNotNull().contentEquals(bar)
            expectThat(blobStore.get(URI.create("foo/bar/baz"))).isNotNull().contentEquals(baz)
        }
    }

    @Test
    fun `Can check if blob exists`() {
        runBlocking {
            blobStore.store(URI.create("foo/bar"), byteArrayOf(4, 5, 6))

            expectThat(blobStore.exists(URI.create("foo/bar"))).isTrue()
            expectThat(blobStore.exists(URI.create("unknown"))).isFalse()
        }
    }
}