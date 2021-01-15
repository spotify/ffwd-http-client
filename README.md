# ffwd-http-client &#187;
[![Build Status](https://github.com/spotify/ffwd-http-client/workflows/JavaCI/badge.svg)](https://github.com/spotify/ffwd-http-client/actions?query=workflow%3AJavaCI)
[![License](https://img.shields.io/github/license/spotify/ffwd.svg)](LICENSE)


This is a java http client for interacting with [ffwd](https://github.com/spotify/ffwd).


# Installation

Add a dependency in Maven. 
```xml
<dependency>
  <groupId>com.spotify.ffwd</groupId>
  <artifactId>ffwd-http-client</artifactId>
  <version>${ffwd-http-client.version}</version>
</dependency>
```

# Releasing

Releasing is done via the `maven-release-plugin` and `nexus-staging-plugin` which are configured via the
`release` [profile](https://github.com/spotify/semantic-metrics/blob/master/pom.xml#L140). Deploys are staged in oss.sonatype.org before being deployed to Maven Central. Check out the [maven-release-plugin docs](http://maven.apache.org/maven-release/maven-release-plugin/) and the [nexus-staging-plugin docs](https://help.sonatype.com/repomanager2) for more information. 

To release, first run: 

``mvn -P release release:prepare``

You will be prompted for the release version and the next development version. On success, follow with:

``mvn -P release release:perform``
