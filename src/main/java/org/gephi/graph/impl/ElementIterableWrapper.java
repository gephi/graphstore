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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;

public abstract class ElementIterableWrapper<T extends Element> implements ElementIterable<T> {

    protected final Iterator<T> iterator;
    protected final GraphLockImpl lock;

    public ElementIterableWrapper(Iterator<T> iterator) {
        this(iterator, null);
    }

    public ElementIterableWrapper(Iterator<T> iterator, GraphLockImpl lock) {
        this.iterator = iterator;
        this.lock = lock;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    protected T[] toArray(T[] a) {
        // TODO This can be improved
        return toCollection().toArray(a);
    }

    @Override
    public Collection<T> toCollection() {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @Override
    public Set<T> toSet() {
        Set<T> set = new HashSet<>();
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    @Override
    public void doBreak() {
        if (lock != null) {
            lock.readUnlock();
        }
    }
}
