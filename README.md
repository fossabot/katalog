<!-- mdformat off(GitHub header) -->
Katalog
[![Build Status](https://travis-ci.org/bolcom/katalog.svg?branch=master)](https://travis-ci.org/bolcom/katalog)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.bol.katalog%3Akatalog-parent&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.bol.katalog%3Akatalog-parent)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fbolcom%2Fkatalog.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fbolcom%2Fkatalog?ref=badge_shield)
======
<!-- mdformat on -->

<p align="center">
  <img src="katalog.svg" alt="Katalog Logo" />
</p>

Nothing to see here, yet.

### To run locally
1. `./mvnw clean install`
1. `java -jar target/katalog-app*.jar`
1. open [`localhost:8080`](http://localhost:8080)

### To run locally with some plugins
1. `./mvnw clean install`
1. `java -Dloader.path=target/katalog-plugin-postgres*.jar,... -jar target/katalog-app*.jar`
1. open [`localhost:8080`](http://localhost:8080)

### To do UI development
1. `brew install node yarn`
1. `yarn global add @angular/cli`
1. `cd katalog-ui/src/main/frontend`
1. `ng serve`
1. open [`localhost:4200`](http://localhost:4200) while the server on 8080 is still running (API requests will be proxied)

### Credentials
Use `user`/`user` or `admin`/`admin`.


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fbolcom%2Fkatalog.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fbolcom%2Fkatalog?ref=badge_large)