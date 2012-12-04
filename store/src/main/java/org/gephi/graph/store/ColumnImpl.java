package org.gephi.graph.store;

import org.gephi.graph.api.Column;

/**
 *
 * @author mbastian
 */
public class ColumnImpl implements Column {

    //Attributes
    protected final String id;
    protected final Class typeClass;
    protected String title;
    protected Object defaultValue;
    //Store Id
    protected int storeId = PropertyStore.NULL_ID;

    public ColumnImpl(String id, Class typeClass) {
        this.id = id;
        this.typeClass = typeClass;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getIndex() {
        return storeId;
    }

    @Override
    public Class getTypeClass() {
        return typeClass;
    }

    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }    
}
