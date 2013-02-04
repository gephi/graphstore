package org.gephi.attribute.time;

import java.util.Arrays;

/**
 *
 * @author mbastian
 */
public class TimestampSet {

    protected int[] array;
    protected int size = 0;

    public TimestampSet() {
        array = new int[0];
    }

    public TimestampSet(int capacity) {
        array = new int[capacity];
        Arrays.fill(array, -1);
    }

    public boolean add(int timestampIndex) {
        return addInner(timestampIndex, false) >= 0 ? true : false;
    }

    public boolean remove(int timestampIndex) {
        return removeInner(timestampIndex) >= 0 ? true : false;
    }

    protected int addInner(int timestampIndex, boolean allowSet) {
        int index = Arrays.binarySearch(array, 0, size, timestampIndex);
        if (index < 0) {
            int insertIndex = -index - 1;

            if (size < array.length - 1) {
                if (insertIndex < size) {
                    System.arraycopy(array, insertIndex, array, insertIndex + 1, size - insertIndex);
                }
                array[insertIndex] = timestampIndex;
            } else {
                int[] newArray = new int[array.length + 1];
                System.arraycopy(array, 0, newArray, 0, insertIndex);
                System.arraycopy(array, insertIndex, newArray, insertIndex + 1, array.length - insertIndex);
                newArray[insertIndex] = timestampIndex;
                array = newArray;
            }

            size++;
            return insertIndex;
        }
        return allowSet ? index : -1;
    }

    protected int removeInner(int timestampIndex) {
        int index = Arrays.binarySearch(array, 0, size, timestampIndex);
        if (index >= 0) {
            int removeIndex = index;

            if (removeIndex == size - 1) {
                size--;
            } else {
                System.arraycopy(array, removeIndex + 1, array, removeIndex, size - removeIndex - 1);
                size--;
            }

            return removeIndex;
        }
        return -1;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(int timestampIndex) {
        int index = Arrays.binarySearch(array, timestampIndex);
        return index >= 0 && index < size;
    }

    public int[] getTimestamps() {
        if (size < array.length - 1) {
            int[] res = new int[size];
            System.arraycopy(array, 0, res, 0, size);
            return res;
        } else {
            return array;
        }
    }

    public void clear() {
        size = 0;
        array = new int[0];
    }
}
