/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.index;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class IndexStringProcessorTest {

  @Test
  public void processIndexString() {
    final List<IndexEntry> acts =
        IndexStringProcessor.processIndexString(
            "Foo<$nopage><$singlepage><$startrange><$endrange>[foo]", emptyList());
    assertEquals(1, acts.size());
    final IndexEntry act = acts.get(0);
    assertEquals("Foo<$nopage><$singlepage><$startrange><$endrange>[foo]", act.getValue());
    assertEquals(
        "Foo<$nopage><$singlepage><$startrange><$endrange>[foo]", act.getFormattedString());
    assertEquals(0, act.getContents().size());
    assertEquals(null, act.getSortString());
    assertEquals(0, act.getChildIndexEntries().size());
    assertEquals(null, act.getSeeChildIndexEntries());
    assertEquals(null, act.getSeeAlsoChildIndexEntries());
    assertEquals(false, act.isStartingRange());
    assertEquals(false, act.isEndingRange());
    assertEquals(false, act.isSuppressesThePageNumber());
    assertEquals(
        singleton("Foo<$nopage><$singlepage><$startrange><$endrange>[foo]:"), act.getRefIDs());
  }

  @Test
  public void normalizeTextValue() {
    assertEquals("foo", IndexStringProcessor.normalizeTextValue("foo"));
    assertEquals("foo bar", IndexStringProcessor.normalizeTextValue("  foo \n bar  "));
    assertEquals("foo\0A0bar", IndexStringProcessor.normalizeTextValue("foo\0A0bar"));
    assertEquals(null, IndexStringProcessor.normalizeTextValue(null));
    assertEquals("", IndexStringProcessor.normalizeTextValue(""));
  }
}
