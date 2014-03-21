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
package org.gephi.graph.store;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author mbastian
 */
public class NumberGenerator {

    public static int[] sortAndRemoveDuplicates(int[] array) {
        IntSet set = new IntOpenHashSet();
        for (int d : array) {
            if (!set.contains(d)) {
                set.add(d);
            }
        }
        int[] res = set.toIntArray();
        Arrays.sort(res);
        return res;
    }

    public static int[] generateRandomInt(int count, boolean duplicates) {
        Random rand = new Random(12336);
        if (duplicates) {
            int[] res = new int[count];
            for (int i = 0; i < 2 * res.length / 3; i++) {
                int d = rand.nextInt(count * 10);
                res[i] = d;
            }
            for (int i = 2 * res.length / 3; i < res.length; i++) {
                int d = res[rand.nextInt(2 * res.length / 3)];
                res[i] = d;
            }
            for (int i = 0; i < res.length; i++) {
                int p = rand.nextInt(count);
                int d = res[i];
                res[i] = res[p];
                res[p] = d;
            }
            return res;
        } else {
            IntSet intSet = new IntOpenHashSet();
            int[] res = new int[count];
            for (int i = 0; i < res.length;) {
                int d = rand.nextInt(count * 10);
                if (!intSet.contains(d)) {
                    res[i] = d;
                    i++;
                    intSet.add(d);
                }
            }
            return res;
        }
    }
}
