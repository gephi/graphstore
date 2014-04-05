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

import cern.colt.bitvector.BitVector;
import cern.colt.map.OpenIntObjectHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.gephi.attribute.time.TimestampSet;

/**
 *
 * @author mbastian
 */
public class DataStructureBenchmark {

    private static int NODES = 500000;
    private static int LOW_NODES = 50000;
    private Object object;

    /**
     * Insertion and memory usage for Int2ObjectOpenHashMap
     */
    public Runnable openHashMapMemory() {
        return new Runnable() {
            @Override
            public void run() {
                int nodes = NODES;
                Int2ObjectMap map = new Int2ObjectOpenHashMap(nodes);
                for (int i = 0; i < nodes; i++) {
                    map.put(i, new Object());
                }
                object = map;
            }
        };
    }

    /**
     * Insertion and memory usage for Int2ObjectOpenHashMap without original
     * capcity
     */
    public Runnable dynamicOpenHashMapMemory() {
        return new Runnable() {
            @Override
            public void run() {
                int nodes = NODES;
                Int2ObjectMap map = new Int2ObjectOpenHashMap();
                for (int i = 0; i < nodes; i++) {
                    map.put(i, new Object());
                }
                object = map;
            }
        };
    }

    /**
     * Insertion and memory usage for Int2ObjectRBTreeMap
     */
    public Runnable rbHashMapMemory() {
        return new Runnable() {
            @Override
            public void run() {
                int nodes = NODES;
                Int2ObjectMap map = new Int2ObjectRBTreeMap();
                for (int i = 0; i < nodes; i++) {
                    map.put(i, new Object());
                }
                object = map;
            }
        };
    }

    /**
     * Insertion and memory usage for OpenIntObjectHashMap
     */
    public Runnable coltOpenHashMapMemory() {
        return new Runnable() {
            @Override
            public void run() {
                int nodes = NODES;
                OpenIntObjectHashMap map = new OpenIntObjectHashMap(nodes);
                for (int i = 0; i < nodes; i++) {
                    map.put(i, new Object());
                }
                object = map;
            }
        };
    }

    /**
     * Insertion and memory usage for basic array
     */
    public Runnable arrayMemory() {
        return new Runnable() {
            @Override
            public void run() {
                int nodes = NODES;
                Object[] array = new Object[nodes];
                for (int i = 0; i < nodes; i++) {
                    array[i] = new Object();
                }
                object = array;
            }
        };
    }

    /**
     * Insertion and memory usage for object list
     */
    public Runnable objectListMemory() {
        return new Runnable() {
            @Override
            public void run() {
                int nodes = NODES;
                final ObjectList list = new ObjectArrayList(nodes);
                for (int i = 0; i < nodes; i++) {
                    list.add(new Integer(1));
                }
                object = list;
            }
        };
    }

    public Runnable dynamicObjectListMemory() {
        return new Runnable() {
            @Override
            public void run() {
                final ObjectList list = new ObjectArrayList();
                int nodes = NODES;
                for (int i = 0; i < nodes; i++) {
                    list.add(new Integer(1));
                }
                object = list;
            }
        };
    }

    /**
     * Insertion and memory usage for object list
     */
    public Runnable objectBigListMemory() {
        return new Runnable() {
            @Override
            public void run() {
                int nodes = NODES;
                final ObjectBigList list = new ObjectBigArrayBigList(nodes);
                for (int i = 0; i < nodes; i++) {
                    list.add(new Integer(1));
                }
                object = list;
            }
        };
    }

    public Runnable openHashMapIteration() {
        int nodes = NODES;
        final Int2ObjectMap map = new Int2ObjectOpenHashMap(nodes);
        for (int i = 0; i < nodes; i++) {
            map.put(i, new Integer(1));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : map.values()) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable rbHashMapIteration() {
        int nodes = NODES;
        final Int2ObjectMap map = new Int2ObjectRBTreeMap();
        for (int i = 0; i < nodes; i++) {
            map.put(i, new Integer(1));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : map.values()) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable arrayIteration() {
        //Create array
        int nodes = NODES;
        final Object[] array = new Object[nodes];
        for (int i = 0; i < nodes; i++) {
            array[i] = new Integer(1);
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : array) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable linkedListIteration() {
        //Create array
        int nodes = NODES;
        final LinkedList list = new LinkedList<Integer>();
        for (int i = 0; i < nodes; i++) {
            list.add(new Integer(1));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : list) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable objectListIteration() {
        int nodes = NODES;
        final ObjectList list = new ObjectArrayList(nodes);
        for (int i = 0; i < nodes; i++) {
            list.add(new Integer(1));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : list) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable objectBigListIteration() {
        int nodes = NODES;
        final ObjectBigList list = new ObjectBigArrayBigList(nodes);
        for (int i = 0; i < nodes; i++) {
            list.add(new Integer(1));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : list) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable objectLinkedHashMapIteration() {
        int nodes = NODES;
        final Int2ObjectLinkedOpenHashMap list = new Int2ObjectLinkedOpenHashMap(nodes);
        for (int i = 0; i < nodes; i++) {
            list.put(new Integer(i), new Integer(1));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : list.values()) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable objectHashObjectSet() {
        int nodes = NODES;
        final ObjectOpenHashSet list = new ObjectOpenHashSet(nodes);
        for (int i = 0; i < nodes; i++) {
            list.add(new Integer(i));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : list) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable objectAVLObjectSet() {
        int nodes = NODES;
        final ObjectAVLTreeSet list = new ObjectAVLTreeSet();
        for (int i = 0; i < nodes; i++) {
            list.add(new Integer(i));
        }
        return new Runnable() {
            @Override
            public void run() {
                int sum = 0;
                for (Object i : list) {
                    sum += (Integer) i;
                }
                object = sum;
            }
        };
    }

    public Runnable openHashMapResetAll() {
        int nodes = LOW_NODES;
        final Int2IntOpenHashMap map = new Int2IntOpenHashMap(nodes);
        for (int i = 0; i < nodes; i++) {
            map.put(i, i);
        }
        return new Runnable() {
            @Override
            public void run() {
                int nodes = LOW_NODES;
                for (int i = 0; i < nodes; i++) {
                    map.put(i, i);
                }
            }
        };
    }

    public Runnable arrayResetAll() {
        int nodes = LOW_NODES;
        final int[] array = new int[nodes];
        for (int i = 0; i < nodes; i++) {
            array[i] = i;
        }
        return new Runnable() {
            @Override
            public void run() {
                int nodes = LOW_NODES;
                for (int i = 0; i < nodes; i++) {
                    array[i] = i;
                }
            }
        };
    }

    public Runnable objectListResetAll() {
        int nodes = LOW_NODES;
        final IntList list = new IntArrayList(nodes);
        for (int i = 0; i < nodes; i++) {
            list.add(i);
        }
        return new Runnable() {
            @Override
            public void run() {
                int nodes = LOW_NODES;
                for (int i = 0; i < nodes; i++) {
                    list.set(i, i);
                }
            }
        };
    }

    public Runnable fastutilObject2IntIterate() {
        int nodes = NODES;
        final Object2IntMap<String> map = new Object2IntOpenHashMap<String>();
        for (int i = 0; i < nodes; i++) {
            map.put("" + i, i);
        }
        final Random rand = new Random(456);
        return new Runnable() {
            @Override
            public void run() {
                int matches = 0;
                for (int i = 0; i < 50000; i++) {
                    int id = rand.nextInt(NODES - 1);
                    if (map.containsKey("" + id)) {
                        matches += id;
                    }
                }
                object = matches;
            }
        };
    }

    public Runnable javaObject2IntIterate() {
        int nodes = NODES;
        final Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < nodes; i++) {
            map.put("" + i, i);
        }
        final Random rand = new Random(456);
        return new Runnable() {
            @Override
            public void run() {
                int matches = 0;
                for (int i = 0; i < 50000; i++) {
                    int id = rand.nextInt(NODES - 1);
                    if (map.containsKey("" + id)) {
                        matches += id;
                    }
                }
                object = matches;
            }
        };
    }

    public Runnable javaArrayIterate() {
        final boolean[] bitset = new boolean[NODES];
        Random rand = new Random(23);
        for (int i = 0; i < NODES; i++) {
            bitset[i] = rand.nextBoolean();
        }
        return new Runnable() {
            @Override
            public void run() {
                int cardinality = 0;
                for (int i = 0; i < NODES; i++) {
                    boolean b = bitset[i];
                    if (b) {
                        cardinality++;
                    }
                }
                object = cardinality;
            }
        };
    }

    public Runnable javaBitVectorIterate() {
        final BitSet bitset = new BitSet(NODES);
        Random rand = new Random(23);
        for (int i = 0; i < NODES; i++) {
            bitset.set(i, rand.nextBoolean());
        }
        return new Runnable() {
            @Override
            public void run() {
                int cardinality = 0;
                for (int i = 0; i < NODES; i++) {
                    boolean b = bitset.get(i);
                    if (b) {
                        cardinality++;
                    }
                }
                object = cardinality;
            }
        };
    }

    public Runnable coltBitVector() {
        final BitVector bitset = new BitVector(NODES);
        Random rand = new Random(23);
        for (int i = 0; i < NODES; i++) {
            if (rand.nextBoolean()) {
                bitset.set(i);
            }
        }
        return new Runnable() {
            @Override
            public void run() {
                int cardinality = 0;
                for (int i = 0; i < NODES; i++) {
                    boolean b = bitset.get(i);
                    if (b) {
                        cardinality++;
                    }
                }
                object = cardinality;
            }
        };
    }

    public Runnable coltBitVectorIterateAndMapLookup() {
        final BitVector bitset = new BitVector(NODES);
        final Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        Random rand = new Random(23);
        for (int i = 0; i < NODES; i++) {
            if (rand.nextBoolean()) {
                bitset.set(i);
                map.put(i, i);
            }
        }
        map.trim();
        return new Runnable() {
            @Override
            public void run() {
                int cardinality = 0;
                for (int i = 0; i < NODES; i++) {
                    boolean b = bitset.get(i);
                    if (b) {
                        long rank = map.get(i);
                        cardinality += rank;
                    }
                }
                object = cardinality;

            }
        };
    }

    public Runnable javaBitVectorMemory() {

        return new Runnable() {
            @Override
            public void run() {
                BitSet bitset = new BitSet(10000000);
                Random rand = new Random(23);
                for (int i = 0; i < 10000000; i++) {
                    bitset.set(i, rand.nextBoolean());
                }
                object = bitset;
            }
        };
    }

    public Runnable coltBitVectorMemory() {

        return new Runnable() {
            @Override
            public void run() {
                BitVector bitset = new BitVector(10000000);
                Random rand = new Random(23);
                for (int i = 0; i < 10000000; i++) {
                    if (rand.nextBoolean()) {
                        bitset.set(i);
                    }
                }
                object = bitset;
            }
        };
    }

    public Runnable sparseArrayIterate(float emptyRatio) {
        final BitVector bitset = new BitVector(NODES);
        final int[] values = new int[NODES];
        Random rand2 = new Random(456);
        Random rand = new Random(23);
        for (int i = 0; i < NODES; i++) {
            if (rand.nextDouble() < emptyRatio) {
                bitset.set(i);
            }
            values[i] = rand2.nextInt(NODES);
        }
        return new Runnable() {
            @Override
            public void run() {
                int cardinality = 0;
                for (int i = 0; i < NODES; i++) {
                    boolean b = bitset.get(i);
                    if (b) {
                        int val = values[i];
                        cardinality += val;
                    }
                }
                object = cardinality;
            }
        };
    }

    public Runnable intHashSetIterate(float emptyRatio) {
        int nodes = (int) (NODES * (1 - emptyRatio));
        Random rand2 = new Random(456);
        final IntSet values = new IntOpenHashSet(nodes);
        for (int i = 0; i < nodes; i++) {
            values.add(rand2.nextInt(NODES));
        }
        return new Runnable() {
            @Override
            public void run() {
                int cardinality = 0;
                IntIterator itr = values.iterator();
                while (itr.hasNext()) {
                    int i = itr.nextInt();
                    cardinality += i;
                }
                object = cardinality;
            }
        };
    }

    public Runnable intAVLSetIterate(float emptyRatio) {
        int nodes = (int) (NODES * (1 - emptyRatio));
        Random rand2 = new Random(456);
        final IntSet values = new IntAVLTreeSet();
        for (int i = 0; i < nodes; i++) {
            values.add(rand2.nextInt(NODES));
        }
        return new Runnable() {
            @Override
            public void run() {
                int cardinality = 0;
                IntIterator itr = values.iterator();
                while (itr.hasNext()) {
                    int i = itr.nextInt();
                    cardinality += i;
                }
                object = cardinality;
            }
        };
    }

    public Runnable arraySetMemory() {
        final int size = 10000;
        final int timestamps = 100;

        return new Runnable() {
            @Override
            public void run() {
                List<TimestampSet> trees = new ArrayList<TimestampSet>();
                Random rand = new Random(1234);
                for (int i = 0; i < size; i++) {
                    TimestampSet set = new TimestampSet(timestamps);
                    for (int j = 0; j < timestamps; j++) {
                        set.add(rand.nextInt(timestamps));
                        trees.add(set);
                    }
                }
                object = trees;
            }
        };
    }
}
