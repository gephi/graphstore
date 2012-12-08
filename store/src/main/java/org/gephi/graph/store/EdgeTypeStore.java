package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortHeapPriorityQueue;
import it.unimi.dsi.fastutil.shorts.ShortPriorityQueue;

/**
 *
 * @author mbastian
 */
public class EdgeTypeStore {

    public final static int MAX_SIZE = 65534;
    public final static int NULL_COUNT = -1;
    public final static int NULL_TYPE = -1;
    public final static short NULL_SHORT = Short.MIN_VALUE;
    //Data
    protected final Object2ShortMap labelMap;
    protected final Short2ObjectMap idMap;
    protected final ShortPriorityQueue garbageQueue;
    protected int[] countArray;
    protected int length;

    public EdgeTypeStore() {
        if (MAX_SIZE >= Short.MAX_VALUE - Short.MIN_VALUE + 1) {
            throw new RuntimeException("Edge Type Store size can't exceed 65534");
        }
        this.garbageQueue = new ShortHeapPriorityQueue(MAX_SIZE);
        this.countArray = new int[MAX_SIZE];
        this.labelMap = new Object2ShortOpenHashMap(MAX_SIZE);
        this.idMap = new Short2ObjectOpenHashMap(MAX_SIZE);
        for (int i = 0; i < countArray.length; i++) {
            countArray[i] = NULL_COUNT;
        }
        labelMap.defaultReturnValue(NULL_SHORT);
    }

    public int getId(final Object label) {
        checkNonNullObject(label);

        short id = labelMap.getShort(label);
        if (id == NULL_SHORT) {
            return NULL_TYPE;
        }
        return shortToInt(id);
    }

    public Object getLabel(final int id) {
        checkValidId(id);

        return idMap.get(intToShort(id));
    }

    public int addType(final Object label) {
        checkNonNullObject(label);

        short id = labelMap.getShort(label);
        if (id == NULL_SHORT) {
            if (!garbageQueue.isEmpty()) {
                id = garbageQueue.dequeueShort();
            } else {
                id = intToShort(length);
                if (length >= MAX_SIZE) {
                    throw new RuntimeException("Maximum number of edge types reached at " + MAX_SIZE);
                }
                countArray[length] = 0;
                length++;
            }
            labelMap.put(label, id);
            idMap.put(id, label);
        }
        return shortToInt(id);
    }

    public int removeType(final Object label) {
        checkNonNullObject(label);

        short id = labelMap.removeShort(label);
        if (id == NULL_SHORT) {
            return NULL_TYPE;
        }
        idMap.remove(id);
        garbageQueue.enqueue(id);

        int intId = shortToInt(id);
        countArray[intId] = 0;
        return intId;
    }

    public Object removeType(final int type) {
        checkValidId(type);

        short id = intToShort(type);
        Object label = idMap.remove(id);
        if (label != null) {
            labelMap.remove(label);
            garbageQueue.enqueue(id);

            countArray[type] = 0;
        }
        return label;
    }

    private int getCapacity() {
        return countArray.length - garbageQueue.size();
    }

    public int increment(final int type) {
        checkValidId(type);

        int count;
        if (type >= length || ((count = countArray[type]) == NULL_COUNT)) {
            throw new RuntimeException("Edge type id=" + type + " doesn't exist");
        }
        countArray[type] = ++count;;
        return count;
    }

    public int decrement(final int type) {
        checkValidId(type);

        int count;
        if (type >= length || ((count = countArray[type]) == NULL_COUNT) || count == 0) {
            throw new RuntimeException("Edge type id=" + type + " doesn't exist");
        }
        countArray[type] = --count;;
        if (count == 0) {
            //do smthing?
        }
        return count;
    }

    public int getCount(final int type) {
        checkValidId(type);

        int count;
        if (type >= length || ((count = countArray[type]) == NULL_COUNT)) {
            throw new RuntimeException("Edge type id=" + type + " doesn't exist");
        }
        return count;
    }

    public boolean contains(final Object label) {
        return labelMap.containsKey(label);
    }

    public boolean contains(final int id) {
        checkValidId(id);

        return idMap.containsKey(intToShort(id));
    }

    public void clear() {
        countArray = new int[MAX_SIZE];
        for (int i = 0; i < countArray.length; i++) {
            countArray[i] = NULL_COUNT;
        }
    }

    public int size() {
        return length - garbageQueue.size();
    }

    short intToShort(final int id) {
        return (short) (id + Short.MIN_VALUE + 1);
    }

    int shortToInt(final short id) {
        return id - Short.MIN_VALUE - 1;
    }

    void checkValidId(final int id) {
        if (id < 0 || id >= MAX_SIZE) {
            throw new IllegalArgumentException("The type must be included between 0 and 65535");
        }
    }

    void checkNonNullObject(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }
}
