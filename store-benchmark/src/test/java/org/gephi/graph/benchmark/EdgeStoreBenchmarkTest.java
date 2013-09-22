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
public class EdgeStoreBenchmarkTest {

    @Test
    public void testPushStore() {
        NanoBench.create().measurements(2).measure("push edge store", new EdgeStoreBenchmark().pushEdgeStore());
    }

    @Test
    public void testIterateStore() {
        NanoBench.create().cpuOnly().measurements(3).measure("iterate edge store", new EdgeStoreBenchmark().iterateEdgeStore());
    }

    public void testIterateStoreWithLocking() {
        NanoBench.create().cpuOnly().measurements(3).measure("iterate edge store with locking", new EdgeStoreBenchmark().iterateEdgeStoreWithLocking());
    }

    @Test
    public void testIterateOutNeighbors() {
        NanoBench.create().cpuOnly().measurements(10).measure("iterate neighbors list out", new EdgeStoreBenchmark().iterateEdgeStoreNeighborsOut());
    }

    @Test
    public void testIterateInOutNeighbors() {
        NanoBench.create().cpuOnly().measurements(10).measure("iterate neighbors list in & out", new EdgeStoreBenchmark().iterateEdgeStoreNeighborsInOut());
    }

   @Test
    public void testResetEdgeStore() {
        NanoBench.create().cpuOnly().measurements(10).measure("reset edge store", new EdgeStoreBenchmark().resetEdgeStore());
    }
    
    @Test
    public void testAddEdge() {
        int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.01,0.1,0.3};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
        NanoBench.create().measurements(10).measure("add edge"+" "+a[i]+" "+p[j], new EdgeStoreBenchmark().addEdge(a[i],p[j]));
            }
    }
    
    @Test
    public void testRemoveEdge() {
        int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.01,0.1,0.3};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
                if(a[i]==7000 && p[j]==0.3)
                    continue;
    
        NanoBench.create().measurements(5).measure(+a[i]+"\t"+p[j], new EdgeStoreBenchmark().removeEdge(a[i],p[j]));
            }
    }
    
    @Test
    public void testIterateEdge() {
        int[] a =  {10,100,500,1000,5000,7000};
         double[] p = {0.01,0.1,0.3};
        for(int i =0;i<a.length;i++)
            for(int j=0;j<p.length;j++)
            {
                 if(a[i]==7000 && p[j]==0.3)
                    continue;
        NanoBench.create().cpuOnly().measurements(5).measure(a[i]+"\t"+p[j], new EdgeStoreBenchmark().iterateEdge(a[i],p[j]));
            }
            }
     @Test
    public void testKleinbergIterateEdge() {
         int[] no ={10,20,30,40,50};
         for(int i=0;i<no.length;i++)
        NanoBench.create().measurements(10).measure("testKleinbergIterateEdge"+" "+no[i], new EdgeStoreBenchmark().iterateKleinbergEdge(no[i],no[i]/2,5));
    }
     @Test
     public void testKleinbergIterateNeighbors(){
         int[] no ={10,20,30,40,50};
         for(int i=0;i<no.length;i++)
         NanoBench.create().measurements(10).measure("Kleinberg iterate neighbors"+" "+no[i], new EdgeStoreBenchmark().iterateKleinbergNeighbors(no[i],no[i]/2,5));
     }
    @Test
     public void testKleinbergRemoveEdge(){
         int[] no ={10,20,30,40,50};
         for(int i=0;i<no.length;i++)
         NanoBench.create().measurements(10).measure("Kleinberg remove edge"+" "+no[i], new EdgeStoreBenchmark().removeKleinbergEdge(no[i],no[i]/2,5));
     }
    @Test
    public void testKleinbergAddEdge(){
        int[] no ={10,20,30,40,50};
         for(int i=0;i<no.length;i++)
        NanoBench.create().measurements(10).measure("Kleinberg Add Edges"+" "+no[i], new EdgeStoreBenchmark().addKleinbergEdge(no[i],no[i]/2,5));
    }
}
