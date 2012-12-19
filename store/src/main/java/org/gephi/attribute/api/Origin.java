package org.gephi.attribute.api;

/**
 *
 * @author mbastian
 */
public enum Origin {

    PROPERTY("AttributeOrigin_property"),
    DATA("AttributeOrigin_data"),
    COMPUTED("AttributeOrigin_computed");
    private final String label;

    Origin(String label) {
        this.label = label;
    }
}
