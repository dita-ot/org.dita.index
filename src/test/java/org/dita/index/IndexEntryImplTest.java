package org.dita.index;

import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.junit.Assert.*;

public class IndexEntryImplTest {

    private IndexEntryImpl self;

    @Before
    public void setUp() {
        this.self = createIndexEntry("Root");
    }

    private IndexEntryImpl createIndexEntry(final String value, final IndexEntry... children) {
        final IndexEntryImpl res = new IndexEntryImpl(value, value.toLowerCase(), value, emptyList());
        for (IndexEntry child : children) {
            res.addChild(child);
        }
        return res;
    }

    @Test
    public void addChild() {
        self.addChild(createIndexEntry("Bar"));
        assertEquals(1, self.getChildIndexEntries().size());

        self.addChild(createIndexEntry("Bar", createIndexEntry("Child A")));
        assertEquals(1, self.getChildIndexEntries().size());
        assertEquals(1, self.getChildIndexEntries().get(0).getChildIndexEntries().size());
        assertFalse(self.getChildIndexEntries().get(0).isEndingRange());
        assertFalse(self.getChildIndexEntries().get(0).isRestoresPageNumber());
        assertFalse(self.getChildIndexEntries().get(0).isStartingRange());
        assertFalse(self.getChildIndexEntries().get(0).isSuppressesThePageNumber());

        final IndexEntryImpl indexEntry = createIndexEntry("Bar", createIndexEntry("Child B"));
        indexEntry.setRestoresPageNumber(true);
        indexEntry.setStartRange(true);
        self.addChild(indexEntry);
        assertEquals(1, self.getChildIndexEntries().size());
        assertEquals(2, self.getChildIndexEntries().get(0).getChildIndexEntries().size());
        assertFalse(self.getChildIndexEntries().get(0).isEndingRange());
        assertTrue(self.getChildIndexEntries().get(0).isRestoresPageNumber());
        assertTrue(self.getChildIndexEntries().get(0).isStartingRange());
        assertFalse(self.getChildIndexEntries().get(0).isSuppressesThePageNumber());
    }

    @Test
    public void addSeeChild() {
        self.addSeeChild(createIndexEntry("Bar"));
        assertEquals(1, self.getSeeChildIndexEntries().size());

        self.addSeeChild(createIndexEntry("Bar", createIndexEntry("Child A")));
        assertEquals(1, self.getSeeChildIndexEntries().size());
        assertEquals(1, self.getSeeChildIndexEntries().get(0).getChildIndexEntries().size());
        assertFalse(self.getSeeChildIndexEntries().get(0).isEndingRange());
        assertFalse(self.getSeeChildIndexEntries().get(0).isRestoresPageNumber());
        assertFalse(self.getSeeChildIndexEntries().get(0).isStartingRange());
        assertFalse(self.getSeeChildIndexEntries().get(0).isSuppressesThePageNumber());

        final IndexEntryImpl indexEntry = createIndexEntry("Bar", createIndexEntry("Child B"));
        indexEntry.setRestoresPageNumber(true);
        indexEntry.setStartRange(true);
        self.addSeeChild(indexEntry);
        assertEquals(1, self.getSeeChildIndexEntries().size());
        assertEquals(2, self.getSeeChildIndexEntries().get(0).getChildIndexEntries().size());
        assertFalse(self.getSeeChildIndexEntries().get(0).isEndingRange());
        assertTrue(self.getSeeChildIndexEntries().get(0).isRestoresPageNumber());
        assertTrue(self.getSeeChildIndexEntries().get(0).isStartingRange());
        assertFalse(self.getSeeChildIndexEntries().get(0).isSuppressesThePageNumber());
    }

    @Test
    public void addSeeAlsoChild() {
        self.addSeeAlsoChild(createIndexEntry("Bar"));
        assertEquals(1, self.getSeeAlsoChildIndexEntries().size());

        self.addSeeAlsoChild(createIndexEntry("Bar", createIndexEntry("Child A")));
        assertEquals(1, self.getSeeAlsoChildIndexEntries().size());
        assertEquals(1, self.getSeeAlsoChildIndexEntries().get(0).getChildIndexEntries().size());
        assertFalse(self.getSeeAlsoChildIndexEntries().get(0).isEndingRange());
        assertFalse(self.getSeeAlsoChildIndexEntries().get(0).isRestoresPageNumber());
        assertFalse(self.getSeeAlsoChildIndexEntries().get(0).isStartingRange());
        assertFalse(self.getSeeAlsoChildIndexEntries().get(0).isSuppressesThePageNumber());

        final IndexEntryImpl indexEntry = createIndexEntry("Bar", createIndexEntry("Child B"));
        indexEntry.setRestoresPageNumber(true);
        indexEntry.setStartRange(true);
        self.addSeeAlsoChild(indexEntry);
        assertEquals(1, self.getSeeAlsoChildIndexEntries().size());
        assertEquals(2, self.getSeeAlsoChildIndexEntries().get(0).getChildIndexEntries().size());
        assertFalse(self.getSeeAlsoChildIndexEntries().get(0).isEndingRange());
        assertTrue(self.getSeeAlsoChildIndexEntries().get(0).isRestoresPageNumber());
        assertTrue(self.getSeeAlsoChildIndexEntries().get(0).isStartingRange());
        assertFalse(self.getSeeAlsoChildIndexEntries().get(0).isSuppressesThePageNumber());
    }

    @Test
    public void setStartRange() {
        self.setStartRange(true);
        assertTrue(self.isStartingRange());
        assertFalse(self.isEndingRange());
        self.setEndsRange(true);
        assertFalse(self.isStartingRange());
        assertTrue(self.isEndingRange());
    }

    @Test
    public void setStartRange_end() {
        self.setEndsRange(true);
        self.setStartRange(true);
        assertTrue(self.isStartingRange());
        assertFalse(self.isEndingRange());
    }

    @Test
    public void setEndsRange() {
        self.setEndsRange(true);
        assertFalse(self.isStartingRange());
        assertTrue(self.isEndingRange());
    }

    @Test
    public void setEndsRange_start() {
        self.setStartRange(true);
        self.setEndsRange(true);
        assertFalse(self.isStartingRange());
        assertTrue(self.isEndingRange());
    }

    @Test
    public void setSuppressesThePageNumber() {
        self.setSuppressesThePageNumber(true);
        assertTrue(self.isSuppressesThePageNumber());
        assertFalse(self.isRestoresPageNumber());
    }

    @Test
    public void setSuppressesThePageNumber_restores() {
        self.setRestoresPageNumber(true);
        self.setSuppressesThePageNumber(true);
        assertTrue(self.isSuppressesThePageNumber());
        assertFalse(self.isRestoresPageNumber());
    }

    @Test
    public void setRestoresPageNumber() {
        self.setRestoresPageNumber(true);
        assertFalse(self.isSuppressesThePageNumber());
        assertTrue(self.isRestoresPageNumber());
    }

    @Test
    public void setRestoresPageNumber_suppresses() {
        self.setSuppressesThePageNumber(true);
        self.setRestoresPageNumber(true);
        assertFalse(self.isSuppressesThePageNumber());
        assertTrue(self.isRestoresPageNumber());
    }

}
