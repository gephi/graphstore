# GraphStore

[![Build Status](https://travis-ci.org/gephi/graphstore.svg?branch=master)](https://travis-ci.org/gephi/graphstore)
[![Dependency Status](https://www.versioneye.com/user/projects/58924d32d69da500167425f6/badge.svg?style=flat)](https://www.versioneye.com/user/projects/58924d32d69da500167425f6)
[![Coverage Status](https://coveralls.io/repos/gephi/graphstore/badge.svg?branch=master&service=github)](https://coveralls.io/github/gephi/graphstore?branch=master)

GraphStore is an in-memory graph structure implementation written in Java. It is designed to be powerful, efficient and robust. It's powering the Gephi software and supports large graphs in intensive applications.

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

## Download

Stable releases can be found on [Maven central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.gephi%22%20AND%20a%3A%22graphstore%22).

## Documentation

API Documentation is available [here](http://gephi.github.com/graphstore/apidocs/index.html).

## Dependencies

GraphStore depends on FastUtil >= 6.0, Colt 1.2.0 and Joda-Time 2.2.

For a complete list of dependencies, consult the `pom.xml` file.

## Developers

### How to build

GraphStore uses Maven for building. 

	> cd store
	> mvn clean install
		
### How to test

	> mvn test

## How to obtain code coverage report

	> mvn jacoco:report

## Contribute

The source code is available under the Apache 2.0 license. Contributions are welcome.
