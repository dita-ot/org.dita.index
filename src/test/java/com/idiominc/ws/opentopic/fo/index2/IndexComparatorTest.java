/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package com.idiominc.ws.opentopic.fo.index2;

import com.idiominc.ws.opentopic.fo.index2.util.IndexEntryImpl;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class IndexComparatorTest {

    @Test
    public void compare() {
        final IndexComparator comparator = new IndexComparator(Locale.ENGLISH);
        final IndexEntryImpl o1 = new IndexEntryImpl("foo", null, "foo", Collections.emptyList());
        final IndexEntryImpl o2 = new IndexEntryImpl("bar", null, "bar", Collections.emptyList());
        assertEquals(1, comparator.compare(o1, o2));
        assertEquals(0, comparator.compare(o1, o1));
        assertEquals(-1, comparator.compare(o2, o1));
    }

    @Test
    public void compare_withSortString() {
        final IndexComparator comparator = new IndexComparator(Locale.ENGLISH);
        final IndexEntryImpl o1 = new IndexEntryImpl("bar", "foo", "foo", Collections.emptyList());
        final IndexEntryImpl o2 = new IndexEntryImpl("foo", "bar", "bar", Collections.emptyList());
        assertEquals(1, comparator.compare(o1, o2));
        assertEquals(0, comparator.compare(o1, o1));
        assertEquals(-1, comparator.compare(o2, o1));
    }
}