package org.gephi.graph.impl;

import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.chars.Char2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.gephi.graph.api.Element;

public abstract class ColumnIndex<K, T extends Element> implements Iterable<Map.Entry<K, ? extends Set<T>>> {

    // Const
    public static final boolean TRIMMING_ENABLED = false;
    public static final int TRIMMING_FREQUENCY = 30;
    // Data
    protected final ColumnImpl column;
    protected final ValueSet<K, T> nullSet;
    protected Map<K, ValueSet<K, T>> map;
    // Variable
    protected int elements;

    public ColumnIndex(ColumnImpl column) {
        this.column = column;
        this.nullSet = new ValueSet<>(null);
    }

    public K putValue(T element, K value) {
        if (value == null) {
            if (nullSet.add(element)) {
                elements++;
            }
        } else {
            ValueSet<K, T> set = getValueSet(value);
            if (set == null) {
                set = addValue(value);
            }
            value = set.value;

            if (set.add(element)) {
                elements++;
            }
        }
        return value;
    }

    public void removeValue(T element, K value) {
        if (value == null) {
            if (nullSet.remove(element)) {
                elements--;
            }
        } else {
            ValueSet<K, T> set = getValueSet(value);
            if (set.remove(element)) {
                elements--;
            }
            if (set.isEmpty()) {
                removeValue(value);
            }
        }
    }

    public K replaceValue(T element, K oldValue, K newValue) {
        removeValue(element, oldValue);
        return putValue(element, newValue);
    }

    public int getCount(K value) {
        if (value == null) {
            return nullSet.size();
        }
        ValueSet<K, T> valueSet = getValueSet(value);
        if (valueSet != null) {
            return valueSet.size();
        } else {
            return 0;
        }
    }

    public Collection values() {
        return new WithNullDecorator();
    }

    public int countValues() {
        return (nullSet.isEmpty() ? 0 : 1) + map.size();
    }

    public Number getMinValue() {
        if (isSortable()) {
            if (map.isEmpty()) {
                return null;
            } else {
                return (Number) ((SortedMap) map).firstKey();
            }
        } else {
            throw new UnsupportedOperationException("'" + column.getId() + "' is not a sortable column (" + column
                    .getTypeClass().getSimpleName() + ").");
        }
    }

    public Number getMaxValue() {
        if (isSortable()) {
            if (map.isEmpty()) {
                return null;
            } else {
                return (Number) ((SortedMap) map).lastKey();
            }
        } else {
            throw new UnsupportedOperationException("'" + column.getId() + "' is not a sortable column (" + column
                    .getTypeClass().getSimpleName() + ").");
        }
    }

    protected void destroy() {
        map = null;
        nullSet.clear();
        elements = 0;
    }

    protected void clear() {
        map.clear();
        nullSet.clear();
        elements = 0;
    }

    @Override
    public Iterator<Map.Entry<K, ? extends Set<T>>> iterator() {
        return new EntryIterator();
    }

    protected ValueSet<K, T> getValueSet(K value) {
        if (value == null) {
            return nullSet;
        }
        return map.get(value);
    }

    protected void removeValue(K value) {
        map.remove(value);
    }

    protected ValueSet<K, T> addValue(K value) {
        ValueSet<K, T> valueSet = new ValueSet<>(value);
        map.put(value, valueSet);
        return valueSet;
    }

    protected boolean isSortable() {
        return Number.class.isAssignableFrom(column.getTypeClass()) && map instanceof SortedMap;
    }

    protected static class DefaultIndex<T extends Element> extends ColumnIndex<Object, T> {

        public DefaultIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenHashMap<>();
        }
    }

    protected static class BooleanIndex<T extends Element> extends ColumnIndex<Boolean, T> {

        public BooleanIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenHashMap<>();
        }
    }

    protected static class DoubleIndex<T extends Element> extends ColumnIndex<Double, T> {

        public DoubleIndex(ColumnImpl column) {
            super(column);

            map = new Double2ObjectAVLTreeMap<>();
        }
    }

    protected static class IntegerIndex<T extends Element> extends ColumnIndex<Integer, T> {

        public IntegerIndex(ColumnImpl column) {
            super(column);

            map = new Int2ObjectAVLTreeMap<>();
        }
    }

    protected static class FloatIndex<T extends Element> extends ColumnIndex<Float, T> {

        public FloatIndex(ColumnImpl column) {
            super(column);

            map = new Float2ObjectAVLTreeMap<>();
        }
    }

    protected static class LongIndex<T extends Element> extends ColumnIndex<Long, T> {

        public LongIndex(ColumnImpl column) {
            super(column);

            map = new Long2ObjectAVLTreeMap<>();
        }
    }

    protected static class ShortIndex<T extends Element> extends ColumnIndex<Short, T> {

        public ShortIndex(ColumnImpl column) {
            super(column);

            map = new Short2ObjectAVLTreeMap<>();
        }
    }

    protected static class ByteIndex<T extends Element> extends ColumnIndex<Byte, T> {

        public ByteIndex(ColumnImpl column) {
            super(column);

            map = new Byte2ObjectAVLTreeMap<>();
        }
    }

    protected static class GenericNumberIndex<T extends Element> extends ColumnIndex<Number, T> {

        public GenericNumberIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectAVLTreeMap<>();
        }
    }

    protected static class CharIndex<T extends Element> extends ColumnIndex<Character, T> {

        public CharIndex(ColumnImpl column) {
            super(column);

            map = new Char2ObjectAVLTreeMap<>();
        }
    }

    protected static class DefaultArrayIndex<T extends Element> extends ColumnIndex<Object[], T> {

        public DefaultArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(ObjectArrays.HASH_STRATEGY);
        }
    }

    protected static class BooleanArrayIndex<T extends Element> extends ColumnIndex<boolean[], T> {

        public BooleanArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(BooleanArrays.HASH_STRATEGY);
        }
    }

    protected static class DoubleArrayIndex<T extends Element> extends ColumnIndex<double[], T> {

        public DoubleArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(DoubleArrays.HASH_STRATEGY);
        }
    }

    protected static class IntegerArrayIndex<T extends Element> extends ColumnIndex<int[], T> {

        public IntegerArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(IntArrays.HASH_STRATEGY);
        }
    }

    protected static class FloatArrayIndex<T extends Element> extends ColumnIndex<float[], T> {

        public FloatArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(FloatArrays.HASH_STRATEGY);
        }
    }

    protected static class LongArrayIndex<T extends Element> extends ColumnIndex<long[], T> {

        public LongArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(LongArrays.HASH_STRATEGY);
        }
    }

    protected static class ShortArrayIndex<T extends Element> extends ColumnIndex<short[], T> {

        public ShortArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(ShortArrays.HASH_STRATEGY);
        }
    }

    protected static class ByteArrayIndex<T extends Element> extends ColumnIndex<byte[], T> {

        public ByteArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(ByteArrays.HASH_STRATEGY);
        }
    }

    protected static class CharArrayIndex<T extends Element> extends ColumnIndex<char[], T> {

        public CharArrayIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(CharArrays.HASH_STRATEGY);
        }
    }

    protected static final class ValueSet<K, T> implements Set<T> {

        protected final K value;
        private final Set<T> set;

        public ValueSet(K value) {
            this.value = value;
            this.set = new ObjectOpenHashSet<>();
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return set.iterator();
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        public <T> T[] toArray(T[] ts) {
            return set.toArray(ts);
        }

        @Override
        public boolean add(T e) {
            return set.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return set.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> clctn) {
            return set.containsAll(clctn);
        }

        @Override
        public boolean addAll(Collection<? extends T> clctn) {
            throw new UnsupportedOperationException("Not supported operation.");
        }

        @Override
        public boolean retainAll(Collection<?> clctn) {
            throw new UnsupportedOperationException("Not supported operation.");
        }

        @Override
        public boolean removeAll(Collection<?> clctn) {
            throw new UnsupportedOperationException("Not supported operation.");
        }

        @Override
        public void clear() {
            set.clear();
        }

        @Override
        public boolean equals(Object o) {
            return set.equals(o);
        }

        @Override
        public int hashCode() {
            return set.hashCode();
        }
    }

    protected final class WithNullDecorator implements Collection<K> {

        private boolean hasNull() {
            return !nullSet.isEmpty();
        }

        @Override
        public int size() {
            return (hasNull() ? 1 : 0) + map.size();
        }

        @Override
        public boolean isEmpty() {
            return !hasNull() && map.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if (o == null && hasNull()) {
                return true;
            } else if (o != null) {
                return map.containsKey((K) o);
            }
            return false;
        }

        @Override
        public Iterator<K> iterator() {
            return new WithNullDecorator.WithNullIterator();
        }

        @Override
        public Object[] toArray() {
            if (hasNull()) {
                Object[] res = new Object[map.size() + 1];
                res[0] = null;
                System.arraycopy(map.keySet().toArray(), 0, res, 1, map.size());
                return res;
            } else {
                return map.keySet().toArray();
            }
        }

        @Override
        public <V> V[] toArray(V[] array) {

            if (hasNull()) {
                if (array.length < size()) {
                    array = (V[]) java.lang.reflect.Array
                            .newInstance(array.getClass().getComponentType(), map.size() + 1);
                }
                array[0] = null;
                System.arraycopy(map.keySet().toArray(), 0, array, 1, map.size());
                return array;
            } else {
                return map.keySet().toArray(array);
            }
        }

        @Override
        public boolean add(K e) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean containsAll(Collection clctn) {
            for (Object o : clctn) {
                if (o == null && nullSet.isEmpty()) {
                    return false;
                } else if (o != null && !map.containsKey((K) o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection clctn) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean removeAll(Collection clctn) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean retainAll(Collection clctn) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported");
        }

        private final class WithNullIterator implements Iterator<K> {

            private final Iterator<K> mapIterator;
            private boolean hasNull;

            public WithNullIterator() {
                hasNull = hasNull();
                mapIterator = map.keySet().iterator();
            }

            @Override
            public boolean hasNext() {
                if (hasNull) {
                    return true;
                }
                return mapIterator.hasNext();
            }

            @Override
            public K next() {
                if (hasNull) {
                    hasNull = false;
                    return null;
                }
                return mapIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported operation.");
            }
        }
    }

    private final class EntryIterator implements Iterator<Map.Entry<K, ? extends Set<T>>> {

        private final Iterator<Map.Entry<K, ValueSet<K, T>>> mapIterator;
        private NullEntry nullEntry;

        public EntryIterator() {
            if (!nullSet.isEmpty()) {
                nullEntry = new NullEntry();
            }
            mapIterator = map.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            if (nullEntry != null) {
                return true;
            }
            return mapIterator.hasNext();
        }

        @Override
        public Map.Entry<K, ? extends Set<T>> next() {
            if (nullEntry != null) {
                NullEntry ne = nullEntry;
                nullEntry = null;
                return ne;
            }
            return mapIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported operation.");
        }
    }

    private class NullEntry implements Map.Entry<K, Set<T>> {

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public Set<T> getValue() {
            return nullSet;
        }

        @Override
        public Set<T> setValue(Set<T> v) {
            throw new UnsupportedOperationException("Not supported operation.");
        }
    }
}
