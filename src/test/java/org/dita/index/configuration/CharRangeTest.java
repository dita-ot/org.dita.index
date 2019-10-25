package org.dita.index.configuration;

import org.dita.index.IndexCollator;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharRangeTest {

    @Test
    public void isInRange() {
        final CharRange range = new CharRange("b", "e");
        final IndexCollator collator = new IndexCollator(Locale.ENGLISH);
        assertFalse(range.isInRange("a", collator));
        assertFalse(range.isInRange("b", collator));
        assertTrue(range.isInRange("c", collator));
        assertTrue(range.isInRange("d", collator));
        assertFalse(range.isInRange("e", collator));
        assertFalse(range.isInRange("f", collator));
    }
}