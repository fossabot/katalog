#!/bin/bash

# Find available plugins
for plugin in `ls plugins`; do
  echo Found plugin: $plugin
  plugins+=plugins/$plugin,
done

# Launch!
exec java -Dloader.path=$plugins -Djava.security.egd=file:/dev/./urandom -jar app.jar
