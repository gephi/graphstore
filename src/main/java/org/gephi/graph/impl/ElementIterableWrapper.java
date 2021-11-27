package org.gephi.graph.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;

public abstract class ElementIterableWrapper<T extends Element> implements ElementIterable<T> {

    protected final Iterator<T> iterator;
    protected final GraphLock lock;

    public ElementIterableWrapper(Iterator<T> iterator) {
        this(iterator, null);
    }

    public ElementIterableWrapper(Iterator<T> iterator, GraphLock lock) {
        this.iterator = iterator;
        this.lock = lock;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    protected T[] toArray(T[] a) {
        return toCollection().toArray(a);
    }

    @Override
    public Collection<T> toCollection() {
        List<T> list = new ArrayList<>();
        for (; iterator.hasNext();) {
            list.add(iterator.next());
        }
        return list;
    }

    @Override
    public void doBreak() {
        if (lock != null) {
            lock.readUnlock();
        }
    }
}
