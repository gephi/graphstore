package org.gephi.graph.store;

import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Origin;

/**
 *
 * @author mbastian
 */
public class ColumnImpl implements Column {

    //Attributes
    protected final String id;
    protected final Class typeClass;
    protected final String title;
    protected final Object defaultValue;
    protected final Origin origin;
    //Store Id
    protected int storeId = PropertyStore.NULL_ID;

    public ColumnImpl(String id, Class typeClass, String title, Object defaultValue, Origin origin) {
        this.id = id;
        this.typeClass = typeClass;
        this.title = title;
        this.defaultValue = defaultValue;
        this.origin = origin;
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

    @Override
    public Origin getOrigin() {
        return origin;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }    
    
    @Override
    public String toString() {
        return title + " (" + typeClass.toString() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Column) {
            ColumnImpl o = (ColumnImpl) obj;
            return id.equals(o.id) && o.typeClass == typeClass;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.typeClass != null ? this.typeClass.hashCode() : 0);
        return hash;
    }
}
