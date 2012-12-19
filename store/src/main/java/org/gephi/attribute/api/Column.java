package org.gephi.attribute.api;

/**
 *
 * @author mbastian
 */
public interface Column {

    public String getId();

    public int getIndex();

    public String getTitle();
    
    public Class getTypeClass();
    
    public Origin getOrigin();
    
    public Object getDefaultValue();
}
