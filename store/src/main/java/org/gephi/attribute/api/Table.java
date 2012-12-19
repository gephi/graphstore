package org.gephi.attribute.api;

/**
 *
 * @author mbastian
 */
public interface Table {
    
    public Column addColumn(String id, Class type);
    
    public Column addColumn(String id, Class type, Origin origin);
    
    public Column addColumn(String id, String title, Class type, Origin origin, Object defaultValue);
    
    public int countColumns();
}
