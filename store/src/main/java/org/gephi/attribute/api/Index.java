package org.gephi.attribute.api;

import java.util.Collection;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public interface Index<T extends Element> {

    public int count(Column column, Object value);

    public Iterable<T> get(Column column, Object value);

    public Collection values(Column column);
    
    public int countValues(Column column);
    
    public int countElements(Column column);
    
    public Number getMinValue(Column column);

    public Number getMaxValue(Column column);

    public Class<T> getIndexClass();

    public String getIndexName();
}
