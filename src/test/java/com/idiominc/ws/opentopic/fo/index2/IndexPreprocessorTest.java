package com.idiominc.ws.opentopic.fo.index2;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.matchers.CompareMatcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;

import static org.junit.Assert.*;

public class IndexPreprocessorTest {

    private final IndexPreprocessor processor = new IndexPreprocessor("prefix", "namespace", false);

    private final DocumentBuilder builder;

    public IndexPreprocessorTest() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
    }

    @Test
    public void process() throws IOException, SAXException, TransformerException {
        try (InputStream src = getClass().getResourceAsStream("/src.xml");
             InputStream exp = getClass().getResourceAsStream("/exp.xml")) {
            final Document srcDoc = builder.parse(src);
            final IndexPreprocessResult result = processor.process(srcDoc);
            assertEquals(8, result.indexEntries.size());

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final ByteArrayOutputStream actString = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(result.document), new StreamResult(actString));
            final Document actDoc = builder.parse(new ByteArrayInputStream(actString.toByteArray()));

            final Document expDoc = builder.parse(exp);
            assertThat(actDoc,
                    CompareMatcher
                            .isIdenticalTo(expDoc)
                            .ignoreElementContentWhitespace()
                            .normalizeWhitespace()
            );
        }
    }

    @Test
    public void createAndAddIndexGroups() {
    }
}