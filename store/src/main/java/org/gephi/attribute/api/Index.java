package org.gephi.attribute.api;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public interface Index<T extends Element> {

    public int count(Column column, Object value);

    public Iterable<T> get(Column column, Object value);

    public Iterable<Entry<Object, Set<T>>> get(Column column);

    public Collection values(Column column);
    
    public int countValues(Column column);
    
    public int countElements(Column column);
    
    public Number getMinValue(Column column);

    public Number getMaxValue(Column column);

    public Class<T> getIndexClass();

    public String getIndexName();
}
