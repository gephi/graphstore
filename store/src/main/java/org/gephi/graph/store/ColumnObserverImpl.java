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
package org.gephi.graph.store;

import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.ColumnObserver;

/**
 *
 * @author mbastian
 */
public class ColumnObserverImpl implements ColumnObserver {

    protected final ColumnImpl column;
    //Version
    protected int version = Integer.MIN_VALUE;
    protected boolean destroyed;

    public ColumnObserverImpl(ColumnImpl column) {
        this.column = column;
        this.version = column.version.version.get();
    }

    @Override
    public synchronized boolean hasColumnChanged() {
        int v = column.version.version.get();
        boolean changed = v != version;
        version = v;
        return changed;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public void destroy() {
        column.destroyColumnObserver(this);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    protected void destroyObserver() {
        destroyed = true;
    }
}
