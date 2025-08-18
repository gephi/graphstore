# GraphStore

[![build](https://github.com/gephi/graphstore/actions/workflows/ci.yml/badge.svg)](https://github.com/gephi/graphstore/actions/workflows/ci.yml)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.gephi/graphstore.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.gephi/graphstore)
[![Coverage Status](https://coveralls.io/repos/gephi/graphstore/badge.svg?branch=master&service=github)](https://coveralls.io/github/gephi/graphstore?branch=master)

GraphStore is an in-memory graph structure implementation written in Java. It's designed to be powerful, efficient and robust. It's powering the Gephi software and supports large graphs in intensive applications.

## Features Highlight

* Blazing fast graph data structure optimized for reading and writing
* Comprehensive APIs to read and modify the graph structure
* Low memory footprint - reduced usage of Java objects and collections optimized for caching
* Supports directed, undirected and mixed graphs
* Supports parallel edges (i.e. edges can have a label)
* Any number of attributes can be associated with nodes or edges
* Thread-safe - Implements read-write locking mechanism to allow multiple reading threads
* Supports dynamic graphs (graphs over time)
* Built-in index on attribute values
* Fast and compact binary serialization
* Spatial indexing based on a quadtree

## Download

Stable releases can be found on [Maven central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.gephi%22%20AND%20a%3A%22graphstore%22).

Development builds can be found on [Sonatype's Snapshot Repository](https://oss.sonatype.org/content/repositories/snapshots/org/gephi/graphstore/).

## Documentation

API Documentation is available [here](https://www.javadoc.io/doc/org.gephi/graphstore/latest/index.html).

Follow [this QuickStart](https://github.com/gephi/graphstore/wiki/Quick-Start) to get started.

## Usage

### From a Maven project

```xml
<dependency>
    <groupId>org.gephi</groupId>
    <artifactId>graphstore</artifactId>
    <version>0.7.3</version>
</dependency>
```

### From a Gradle project

```
compile 'org.gephi:graphstore:0.7.3'
```

## Dependencies

GraphStore is built for JRE 11+ and depends on FastUtil and Colt.

For a complete list of dependencies, consult the `pom.xml` file.

## Developers

### How to build

GraphStore uses Maven for building. 

	> mvn clean install

Note that code formatting is automatically applied at that time.
		
### How to test

	> mvn test

## How to obtain code coverage report

	> mvn jacoco:report

## Contribute

The source code is available under the Apache 2.0 license. Contributions are welcome.
