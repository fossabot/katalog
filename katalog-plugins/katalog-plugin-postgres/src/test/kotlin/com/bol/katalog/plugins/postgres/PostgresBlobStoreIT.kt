package com.bol.katalog.plugins.postgres

import com.bol.katalog.store.BlobStore
import com.bol.katalog.testing.store.AbstractBlobStoreTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest
@Transactional
class PostgresBlobStoreIT : AbstractBlobStoreTest() {
    @Autowired
    private lateinit var blobStore: BlobStore

    @Test
    fun `Can roundtrip blobs`() {
        canRoundtripBlobs(blobStore)
    }

    @Test
    fun `Can check if blob exists`() {
        canCheckIfBlobExists(blobStore)
    }
}