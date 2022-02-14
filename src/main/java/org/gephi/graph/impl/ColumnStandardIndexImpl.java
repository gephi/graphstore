/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.Element;

public abstract class ColumnStandardIndexImpl<K, T extends Element> implements ColumnIndexImpl<K, T> {

    // Lock (optional)
    protected final TableLockImpl lock;
    // Data
    protected final ColumnImpl column;
    protected final ValueSet<K, T> nullSet;
    protected Map<K, ValueSet<K, T>> map;
    // Variable
    protected int elements;
    // Version
    protected final AtomicInteger version = new AtomicInteger(Integer.MIN_VALUE);

    protected ColumnStandardIndexImpl(ColumnImpl column) {
        this.column = column;
        this.nullSet = new ValueSet<>(null);
        this.lock = GraphStoreConfiguration.ENABLE_AUTO_LOCKING ? new TableLockImpl() : null;
    }

    protected static boolean isSupportedType(ColumnImpl col) {
        return !col.isDynamicAttribute();
    }

    @Override
    public K putValue(T element, K value) {
        lock();
        try {
            if (value == null) {
                if (nullSet.add(element)) {
                    elements++;
                    version.incrementAndGet();
                }
            } else {
                ValueSet<K, T> set = getValueSet(value);
                if (set == null) {
                    set = addValue(value);
                }
                value = set.value;

                if (set.add(element)) {
                    elements++;
                    version.incrementAndGet();
                }
            }
        } finally {
            unlock();
        }
        return value;
    }

    @Override
    public void removeValue(T element, K value) {
        lock();
        try {
            if (value == null) {
                if (nullSet.remove(element)) {
                    elements--;
                    version.incrementAndGet();
                }
            } else {
                ValueSet<K, T> set = getValueSet(value);
                if (set.remove(element)) {
                    elements--;
                    version.incrementAndGet();
                }
                if (set.isEmpty()) {
                    removeValue(value);
                }
            }
        } finally {
            unlock();
        }
    }

    @Override
    public K replaceValue(T element, K oldValue, K newValue) {
        removeValue(element, oldValue);
        return putValue(element, newValue);
    }

    protected int getCount(K value) {
        lock();
        try {
            if (value == null) {
                return nullSet.size();
            }
            ValueSet<K, T> valueSet = getValueSet(value);
            if (valueSet != null) {
                return valueSet.size();
            } else {
                return 0;
            }
        } finally {
            unlock();
        }
    }

    @Override
    public int count(K value) {
        return getCount(value);
    }

    @Override
    public Collection<K> values() {
        lock();
        try {
            return new ArrayList<>(new WithNullDecorator());
        } finally {
            unlock();
        }
    }

    @Override
    public int countValues() {
        return (nullSet.isEmpty() ? 0 : 1) + map.size();
    }

    @Override
    public int countElements() {
        return elements;
    }

    @Override
    public Number getMinValue() {
        lock();
        try {
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
        } finally {
            unlock();
        }
    }

    @Override
    public Number getMaxValue() {
        lock();
        try {
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
        } finally {
            unlock();
        }
    }

    @Override
    public void destroy() {
        lock();
        map = null;
        nullSet.clear();
        elements = 0;
        version.incrementAndGet();
        unlock();
    }

    @Override
    public void clear() {
        lock();
        map.clear();
        nullSet.clear();
        elements = 0;
        version.incrementAndGet();
        unlock();
    }

    @Override
    public Iterator<Map.Entry<K, ? extends Set<T>>> iterator() {
        return new EntryIterator();
    }

    @Override
    public Iterable<T> get(K value) {
        lock();
        ValueSet<K, T> valueSet = getValueSet(value);
        if (valueSet == null) {
            return ValueSet.EMPTY;
        }
        return new LockableIterable<>(valueSet.set);
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

    @Override
    public boolean isSortable() {
        return Number.class.isAssignableFrom(column.getTypeClass()) && map instanceof SortedMap;
    }

    @Override
    public ColumnImpl getColumn() {
        return column;
    }

    @Override
    public int getVersion() {
        return version.get();
    }

    void lock() {
        if (lock != null) {
            lock.lock();
        }
    }

    void unlock() {
        if (lock != null) {
            lock.unlock();
        }
    }

    protected static class DefaultStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Object, T> {

        public DefaultStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenHashMap<>();
        }
    }

    protected static class BooleanStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Boolean, T> {

        public BooleanStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenHashMap<>();
        }
    }

    protected static class DoubleStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Double, T> {

        public DoubleStandardIndex(ColumnImpl column) {
            super(column);

            map = new Double2ObjectAVLTreeMap<>();
        }
    }

    protected static class IntegerStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Integer, T> {

        public IntegerStandardIndex(ColumnImpl column) {
            super(column);

            map = new Int2ObjectAVLTreeMap<>();
        }
    }

    protected static class FloatStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Float, T> {

        public FloatStandardIndex(ColumnImpl column) {
            super(column);

            map = new Float2ObjectAVLTreeMap<>();
        }
    }

    protected static class LongStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Long, T> {

        public LongStandardIndex(ColumnImpl column) {
            super(column);

            map = new Long2ObjectAVLTreeMap<>();
        }
    }

    protected static class ShortStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Short, T> {

        public ShortStandardIndex(ColumnImpl column) {
            super(column);

            map = new Short2ObjectAVLTreeMap<>();
        }
    }

    protected static class ByteStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Byte, T> {

        public ByteStandardIndex(ColumnImpl column) {
            super(column);

            map = new Byte2ObjectAVLTreeMap<>();
        }
    }

    protected static class GenericNumberStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Number, T> {

        public GenericNumberStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectAVLTreeMap<>();
        }
    }

    protected static class CharStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Character, T> {

        public CharStandardIndex(ColumnImpl column) {
            super(column);

            map = new Char2ObjectAVLTreeMap<>();
        }
    }

    protected static class DefaultArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<Object[], T> {

        public DefaultArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(ObjectArrays.HASH_STRATEGY);
        }
    }

    protected static class BooleanArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<boolean[], T> {

        public BooleanArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(BooleanArrays.HASH_STRATEGY);
        }
    }

    protected static class DoubleArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<double[], T> {

        public DoubleArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(DoubleArrays.HASH_STRATEGY);
        }
    }

    protected static class IntegerArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<int[], T> {

        public IntegerArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(IntArrays.HASH_STRATEGY);
        }
    }

    protected static class FloatArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<float[], T> {

        public FloatArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(FloatArrays.HASH_STRATEGY);
        }
    }

    protected static class LongArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<long[], T> {

        public LongArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(LongArrays.HASH_STRATEGY);
        }
    }

    protected static class ShortArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<short[], T> {

        public ShortArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(ShortArrays.HASH_STRATEGY);
        }
    }

    protected static class ByteArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<byte[], T> {

        public ByteArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(ByteArrays.HASH_STRATEGY);
        }
    }

    protected static class CharArrayStandardIndex<T extends Element> extends ColumnStandardIndexImpl<char[], T> {

        public CharArrayStandardIndex(ColumnImpl column) {
            super(column);

            map = new Object2ObjectOpenCustomHashMap<>(CharArrays.HASH_STRATEGY);
        }
    }

    protected static final class ValueSet<K, T> implements Set<T> {

        protected static ValueSet EMPTY = new ValueSet(null);
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

    private class LockableIterable<E> implements Iterable<E> {

        private final Iterable<E> ite;

        public LockableIterable(Iterable<E> ite) {
            this.ite = ite;
        }

        @Override
        public Iterator<E> iterator() {
            return new LockableIterator<>(ite.iterator());
        }
    }

    private class LockableIterator<E> implements Iterator<E> {

        private final Iterator<E> itr;

        public LockableIterator(Iterator<E> itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            boolean n = itr.hasNext();
            if (!n && lock != null) {
                lock.unlock();
            }
            return n;
        }

        @Override
        public E next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
