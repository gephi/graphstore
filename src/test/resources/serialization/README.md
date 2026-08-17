# Serialization golden fixtures

Golden files produced by released (or, for the current minor, current) versions of graphstore, used by
`org.gephi.graph.impl.SerializationCompatibilityTest` to guard the on-disk serialization format.

## Layout

One directory per **MINOR** version:

```
serialization/
  0.4/   graph-basic.graphstore, graph-parallel.graphstore
  0.5/   graph-basic.graphstore, graph-parallel.graphstore
  0.6/   graph-basic.graphstore, graph-parallel.graphstore
  0.7/   graph-basic.graphstore, graph-parallel.graphstore
  0.8/   graph-basic, graph-parallel, graph-types-timestamp, graph-types-interval, graph-views
```

Patch versions are not tracked separately: within a minor the format does not change. (0.6.13 and 0.6.14 were
verified byte-identical, so only one copy is kept.)

Every file is a raw `DataOutputStream` dump of `GraphModel.Serialization.write(...)` -- no compression, no wrapper.
Read them back with `GraphModel.Serialization.read(new DataInputStream(...))`.

## The rule this encodes

**A minor bump MAY change the byte format. Older formats MUST remain readable.**

That is exactly what the test enforces:

1. *Backward compatibility* -- every fixture, from `0.4/` up, must deserialize and hold the expected content. This
   applies to all minors, forever.
2. *Format drift* -- for the **current** minor only, serializing the model built by
   `SerializationFixtureGenerator` must reproduce the committed bytes exactly. Older minors are exempt on purpose:
   we no longer write those formats, so pinning their bytes would fail by design.
3. *Determinism* -- the same content must always serialize to the same bytes, independently of the order the model
   was built in.

## Regenerating

Regeneration only ever applies to the current minor (`0.8/`). The legacy directories are historical artifacts and
must never be rewritten -- their whole value is that they were produced by an older release.

```
mvn -q test-compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/test-cp.txt
java -cp "target/classes:target/test-classes:$(cat target/test-cp.txt)" \
    org.gephi.graph.impl.SerializationFixtureGenerator
```

Optionally pass the fixture root as the single argument; it defaults to `src/test/resources/serialization`.

### Regenerating is a deliberate act, not a fix for a red test

A failing byte-pin test means one of two things:

* **a bug** -- something changed the bytes we write without anyone intending to; or
* **an intentional format change** -- in which case it belongs with a version bump (and, if the change is not
  backward compatible on read, a `Serialization.VERSION` bump too), and the fixtures are regenerated as part of that
  change, in the same commit, with the reason written down.

Regenerating to make the test green silently discards the guard: the next reader has no way to tell whether the
format moved on purpose. If you find yourself running the generator because CI is red, stop and work out which of
the two cases you are in first.

When the project moves to a new minor, create a new directory for it, generate its fixtures there, bump
`SerializationFixtureGenerator.CURRENT_MINOR` and add the new minor to `SerializationCompatibilityTest.ALL_MINORS`.
Leave the previous minor's directory in place -- it becomes a legacy read-only fixture from that point on.

## Notes and caveats

* `0.7/` was produced by a **0.7.0-SNAPSHOT** build, not by a released 0.7.0, so it may not match the released
  0.7.0 format exactly.
* The legacy fixtures (0.4 - 0.7) are tiny (~830-890 bytes). They only cover the format *skeleton*: a couple of
  nodes, an edge or two, the default columns, element and text properties. They say nothing about the attribute
  **type surface**. `0.8/` carries that coverage: a column of every supported type, both time representations,
  timestamp and interval maps of each value type, graph attributes, views, edge types, dynamic edge weights.
* Byte-pinned fixtures must stay deterministic. In particular a generic `java.util.HashMap` / `HashSet` must never
  be used as an attribute *value* in a fixture: `Serialization.serializeMap` / `serializeSet` iterate arbitrary maps
  and sets in hash order, and reading them back produces fastutil implementations whose iteration order need not
  match. The `Map` and `Set` *columns* are declared in the fixtures (so the column-type surface is pinned) but left
  with null values; actual `Map` / `Set` values are exercised by the round-trip-only test, which makes no byte
  assertions.
