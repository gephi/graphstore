/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

module GraphStore {
    requires fastutil;
    requires colt;
    requires concurrent;
    requires java.desktop; // because of the class java.awt.Color
    exports org.gephi.graph.impl;
}
