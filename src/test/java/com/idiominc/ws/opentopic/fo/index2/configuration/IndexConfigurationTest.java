package com.idiominc.ws.opentopic.fo.index2.configuration;

import com.idiominc.ws.opentopic.fo.index2.IndexCollator;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class IndexConfigurationTest {

    private final DocumentBuilder builder;

    public IndexConfigurationTest() throws ParserConfigurationException {
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Test
    public void getEntries() {
    }

    @Test
    public void parse() throws ParseException, IOException, SAXException {
        final Document doc;
        try (InputStream in = getClass().getResourceAsStream("/index/en.xml")) {
            doc = builder.parse(in);
        }

        final IndexConfiguration act = IndexConfiguration.parse(doc);
        assertEquals(28, act.getEntries().size());
        final ConfigEntry specials = act.getEntries().get(0);
        assertEquals("Specials", specials.getKey());
        assertEquals("Special Characters", specials.getLabel());
        assertEquals(32, specials.getGroupMembers().size());
        assertEquals(
                asList("\\", "`", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "=", "+", "'", "\"", ";", ":", "<", ">", ".", ",", "/", "?", "[", "]", "{", "}", "|"),
                specials.getGroupMembers());
        final ConfigEntry numbers = act.getEntries().get(1);
        assertEquals("Numbers", numbers.getKey());
        assertEquals("Numerics", numbers.getLabel());
        assertEquals(20, numbers.getGroupMembers().size());
        assertEquals(
                asList("0", "０", "1", "１", "2", "２", "3", "３", "4", "４", "5", "５", "6", "６", "7", "７", "8", "８", "9", "９"),
                numbers.getGroupMembers());
        final ConfigEntry a = act.getEntries().get(2);
        assertEquals("A", a.getKey());
        assertEquals("A", a.getLabel());
        assertEquals(2, a.getGroupMembers().size());
        assertEquals(
                asList("A", "a"),
                a.getGroupMembers());
    }

    @Test
    public void parseRange() throws ParseException, IOException, SAXException {
        final Document doc;
        try (InputStream in = getClass().getResourceAsStream("/index/range.xml")) {
            doc = builder.parse(in);
        }

        final IndexConfiguration act = IndexConfiguration.parse(doc);
        final ConfigEntry range = act.getEntries().get(0);
        assertFalse(range.isInRange("a", new IndexCollator(Locale.ENGLISH)));
        assertTrue(range.isInRange("b", new IndexCollator(Locale.ENGLISH)));
        assertTrue(range.isInRange("c", new IndexCollator(Locale.ENGLISH)));
        assertFalse(range.isInRange("d", new IndexCollator(Locale.ENGLISH)));
    }
}