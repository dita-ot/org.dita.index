/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.index;

import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class IndexDitaProcessorTest {

    private final DocumentBuilder builder;
    private final IndexDitaProcessor processor;

    public IndexDitaProcessorTest() throws ParserConfigurationException {
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        processor = new IndexDitaProcessor();
        processor.setLogger(new DummyLogger());
    }

    @Test
    public void processIndexDitaNode() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>  Foo  Bar  </indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(0, entry.getChildIndexEntries().size());
        assertEquals("Foo Bar", entry.getValue());
        assertEquals(null, entry.getSortString());
        assertEquals("Foo Bar", entry.getFormattedString());
        assertEquals(Collections.singleton("Foo Bar:"), entry.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_framemaker() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>Foo [foo]</indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(0, entry.getChildIndexEntries().size());
        assertEquals("Foo ", entry.getValue());
        assertEquals("foo", entry.getSortString());
        assertEquals("Foo ", entry.getFormattedString());
        assertEquals(Collections.singleton("Foo :"), entry.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_empty() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>  </indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(0, indexEntries.size());
    }

    @Test
    public void processIndexDitaNode_element() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>  Foo <keyword class='- topic/keyword '> Bar </keyword> </indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(0, entry.getChildIndexEntries().size());
        assertEquals("Foo Bar", entry.getValue());
        assertEquals(null, entry.getSortString());
        assertEquals("Foo Bar", entry.getFormattedString());
        assertEquals(Collections.singleton("Foo Bar:"), entry.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_sortAs() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>Foo<index-sort-as class='+ topic/index-base indexing-d/index-sort-as '>foo</index-sort-as></indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(0, entry.getChildIndexEntries().size());
        assertEquals("Foo", entry.getValue());
        assertEquals("foo", entry.getSortString());
        assertEquals("Foo", entry.getFormattedString());
        assertEquals(Collections.singleton("Foo:"), entry.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_child() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>Foo<indexterm class='- topic/indexterm '>Bar</indexterm></indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(1, entry.getChildIndexEntries().size());
        assertEquals("Foo", entry.getValue());
        assertEquals(null, entry.getSortString());
        assertEquals("Foo", entry.getFormattedString());
        assertEquals(Collections.singleton("Foo:"), entry.getRefIDs());

        final IndexEntry child = entry.getChildIndexEntries().get(0);
        assertEquals(0, child.getChildIndexEntries().size());
        assertEquals("Bar", child.getValue());
        assertEquals(null, child.getSortString());
        assertEquals("Bar", child.getFormattedString());
        assertEquals(Collections.singleton("Foo:Bar:"), child.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_see() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>Foo<index-see class='+ topic/index-base indexing-d/index-see '>Bar</index-see><index-see-also class='+ topic/index-base indexing-d/index-see-also '>Baz</index-see-also></indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(0, entry.getChildIndexEntries().size());
        assertEquals("Foo", entry.getValue());
        assertEquals(null, entry.getSortString());
        assertEquals("Foo", entry.getFormattedString());
        assertEquals(Collections.singleton("Foo:"), entry.getRefIDs());

        final IndexEntry see = entry.getSeeChildIndexEntries().get(0);
        assertEquals(0, see.getChildIndexEntries().size());
        assertEquals("Bar", see.getValue());
        assertEquals(null, see.getSortString());
        assertEquals("Bar", see.getFormattedString());
        assertEquals(Collections.singleton("Bar:"), see.getRefIDs());

        final IndexEntry seeAlso = entry.getSeeAlsoChildIndexEntries().get(0);
        assertEquals(0, seeAlso.getChildIndexEntries().size());
        assertEquals("Baz", seeAlso.getValue());
        assertEquals(null, seeAlso.getSortString());
        assertEquals("Baz", seeAlso.getFormattedString());
        assertEquals(Collections.singleton("Baz:"), seeAlso.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_mixed() {
        final Node node = getNode("<indexterm class='- topic/indexterm '>Foo<indexterm class='- topic/indexterm '>Qux</indexterm><index-see class='+ topic/index-base indexing-d/index-see '>Bar</index-see><index-see-also class='+ topic/index-base indexing-d/index-see-also '>Baz</index-see-also></indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(1, entry.getChildIndexEntries().size());
        assertEquals(null, entry.getSeeChildIndexEntries());
        assertEquals(null, entry.getSeeAlsoChildIndexEntries());
        assertEquals("Foo", entry.getValue());
        assertEquals(null, entry.getSortString());
        assertEquals("Foo", entry.getFormattedString());
        assertEquals(Collections.singleton("Foo:"), entry.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_start() {
        final Node node = getNode("<indexterm class='- topic/indexterm ' start='foo'>Foo</indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(0, entry.getChildIndexEntries().size());
        assertEquals("Foo", entry.getValue());
        assertTrue(entry.isStartingRange());
        assertFalse(entry.isEndingRange());
        assertEquals(null, entry.getSortString());
        assertEquals("Foo", entry.getFormattedString());
        assertEquals(Collections.singleton("foo"), entry.getRefIDs());
    }

    @Test
    public void processIndexDitaNode_end() {
        final Node node = getNode("<indexterm class='- topic/indexterm ' end='foo'>Foo</indexterm>");
        final List<IndexEntry> indexEntries = processor.processIndexDitaNode(node, "");
        assertEquals(1, indexEntries.size());

        final IndexEntry entry = indexEntries.get(0);
        assertEquals(0, entry.getChildIndexEntries().size());
        assertEquals("Foo", entry.getValue());
        assertFalse(entry.isStartingRange());
        assertTrue(entry.isEndingRange());
        assertEquals(null, entry.getSortString());
        assertEquals("Foo", entry.getFormattedString());
        assertEquals(Collections.singleton("foo"), entry.getRefIDs());
    }

    private Node getNode(String s) {
        try {
            final InputSource in = new InputSource();
            in.setCharacterStream(new StringReader(s));
            return builder.parse(in).getDocumentElement();
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
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