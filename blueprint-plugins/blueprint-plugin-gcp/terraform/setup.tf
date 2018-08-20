variable "BLUEPRINT_GOOGLE_PROJECT" {}

# Setup project
provider "google" {
  project = "${var.BLUEPRINT_GOOGLE_PROJECT}"
}

# Store terraform state
terraform {
  backend "gcs" {
    bucket = "bolcom-blueprint-terraform"
    prefix = "terraform/state"
  }
}

# Create service account
resource "google_service_account" "travis-ci" {
  account_id   = "travis-ci"
  display_name = "Travis CI integration testing"
}

# Create test bucket
resource "google_storage_bucket" "test-bucket" {
  name     = "bolcom-blueprint-test-bucket"
  location = "EU"
}

# Apply rights to service account for test bucket
resource "google_storage_bucket_iam_binding" "binding" {
  bucket = "${google_storage_bucket.test-bucket.name}"
  role   = "roles/storage.objectAdmin"

  members = [
    "serviceAccount:${google_service_account.travis-ci.email}",
  ]
}

# Apply datastore rights
resource "google_project_iam_member" "datastore_user" {
  role = "roles/datastore.user"
  member = "serviceAccount:${google_service_account.travis-ci.email}"
}