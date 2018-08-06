# Overview

## Goal
The goal of Blueprint is to be an API registry, with the following initial goals:

1. Provide a basic registry for Swagger 2 schemas
1. Provide a basic registry for JSON schemas
1. Allow schema validation against guidelines

## Structure
Schemas are stored in the following hierarchy:

1. Namespace
1. Schema name
1. Version
1. Artifact(s)

#### Schema
A schema also defines a `type`, which defines:
1. Which format is the schema (Swagger 2, JSON schema, OpenAPI 3, etc)
1. What kind of versioning does it use: Maven-style? Npm-style? It defines how we determine compatible version ranges, how we determine if a version is a 'SNAPSHOT', etc

A schema may also store arbitrary metadata. TBD: Arbitrary JSON, or do we want to apply semantic meaning to the values stored here?

#### Version
A version contains information about:
1. Is it published (i.e. generally available)?
1. is it deprecated?
1. When was it first published and last updated?

## Integrating in CI/CD pipelines
We want to be able to push artifacts to Blueprint during a build process. Validation should happen at this stage so builds can potentially be failed if the quality gate is not met.

Initially this can happen as a Maven plugin, built on top of a JVM client.

## Querying
We want to be able to look up:

1. All namespaces
1. All schemas in a namespace
1. All versions for a schema
1. All artifacts for a specific version
1. A specific artifact

Additionally:
1. All schemas (across namespaces) that are of a certain type
1. All schemas (across namespaces) that have certain metadata

## Technology
Kotlin backend, Angular frontend.

We want to be able to run Blueprint "locally" in two ways: For testing (using an in-memory stack) and in an "on-premise" kind of way (using a database and a message queue).

Additionally, we want to be able to run on Google Cloud.

Auditing should be built-in. Therefore the database should probably consist of two entities only:
1. Events happening on a certain namespace (and schema)
1. The artifacts themselves

The last snapshot of the events for a namespace (+ schema) can be cached in-memory for quick performance.

For now we will focus on an MVP, so we'll keep extension points to a minimum (e.g. no pluggable schema types, initially). Once we have a feeling for how everything works we can extract a plugin API.

### Technology suggestions
* Local: JMS through embedded ActiveMQ and jOOQ + embedded H2
* GCP: PubSub and Cloud Datastore
* On-Premise (low priority): Anything supported by JMS and anything supported by jOOQ?
