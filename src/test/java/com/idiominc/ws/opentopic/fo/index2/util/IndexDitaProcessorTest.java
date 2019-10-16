/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package com.idiominc.ws.opentopic.fo.index2.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class IndexDitaProcessorTest {

    private final IndexDitaProcessor processor = new IndexDitaProcessor();

    @Test
    public void processIndexDitaNode() {
    }

    @Test
    public void stripFormatting() {
        assertEquals("foo", processor.stripFormatting("foo"));
        assertEquals("foo", processor.stripFormatting("foo<>"));
        assertEquals("foo<", processor.stripFormatting("foo<"));
        assertEquals("foo>", processor.stripFormatting("foo>"));
        assertEquals("", processor.stripFormatting("<foo>"));
        assertEquals("Foo[foo]", processor.stripFormatting("Foo<$nopage><$singlepage><$startrange><$endrange>[foo]"));
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