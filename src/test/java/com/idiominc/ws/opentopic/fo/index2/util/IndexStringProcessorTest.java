/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package com.idiominc.ws.opentopic.fo.index2.util;

import com.idiominc.ws.opentopic.fo.index2.IndexEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

public class IndexStringProcessorTest {

    private IndexStringProcessor processor;

    @Before
    public void setUp() {
        processor = new IndexStringProcessor() {
        };
    }

    @Test
    public void processIndexString() {
        final List<IndexEntry> acts = processor.processIndexString(
                "Foo<$nopage><$singlepage><$startrange><$endrange>[foo]",
                emptyList());
        assertEquals(1, acts.size());
        final IndexEntry act = acts.get(0);
        assertEquals("Foo<$nopage><$singlepage><$startrange><$endrange>[foo]", act.getValue());
        assertEquals("Foo<$nopage><$singlepage><$startrange><$endrange>[foo]", act.getFormattedString());
        assertEquals(0, act.getContents().size());
        assertEquals(null, act.getSortString());
        assertEquals(0, act.getChildIndexEntries().size());
        assertEquals(0, act.getSeeChildIndexEntries().size());
        assertEquals(0, act.getSeeAlsoChildIndexEntries().size());
        assertEquals(false, act.isStartingRange());
        assertEquals(false, act.isEndingRange());
        assertEquals(false, act.isSuppressesThePageNumber());
        assertEquals(singleton("Foo<$nopage><$singlepage><$startrange><$endrange>[foo]:"), act.getRefIDs());
    }

    @Test
    public void normalizeTextValue() {
        assertEquals("foo", processor.normalizeTextValue("foo"));
        assertEquals("foo bar", processor.normalizeTextValue("  foo \n bar  "));
        assertEquals("foo\0A0bar", processor.normalizeTextValue("foo\0A0bar"));
        assertEquals(null, processor.normalizeTextValue(null));
        assertEquals("", processor.normalizeTextValue(""));
    }
}