/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.store;

import org.gephi.attribute.api.Table;
import org.gephi.attribute.api.TableObserver;

/**
 *
 * @author mbastian
 */
public class TableObserverImpl implements TableObserver {

    protected final TableImpl table;
    protected boolean destroyed;
    //Hashcodes
    protected int tableHash;

    public TableObserverImpl(TableImpl table) {
        this.table = table;

        tableHash = table.hashCode();
    }

    @Override
    public synchronized boolean hasTableChanged() {
        int newHash = table.hashCode();
        boolean changed = newHash != tableHash;
        tableHash = newHash;
        return changed;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public void destroy() {
        table.destroyTableObserver(this);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    protected void destroyObserver() {
        tableHash = 0;
        destroyed = true;
    }
}
