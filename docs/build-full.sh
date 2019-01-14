#!/usr/bin/env bash

# Install Gulp and Antora
yarn global add gulp @antora/cli @antora/site-generator-default

cd ui

    # Install UI dependencies
    yarn install

    # Build UI
    gulp pack

cd ..

# Build site
antora site.yml

# Disable jekyll on Github Pages
touch generated/.nojekyll
