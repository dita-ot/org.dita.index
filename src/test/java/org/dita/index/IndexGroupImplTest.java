package org.dita.index;

import org.dita.index.configuration.ConfigEntryImpl;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class IndexGroupImplTest {

    private IndexGroupImpl self;

    @Before
    public void setUp() {
        final ConfigEntryImpl configEntry = createConfigEntry("A", "B", "C");
        self = new IndexGroupImpl("label", configEntry);
    }

    @Test
    public void addEntry() {
        final IndexEntryImpl entry = createIndexEntry("A");
        self.addEntry(entry);

        assertEquals(1, self.getEntries().size());
    }

    @Test
    public void addEntry_child() {
        final IndexGroupImpl c1 = new IndexGroupImpl("child", createConfigEntry("D", "E", "F"));
        self.addChild(c1);
        final IndexGroupImpl c2 = new IndexGroupImpl("child", createConfigEntry("A", "B", "C"));
        self.addChild(c2);
        final IndexGroupImpl c3 = new IndexGroupImpl("child", createConfigEntry("A", "B", "C"));
        self.addChild(c3);
        final IndexEntryImpl entry = createIndexEntry("A");
        self.addEntry(entry);

        assertEquals(0, self.getEntries().size());
        assertEquals(0, c1.getEntries().size());
        assertEquals(1, c2.getEntries().size());
        assertEquals(0, c3.getEntries().size());
    }

    private IndexEntryImpl createIndexEntry(final String value) {
        return new IndexEntryImpl(value, null, value, emptyList());
    }

    private ConfigEntryImpl createConfigEntry(final String... members) {
        return new ConfigEntryImpl("label", "key", asList(members));
    }

    @Test
    public void addChild() {
        final IndexGroupImpl c1 = new IndexGroupImpl("a", createConfigEntry("A"));
        self.addChild(c1);
        final IndexGroupImpl c2 = new IndexGroupImpl("b", createConfigEntry("Ab", "B"));
        self.addChild(c2);
        final IndexGroupImpl c3 = new IndexGroupImpl("c", createConfigEntry("C"));
        self.addChild(c3);
        // XXX: the results of addChild are not exposed in any way
    }
}