# GraphStore

GraphStore is an in-memory graph structure implementation written in Java. It is designed to be powerful, efficient and robust. It's powering the Gephi software and supports large graphs in intensive applications.

## Features Highlight

* Blazing fast graph data structure optimized for reading and writing. 
* Comprehensive APIs to read and modify the graph structure.
* Low memory footprint. Reduced usage of Java objects and collections optimized for caching.
* Supports directed, undirected and mixed graphs
* Supports parallel edges. Edges can have a label.
* Any number of attributes can be associated with nodes or edges.
* Thread-safe. Implements read-write locking mechanism to allow multiple reading threads.
* Supports dynamic graphs (graphs over time).
* Built-in index on attribute values.
* Fast and compact binary serialization.

## Download

No stable version has been released yet. You can download the development version:

[graphstore-0.1-SNAPSHOT.jar](http://nexus.gephi.org/nexus/service/local/artifact/maven/content?r=snapshots&g=org.gephi&a=graphstore&v=0.1-SNAPSHOT&p=jar&c=jar)

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

## Contribute

The source code is available under the Apache 2.0 license. Contributions are welcome.
