package org.gephi.graph.api;

/**
 *
 * @author mbastian
 */
public interface Column {

    public String getId();

    public int getIndex();

    public String getTitle();
    
    public Class getTypeClass();
    
    public Object getDefaultValue();
}
