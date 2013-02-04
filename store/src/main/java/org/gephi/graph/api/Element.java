package org.gephi.graph.api;

import java.util.Set;
import org.gephi.attribute.api.Column;

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
    
    public void setProperty(String key, Object value, double timestamp);

    public void setProperty(Column column, Object value, double timestamp);
    
    public boolean addTimestamp(double timestamp);
    
    public boolean removeTimestamp(double timestamp);
    
    public double[] getTimestamps();

    public void clearProperties();
}
