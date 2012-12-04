package org.gephi.graph.store;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.ShortHeapPriorityQueue;
import it.unimi.dsi.fastutil.shorts.ShortPriorityQueue;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public class PropertyStore<T extends Element> {

    //Const
    public final static int NULL_ID = -1;
    public final static int MAX_SIZE = 65534;
    public final static short NULL_SHORT = Short.MIN_VALUE;
    //Element
    protected final Class<T> elementType;
    //Columns
    protected final Object2ShortMap<String> idMap;
    protected final AbstractIndex[] columns;
    protected final ShortPriorityQueue garbageQueue;
    protected int length;

    public PropertyStore(Class<T> elementType) {
        if (MAX_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE + 1) {
            throw new RuntimeException("Edge Type Store size can't exceed 65534");
        }
        this.garbageQueue = new ShortHeapPriorityQueue(MAX_SIZE);
        this.idMap = new Object2ShortOpenHashMap<String>(MAX_SIZE);
        this.columns = new AbstractIndex[MAX_SIZE];
        this.elementType = elementType;
        idMap.defaultReturnValue(NULL_SHORT);
    }

    public void addColumn(final Column column) {
        checkNonNullColumnObject(column);

        final ColumnImpl columnImpl = (ColumnImpl) column;
        short id = idMap.getShort(columnImpl.getId());
        if (id == NULL_SHORT) {
            if (!garbageQueue.isEmpty()) {
                id = garbageQueue.dequeueShort();
            } else {
                id = intToShort(length);
                if (length >= MAX_SIZE) {
                    throw new RuntimeException("Maximum number of edge types reached at " + MAX_SIZE);
                }
                length++;
            }
            idMap.put(column.getId(), id);
            int intIndex = shortToInt(id);
            columnImpl.setStoreId(intIndex);
            columns[intIndex] = createIndex(columnImpl);
        } else {
            throw new IllegalArgumentException("The column already exist");
        }
    }

    public void removeColumn(final Column column) {
        checkNonNullColumnObject(column);

        final ColumnImpl columnImpl = (ColumnImpl) column;
        short id = idMap.removeShort(column.getId());
        if (id == NULL_SHORT) {
            throw new IllegalArgumentException("The column doesnt exist");
        }
        garbageQueue.enqueue(id);

        int intId = shortToInt(id);
        columns[intId].destroy();
        columns[intId] = null;
    }
    
    public int getColumnIndex(final String key) {
       short id = idMap.getShort(key);
       if(id == NULL_SHORT) {
           throw new IllegalArgumentException("The column doesnt exist");
       }
       return shortToInt(id);
    }
    
    public Column getColumnByIndex(final int index) {
       if(index < 0 || index >= columns.length) {
           throw new IllegalArgumentException("The column doesnt exist");
       }
       AbstractIndex a = columns[index];
       if(a == null) {
           throw new IllegalArgumentException("The column doesnt exist");
       }
       return a.column;
    }
    
    public Column getColumn(final String key) {
       short id = idMap.getShort(key);
       if(id == NULL_SHORT) {
           throw new IllegalArgumentException("The column doesnt exist");
       }
       return columns[shortToInt(id)].column;
    }
    

    public boolean hasColumn(String key) {
        return idMap.containsKey(key);
    }
    
    public Set<String> getPropertyKeys() {
        return idMap.keySet();
    }

    public int getCount(String key, Object value) {
        checkNonNullObject(key);

        AbstractIndex index = getIndex(key);
        return index.getCount(value);
    }

    public int getCount(Column column, Object value) {
        checkNonNullColumnObject(column);

        AbstractIndex index = getIndex((ColumnImpl) column);
        return index.getCount(value);
    }

    public Iterable<T> get(String key, Object value) {
        checkNonNullObject(key);

        AbstractIndex index = getIndex(key);
        return index.getValueSet(value);
    }

    public Iterable<T> get(Column column, Object value) {
        checkNonNullColumnObject(column);

        AbstractIndex index = getIndex((ColumnImpl) column);
        return index.getValueSet(value);
    }

    public void put(String key, Object value, T element) {
        checkNonNullObject(key);

        AbstractIndex index = getIndex(key);
        index.putValue(element, value);
    }

    public void put(Column column, Object value, T element) {
        checkNonNullColumnObject(column);

        AbstractIndex index = getIndex((ColumnImpl) column);
        index.putValue(element, value);
    }

    public void remove(String key, Object value, T element) {
        checkNonNullObject(key);

        AbstractIndex index = getIndex(key);
        index.removeValue(element, value);
    }

    public void remove(Column column, Object value, T element) {
        checkNonNullColumnObject(column);

        AbstractIndex index = getIndex((ColumnImpl) column);
        index.removeValue(element, value);
    }
    
    public void set(String key, Object oldValue, Object value, T element) {
        checkNonNullObject(key);

        AbstractIndex index = getIndex(key);
        index.replaceValue(element, oldValue, value);
    }

    public void set(Column column, Object oldValue, Object value, T element) {
        checkNonNullColumnObject(column);

        AbstractIndex index = getIndex((ColumnImpl) column);
        index.replaceValue(element, oldValue, value);
    }

    public int size() {
        return length - garbageQueue.size();
    }

    public Class<T> getElementClass() {
        return elementType;
    }

    AbstractIndex getIndex(String id) {
        short shortId = idMap.getShort(id);
        if (shortId == NULL_SHORT) {
            return null;
        }
        return columns[shortToInt(shortId)];
    }

    AbstractIndex getIndex(ColumnImpl column) {
        return columns[column.getStoreId()];
    }

    AbstractIndex createIndex(ColumnImpl column) {
        return null;
    }

    void destoryIndex(AbstractIndex index) {
        index.destroy();
    }

    short intToShort(final int id) {
        return (short) (id + Short.MIN_VALUE + 1);
    }

    int shortToInt(final short id) {
        return id - Short.MIN_VALUE - 1;
    }

    void checkNonNullObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    void checkNonNullColumnObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof ColumnImpl)) {
            throw new ClassCastException("Must be ColumnImpl object");
        }
    }

    protected abstract class AbstractIndex<K> {

        //Const
        public static final boolean TRIMMING_ENABLED = false;
        public static final int TRIMMING_FREQUENCY = 30;
        //Data
        protected final ColumnImpl column;
        protected final Set<T> nullSet;
        protected Map<K, Set<T>> map;

        public AbstractIndex(ColumnImpl column) {
            this.column = column;
            this.nullSet = new ObjectOpenHashSet<T>();
        }

        public void putValue(T element, K value) {
            Set<T> set;
            if (value == null) {
                set = nullSet;
            } else {
                set = getValueSet(value);
                if (set == null) {
                    set = addValue(value);
                }
            }
            set.add(element);
        }

        public void removeValue(T element, K value) {
            if (value == null) {
                nullSet.remove(element);
            } else {
                Set<T> set = getValueSet(value);
                set.remove(element);
                if (set.isEmpty() && value != null) {
                    removeValue(value);
                }
            }
        }

        public void replaceValue(T element, K oldValue, K newValue) {
            removeValue(element, oldValue);
            putValue(element, newValue);
        }

        public int getCount(K value) {
            if (value == null) {
                return nullSet.size();
            }
            Set<T> valueSet = getValueSet(value);
            if (valueSet != null) {
                return valueSet.size();
            } else {
                return 0;
            }
        }

        public Object getMinValue() {
            if (isSortable()) {
                if (map.isEmpty()) {
                    return null;
                } else {
                    return ((SortedMap) map).firstKey();
                }
            } else {
                throw new UnsupportedOperationException("is not a sortable column.");
            }
        }

        public Object getMaxValue() {
            if (isSortable()) {
                if (map.isEmpty()) {
                    return null;
                } else {
                    return ((SortedMap) map).lastKey();
                }
            } else {
                throw new UnsupportedOperationException(" is not a sortable column.");
            }
        }

        protected Set<T> getValueSet(K value) {
            return map.get(value);
        }

        protected void removeValue(K value) {
            map.remove(value);
        }

        protected Set<T> addValue(K value) {
            Set<T> set = new ObjectOpenHashSet<T>();
            map.put(value, set);
            return set;
        }

        protected void destroy() {
            map = null;
        }

        private boolean isSortable() {
            return column.getTypeClass().isAssignableFrom(Number.class);
        }
    }

    protected class DoubleIndex extends AbstractIndex<Double> {

        public DoubleIndex(ColumnImpl column) {
            super(column);

            map = new Double2ObjectAVLTreeMap<Set<T>>();
        }
    }

    protected class IntegerIndex extends AbstractIndex<Integer> {

        public IntegerIndex(ColumnImpl column) {
            super(column);

            map = new Int2ObjectAVLTreeMap<Set<T>>();
        }
    }

    protected class FloatIndex extends AbstractIndex<Float> {

        public FloatIndex(ColumnImpl column) {
            super(column);

            map = new Float2ObjectAVLTreeMap<Set<T>>();
        }
    }

    protected class LongIndex extends AbstractIndex<Long> {

        public LongIndex(ColumnImpl column) {
            super(column);

            map = new Long2ObjectAVLTreeMap<Set<T>>();
        }
    }

    protected class ShortIndex extends AbstractIndex<Short> {

        public ShortIndex(ColumnImpl column) {
            super(column);

            map = new Short2ObjectAVLTreeMap<Set<T>>();
        }
    }

    protected class ByteIndex extends AbstractIndex<Byte> {

        public ByteIndex(ColumnImpl column) {
            super(column);

            map = new Byte2ObjectAVLTreeMap<Set<T>>();
        }
    }
}
