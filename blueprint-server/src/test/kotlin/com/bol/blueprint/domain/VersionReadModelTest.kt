package com.bol.blueprint.domain

import com.bol.blueprint.TestData
import com.bol.blueprint.TestUsers
import com.bol.blueprint.applyBasicTestSet
import com.bol.blueprint.cqrs.CommandPublisher
import com.bol.blueprint.domain.readmodels.SchemaReadModel
import com.bol.blueprint.domain.readmodels.VersionReadModel
import com.bol.blueprint.store.InMemoryBlobStore
import com.bol.blueprint.store.InMemoryEventStore
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.util.*

class VersionReadModelTest {
    private lateinit var commandHandler: CommandHandler
    private val schemas = SchemaReadModel()
    private val versions = VersionReadModel(schemas)
    private val eventStore = InMemoryEventStore()
    private val blobStore = InMemoryBlobStore()

    @Before
    fun before() {
        val publisher = CommandPublisher(eventStore, TestUsers.user(), emptyList(), TestData.clock)
        val initialHandler = CommandHandler(publisher, blobStore)
        runBlocking { initialHandler.applyBasicTestSet() }

        // Replay the events from the event store into 'query'
        val replayPublisher = CommandPublisher(eventStore, TestUsers.user(), listOf(schemas, versions), TestData.clock)
        commandHandler = CommandHandler(replayPublisher, blobStore)
        runBlocking { replayPublisher.replayFromStore() }
    }

    @Test
    fun `Can register versions`() {
        expectThat(versions.getVersions(TestData.ns1_schema1)).containsExactly(
            Version(
                TestData.ns1_schema1_v100,
                TestData.clock.instant(),
                Semver("1.0.0", Semver.SemverType.IVY)
            ),
            Version(
                TestData.ns1_schema1_v101,
                TestData.clock.instant(),
                Semver("1.0.1", Semver.SemverType.IVY)
            ),
            Version(
                TestData.ns1_schema1_v200snapshot,
                TestData.clock.instant(),
                Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY)
            )
        )

        expectThat(versions.getVersions(TestData.ns2_schema3)).containsExactly(
            Version(
                TestData.ns2_schema3_v100,
                TestData.clock.instant(),
                Semver("1.0.0", Semver.SemverType.IVY)
            )
        )
    }

    @Test
    fun `Can find schemas of versions`() {
        expectThat(versions.getVersions(TestData.ns1_schema1).map { versions.getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns1_schema1
        )
        expectThat(versions.getVersions(TestData.ns2_schema3).map { versions.getVersionSchemaId(it.id) }.distinct().single()).isEqualTo(
            TestData.ns2_schema3
        )
    }

    @Test
    fun `Can delete version`() {
        val version = versions.getVersion(TestData.ns1_schema1_v100)!!

        runBlocking {
            commandHandler.deleteVersion(TestData.ns1_schema1_v100)
        }

        expectThat(versions.getVersions(TestData.ns1_schema1)).containsExactly(
            Version(
                TestData.ns1_schema1_v101,
                TestData.clock.instant(),
                Semver("1.0.1", Semver.SemverType.IVY)
            ),
            Version(
                TestData.ns1_schema1_v200snapshot,
                TestData.clock.instant(),
                Semver("2.0.0-SNAPSHOT", Semver.SemverType.IVY)
            )
        )

        expectThat(versions.getVersionSchemaId(version.id)).isNull()
    }

    @Test
    fun `Can get current major versions`() {
        val result = versions.getCurrentMajorVersions(listOf(v("1.0.0"), v("1.0.1"), v("2.0.0-SNAPSHOT")))
        expectThat(result.map { it.semVer.value }).containsExactly("2.0.0-SNAPSHOT", "1.0.1")
    }

    private fun v(version: String) =
        Version(
            UUID.randomUUID(),
            TestData.clock.instant(),
            Semver(version, Semver.SemverType.IVY)
        )
}