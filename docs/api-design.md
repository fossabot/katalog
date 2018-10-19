# REST API design

There are 4 levels of resources:

- `Namespaces` can contain multiple `Schemas`
- `Schemas` can contain multiple `Versions`
- `Versions` can contain multiple `Artifacts`
- An `Artifact` is a single blob

Every level is modelled as a separate top-level resource.

### Querying namespaces
`GET /api/v1/namespaces`

This will simply return a list of namespaces. This would work the same as the current API.

However, things get more interesting once we go down to the `schema` level:

### Querying schemas
`GET /api/v1/schemas`

This will return a list of *all* schemas. Normally, we'd filter them on namespaces, like so:

`GET /api/v1/schemas?ns=ns1,ns2,ns3`

This allows us to add more interesting subqueries: For instance, getting only the most recent major versions for a schema could be done through a `GET /api/v1/versions/major` call.

### Identifiers
Resources are identified with a GUID. This GUID can be used to retrieve, delete or update individual resources.

### Benefits
A UI example: Showing a simple overview of namespaces, their schemas and the most recent version per schema.

This would work out to this set of queries:

* `GET /api/v1/namespaces`
* `GET /api/v1/schemas?ns={the result of the namespaces call}`
* `GET /api/v1/versions/major?ns={the result of the schemas call}`

## Accessing an artifact directly

For artifacts we provide a `repository` URL which is not a complete REST API but simply allows direct access to an artifact.

It looks like: `GET /api/v1/repository/{namespace}/{schema}/{version}/{artifact}`

This can also support things like redirects if a namespace was renamed, etc.