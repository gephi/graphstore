/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

module org.gephi.graphstore {
    requires fastutil;
    requires colt;
    requires concurrent;
    requires java.desktop; // because of the class java.awt.Color
    exports org.gephi.graph.impl;
    exports org.gephi.graph.api; // so that tests can access it. There are better ways I am sure.
}
