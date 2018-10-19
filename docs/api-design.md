# REST API design

There are 4 levels of resources:

- `Namespaces` can contain multiple `Schemas`
- `Schemas` can contain multiple `Versions`
- `Versions` can contain multiple `Artifacts`
- An `Artifact` is a single blob

## Current POC API
The current POC API treats this as a single hierachy, e.g.:

`GET /api/v1/namespaces/{namespace}/schemas/{schema}/versions/{version}/artifacts/my-api-swagger.json`

This works, but it is quite limiting in the way we can structure our resources.

For instance, having any kind of sub-resource under `/api/v1/namespaces` is impossible since it is assumed that anything after `/namespaces` is the actual *name* of a namespace.

Additionally, in the UI just showing a simple overview of namespaces and their schemas and the most recent version already requires either a UI-specific API endpoint or doing a LOT of API calls. The current API also doesn't lend itself to batching queries at all.

## Alternative design
An alternative design, based on thoughts by @breun:

### Querying namespaces
`GET /api/v1/namespaces`

This will simply return a list of namespaces. This would work the same as the current API.

However, things get more interesting once we go down to the `schema` level:

### Querying schemas
`GET /api/v1/schemas`

This will return a list of *all* schemas. Normally, we'd filter them on namespaces, like so:

`GET /api/v1/schemas?ns=ns1,ns2,ns3`

This filtering on multiple namespaces at once was not possible in the previous API design.

For versions and artifacts the same thing would apply: They become top-level resources.

This allows us to add more interesting subqueries: For instance, getting only the most recent major versions for a schema could be done through a `GET /api/v1/versions/major` call.

## Benefits
This new design would hopefully obviate the need for a UI-specific API.

To go back to the original UI example: Showing a simple overview of namespaces, their schemas and the most recent version per schema.

This would work out to this set of queries:

* `GET /api/v1/namespaces`
* `GET /api/v1/schemas?ns={the result of the namespaces call}`
* `GET /api/v1/versions/major?ns={the result of the schemas call}`

The API becomes more expressive and we can avoid creating UI-specific API's.