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

Regeneration applies to the current version only. The legacy directories are historical artifacts and are never
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

Fixing a bug in the state being written also moves the bytes without changing the layout. The 0.8 fixtures were
regenerated once for this reason, when dynamic attributes stopped inflating the time index reference counts.

When the project moves to a new minor: create its directory, generate its fixtures, bump
`SerializationFixtureGenerator.CURRENT_MINOR`, and add the minor to `SerializationCompatibilityTest.ALL_MINORS`.