package org.fufeng.tdd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Sort {

    public static int[] bubble(int ...arrays) {
        return bubble(Sort::swap, arrays);
    }

    private static void swap(int i, int j, int[] arrays) {
        int temp = arrays[i];
        arrays[i] = arrays[j];
        arrays[j] = temp;
    }

    interface SwapFunc {
        void swap(int i, int j, int[] arrays);
    }

    public static int[] bubble(SwapFunc func, int ...arrays) {
        for (int i = 0; i < arrays.length; i++) {
            for (int j = i+1; j < arrays.length; j++) {
                if (arrays[i] > arrays[j]) {
                    func.swap(i, j, arrays);
                }
            }
        }
        return arrays;
    }


}
