/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gephi.graph.benchmark;

import org.gephi.nanobench.NanoBench;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class NodeStoreBenchmarkTest {

   @Test
    public void testPushStore() {
        NanoBench.create().measurements(10).measure("push node store", new NodeStoreBenchmark().pushStore());
    }

    @Test
    public void testIterateStore() {
        NanoBench.create().cpuOnly().measurements(10).measure("iterate node store", new NodeStoreBenchmark().iterateStore());
    }

    @Test
    public void testResetNodeStore() {
        NanoBench.create().measurements(10).measure("reset node store", new NodeStoreBenchmark().resetNodeStore());
    }
    
    @Test
    public void testAddNode() {
         int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.01,0.1,0.3};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
            NanoBench.create().measurements(5).measure("add node "+" "+a[i]+" "+p[j], new NodeStoreBenchmark().addNode(a[i],p[j]));
            
            }
    }
     @Test
     public void testRemoveNode() {
         int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.1,};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
                if(a[i]==7000 && p[j]==0.3)
                    continue;
         NanoBench.create().measurements(5).measure("Remove Node"+" "+a[i]+" "+p[j], new NodeStoreBenchmark().removeNode(a[i],p[j]));
            }
     }
     @Test 
     public void testIterateNode(){
         int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.1};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
                if(a[i]==7000 && p[j]==0.3)
                    continue;
     
         NanoBench.create().measurements(5).measure("Iterate Nodes"+" "+a[i]+" "+p[j], new NodeStoreBenchmark().iterateNodes(a[i],p[j]));
     
            }}
     @Test 
     public void testIterateNeighbors(){
         int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.1};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
                if(a[i]==7000 && p[j]==0.3)
                    continue;
         NanoBench.create().measurements(5).measure("Iterate Neighbors"+" "+a[i]+" "+p[j], new NodeStoreBenchmark().iterateNeighbors(a[i],p[j]));
            } 
     }
     @Test
     public void testKleinbergAddNode(){
         int[] no ={10,20,30,40,50};
         for(int i=0;i<no.length;i++)
         NanoBench.create().measurements(10).measure("Kleinberg add node"+" "+no[i], new NodeStoreBenchmark().addKleinbergNode(no[i],no[i]/2,5));
     
     }
     @Test
     public void testKleinbergRemoveNode(){
         int[] no ={10,20,30,40,50};
         for(int i=0;i<no.length;i++)
         NanoBench.create().measurements(10).measure("Kleinberg remove node"+" "+no[i], new NodeStoreBenchmark().RemoveKleinbergNode(no[i],no[i]/2,5));
     
     }
    @Test
     public void testKleinbergIterateNode(){
         int[] no ={10,20,30,40,50};
         for(int i=0;i<no.length;i++)
         NanoBench.create().measurements(10).measure("Kleinberg Iterate nodes"+" "+no[i], new NodeStoreBenchmark().iterateKleinbergNodes(no[i],no[i]/2,5));
     
     }
    @Test
    public void testGetAttributeRandom(){
        int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.01,0.1,0.3};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
        NanoBench.create().measurements(10).measure("random get attributes"+" "+a[i]+" "+p[j], new NodeStoreBenchmark().addAttrRandomGraph(a[i],p[j]));
    }
    }
}
