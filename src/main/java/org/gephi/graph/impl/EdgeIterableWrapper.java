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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;

public class EdgeIterableWrapper extends ElementIterableWrapper<Edge> implements EdgeIterable {

    public EdgeIterableWrapper(Supplier<Iterator<Edge>> iteratorSupplier, GraphLockImpl lock) {
        super(iteratorSupplier, lock);
    }

    public EdgeIterableWrapper(Supplier<Iterator<Edge>> iteratorSupplier, Supplier<Spliterator<Edge>> spliteratorSupplier, GraphLockImpl lock) {
        super(iteratorSupplier, spliteratorSupplier, lock);
    }

    @Override
    public Edge[] toArray() {
        if (parallelPossible && lock != null) {
            lock.readLock();
            try {
                return StreamSupport.stream(spliterator(), true).toArray(Edge[]::new);
            } finally {
                lock.readUnlock();
            }
        }
        return StreamSupport.stream(spliterator(), parallelPossible).toArray(Edge[]::new);
    }
}
