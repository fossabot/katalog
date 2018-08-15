package com.bol.blueprint.plugins.postgres

import com.bol.blueprint.store.BlobStore
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class PostgresBlobStoreIT {
    @Autowired
    private lateinit var blobStore: BlobStore

    @Test
    fun `Can roundtrip blobs`() {
        val bar = byteArrayOf(1, 2, 3)
        val baz = byteArrayOf(4, 5, 6)

        runBlocking {
            blobStore.store(URI.create("/foo/bar"), bar)
            blobStore.store(URI.create("/foo/bar/baz"), baz)

            Assertions.assertThat(blobStore.get(URI.create("/foo/bar"))).isEqualTo(bar)
            Assertions.assertThat(blobStore.get(URI.create("/foo/bar/baz"))).isEqualTo(baz)
        }
    }

    @Test
    fun `Can check if blob exists`() {
        runBlocking {
            blobStore.store(URI.create("/foo/bar"), byteArrayOf(4, 5, 6))

            Assertions.assertThat(blobStore.exists(URI.create("/foo/bar"))).isTrue()
            Assertions.assertThat(blobStore.exists(URI.create("/unknown"))).isFalse()
        }
    }

}