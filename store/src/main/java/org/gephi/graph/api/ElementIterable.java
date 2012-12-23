package org.gephi.graph.api;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author mbastian
 */
public interface ElementIterable<T extends Element> extends Iterable<T> {

    @Override
    public Iterator<T> iterator();

    public T[] toArray();

    public Collection<T> toCollection();

    public void doBreak();
}
