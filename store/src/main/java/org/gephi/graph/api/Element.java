package org.gephi.graph.api;

import org.gephi.attribute.api.Column;
import java.util.Set;

/**
 *
 * @author mbastian
 */
public interface Element {

    public Object getId();

    public Object getProperty(String key);

    public Object getProperty(Column column);

    public Object[] getProperties();

    public Set<String> getPropertyKeys();

    public Object removeProperty(String key);
    
    public Object removeProperty(Column column);

    public void setProperty(String key, Object value);

    public void setProperty(Column column, Object value);

    public void clearProperties();
}
