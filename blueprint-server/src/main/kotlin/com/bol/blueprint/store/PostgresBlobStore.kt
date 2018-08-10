package com.bol.blueprint.store

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.net.URI

class PostgresBlobStore(
    @Autowired val jdbcTemplate: JdbcTemplate
) : BlobStore {
    override suspend fun exists(path: URI): Boolean {
        return jdbcTemplate.queryForObject("select exists(select 1 from blobs where path = ?)", arrayOf(convertPath(path)), Boolean::class.java)
    }

    override suspend fun get(path: URI): ByteArray? {
        return jdbcTemplate.queryForObject("select contents from blobs where path = ?", arrayOf(convertPath(path))) { rs, _ ->
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