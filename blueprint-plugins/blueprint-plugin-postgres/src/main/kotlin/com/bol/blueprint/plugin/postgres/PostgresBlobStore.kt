package com.bol.blueprint.plugin.postgres

import com.bol.blueprint.store.BlobStore
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import java.net.URI

class PostgresBlobStore(private val jdbcTemplate: JdbcTemplate) : BlobStore {
    override suspend fun delete(path: URI) {
        jdbcTemplate.update("delete from blobs where path = ?", convertPath(path))
    }

    override suspend fun exists(path: URI): Boolean {
        return jdbcTemplate.queryForObject("select exists(select 1 from blobs where path = ?)", arrayOf(convertPath(path)), Boolean::class.java)
    }

    override suspend fun get(path: URI): ByteArray? {
        return jdbcTemplate.queryForObject("select contents from blobs where path = ?", convertPath(path)) { rs, _ ->
            return@queryForObject rs.getBinaryStream(1).readBytes()
        }
    }

    override suspend fun store(path: URI, data: ByteArray) {
        jdbcTemplate.update(
            "insert into blobs (path, contents) values (?, ?)",
            convertPath(path), data
        )
    }

    private fun convertPath(path: URI) = path.path
}