<!-- mdformat off(GitHub header) -->
Blueprint
[![Build Status](https://travis-ci.org/bolcom/blueprint.svg?branch=master)](https://travis-ci.org/bolcom/blueprint)
======
<!-- mdformat on -->

<p align="center">
  <img src="docs/assets/blueprint.svg" alt="Blueprint Logo" />
</p>

Nothing to see here, yet.

### To run locally
1. `./mvnw clean install`
1. `java -jar target/blueprint-app*.jar`
1. open [`localhost:8080`](http://localhost:8080)

### To run locally with some plugins
1. `./mvnw clean install`
1. `java -Dloader.path=target/blueprint-plugin-postgres*.jar,... -jar target/blueprint-app*.jar`
1. open [`localhost:8080`](http://localhost:8080)

### To do UI development
1. `brew install node yarn`
1. `yarn global add @angular/cli`
1. `cd blueprint-ui/src/main/frontend`
1. `ng serve`
1. open [`localhost:4200`](http://localhost:4200) while the server on 8080 is still running (API requests will be proxied)
