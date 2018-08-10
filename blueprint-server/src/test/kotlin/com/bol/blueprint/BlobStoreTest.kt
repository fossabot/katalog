package com.bol.blueprint

import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.PostgresBlobStore
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Transactional
abstract class AbstractBlobStoreTest {
    protected abstract var blobStore: BlobStore

    @Test
    fun `Can roundtrip blobs`() {
        val bar = byteArrayOf(1, 2, 3)
        val baz = byteArrayOf(4, 5, 6)

        runBlocking {
            blobStore.store(URI.create("/foo/bar"), bar)
            blobStore.store(URI.create("/foo/bar/baz"), baz)

            assertThat(blobStore.get(URI.create("/foo/bar"))).isEqualTo(bar)
            assertThat(blobStore.get(URI.create("/foo/bar/baz"))).isEqualTo(baz)
        }
    }

    @Test
    fun `Can check if blob exists`() {
        runBlocking {
            blobStore.store(URI.create("/foo/bar"), byteArrayOf(4, 5, 6))

            assertThat(blobStore.exists(URI.create("/foo/bar"))).isTrue()
            assertThat(blobStore.exists(URI.create("/unknown"))).isFalse()
        }
    }
}

@RunWith(SpringRunner::class)
@SpringBootTest
class PostgresBlobStoreTest : AbstractBlobStoreTest() {
    override lateinit var blobStore: BlobStore

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun before() {
        blobStore = PostgresBlobStore(jdbcTemplate)
    }
}

class InMemoryBlobStoreTest : AbstractBlobStoreTest() {
    override lateinit var blobStore: BlobStore

    @Before
    fun before() {
        blobStore = InMemoryBlobStore()
    }
}