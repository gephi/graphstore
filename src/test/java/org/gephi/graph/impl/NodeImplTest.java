package org.gephi.graph.impl;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NodeImplTest {

    @Test
    public void testProperties() {
        GraphStore graphStore = GraphGenerator.generateTinyGraphStore();
        Node n = graphStore.getNode("1");
        Assert.assertNotNull(n.getTextProperties());
        Assert.assertNotNull(n.getColor());
        Assert.assertEquals(n.alpha(), 1f);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPropertiesDisabled() {
        GraphStore graphStore = GraphGenerator
                .generateTinyGraphStore(Configuration.builder().enableNodeProperties(false).build());
        Node n = graphStore.getNode("1");
        Assert.assertNull(n.getColor());
    }
}
