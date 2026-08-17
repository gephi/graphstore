# Serialization golden fixtures

Golden files used by `org.gephi.graph.impl.SerializationCompatibilityTest` to guard the on-disk serialization format.

## Layout

One directory per minor version:

```
serialization/
  0.4/   graph-basic, graph-parallel
  0.5/   graph-basic, graph-parallel
  0.6/   graph-basic, graph-parallel
  0.7/   graph-basic, graph-parallel
  0.8/   graph-basic, graph-parallel, graph-types-timestamp, graph-types-interval, graph-views
```

Patch versions are not tracked separately; the format is stable within a minor. 0.6.13 and 0.6.14 are byte-identical,
so 0.6 holds one copy.

Each file is a raw `GraphModel.Serialization.write(...)` dump over a `DataOutputStream`, with no compression. Read them
with `GraphModel.Serialization.read(new DataInputStream(...))`.

## Contract

A minor bump may change the byte format. Older formats must remain readable.

1. Every fixture from 0.4 up deserializes and holds the expected content.
2. For the current minor, serializing the model built by `SerializationFixtureGenerator` reproduces the committed
   bytes. Older minors are exempt, since graphstore no longer writes those formats.
3. The same content serializes to the same bytes regardless of build order.

## Regenerating

Regeneration applies to the current minor only. The legacy directories are historical artifacts and are never
rewritten.

```
mvn -q test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/test-cp.txt
java -cp "target/classes:target/test-classes:$(cat target/test-cp.txt)" \
    org.gephi.graph.impl.SerializationFixtureGenerator
```

The fixture root defaults to `src/test/resources/serialization` and can be passed as the single argument.

A failing byte-pin means either a bug or an intended format change. An intended change is committed together with the
regenerated fixtures, plus a `Serialization.VERSION` bump if it breaks read compatibility.

When the project moves to a new minor: create its directory, generate its fixtures, bump
`SerializationFixtureGenerator.CURRENT_MINOR`, and add the minor to `SerializationCompatibilityTest.ALL_MINORS`.

## Notes

* `0.7/` came from a 0.7.0-SNAPSHOT build and may differ from released 0.7.0.
* The 0.4 to 0.7 fixtures are 830-890 bytes and cover the format skeleton only: a few nodes and edges, the default
  columns, element and text properties. `0.8/` covers the attribute type surface: a column of every supported type,
  both time representations, timestamp and interval maps of each value type, graph attributes, views, edge types and
  dynamic edge weights.
* A generic `HashMap` or `HashSet` cannot be used as an attribute value in a byte-pinned fixture.
  `Serialization.serializeMap` and `serializeSet` iterate in hash order, and reading them back yields fastutil types
  with a different order. The `Map` and `Set` columns are declared with null values; real values are covered by the
  round-trip-only test.
