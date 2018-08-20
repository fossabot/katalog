This plugin adds support for Google Cloud (GCP).

### Setting up GCP
To hack on this plugin, you will need to set up your own GCP resources:

1. `cd terraform`
1. `export TF_VAR_BLUEPRINT_GOOGLE_PROJECT=[your google project name]` 
1. `terraform apply`

### GCP Datastore
For the blob store you will need to set up a "Firestore in Datastore mode" database manually, since Terraform does not support this yet.

Alternatively, you can run the [Datastore emulator](https://cloud.google.com/datastore/docs/tools/datastore-emulator).