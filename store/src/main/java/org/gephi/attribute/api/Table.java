package org.gephi.attribute.api;

/**
 *
 * @author mbastian
 */
public interface Table {

    public Column addColumn(String id, Class type);

    public Column addColumn(String id, Class type, Origin origin);

    public Column addColumn(String id, String title, Class type, Origin origin, Object defaultValue, boolean indexed);

    public Column getColumn(int index);

    public Column getColumn(String id);

    public boolean hasColumn(String id);

    public void removeColumn(Column column);
    
    public void removeColumn(String id);

    public int countColumns();
}
