package org.gephi.graph.utils;

import java.util.Map;

public class MapDeepEquals {

    /**
     * Compares two maps for equality. This is based around the idea that if the
     * keys are deep equal and the values the keys return are deep equal then
     * the maps are equal.
     *
     * @param m1 - first map
     * @param m2 - second map
     * @return - weather the maps are deep equal
     */
    public static boolean mapDeepEquals(Map<?, ?> m1, Map<?, ?> m2) {
        if (m1.size() != m1.size()) {
            return false;
        }

        for (Map.Entry<?, ?> e : m1.entrySet()) {
            Object o = m2.get(e.getKey());
            if (e.getValue() == null && o != null) {
                return false;
            }
            if (!e.getValue().equals(o)) {
                return false;
            }
        }
        return true;
    }
}