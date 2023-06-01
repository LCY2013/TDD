package org.fufeng.tdd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SortTest {

    @Test
    public void should_sort_array() {
        assertArrayEquals(new int[]{0, 1, 2, 3, 4, 7, 8, 9}, Sort.bubble(4, 7, 3, 2, 8, 9, 0, 1));
    }

    @Test
    public void should_sort_smaller_array() {
        assertArrayEquals(new int[]{1, 2}, Sort.bubble(2, 1));
    }

    @Test
    public void should_sort() {
        Sort.SwapFunc func = mock(Sort.SwapFunc.class);

        Sort.bubble(func, 2, 1);

        verify(func).swap(eq(0), eq(1), any());
    }

    @Test
    public void should_sort_3_values() {
        Sort.SwapFunc func = mock(Sort.SwapFunc.class);

        Sort.bubble(func, 3, 2, 1);

        verify(func).swap(eq(0), eq(1), any());
        verify(func).swap(eq(0), eq(2), any());
        verify(func).swap(eq(1), eq(2), any());
    }

}