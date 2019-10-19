package com.idiominc.ws.opentopic.fo.index2;

import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;
import com.idiominc.ws.opentopic.fo.index2.util.IndexDitaProcessor;
import com.idiominc.ws.opentopic.fo.index2.util.IndexStringProcessor;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import java.util.*;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static org.dita.dost.util.Constants.*;

/*
Copyright (c) 2004-2006 by Idiom Technologies, Inc. All rights reserved.
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other
trademarks are the property of their respective owners.

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project.
See the accompanying LICENSE file for applicable license.
 */
public final class IndexPreprocessor {

    /**
     * Index term level separator.
     */
    public static final String VALUE_SEPARATOR = ":";
    private static final String EL_INDEX_RANGE_START_NAME = "start";
    private static final String EL_INDEX_RANGE_END_NAME = "end";
    private static final String HASH_PREFIX = "indexid";

    private final String prefix;
    private final String namespaceUrl;
    private final Deque<Boolean> excludedDraftSection = new ArrayDeque<>();
    private final IndexDitaProcessor indexDitaProcessor;
    private final IndexGroupProcessor indexGroupProcessor;
    private boolean includeDraft;

    /**
     * Create new index preprocessor.
     *
     * @param prefix           index prefix
     * @param namespaceUrl index element namespace URI
     */
    public IndexPreprocessor(final String prefix, final String namespaceUrl, final boolean includeDraft) {
        this.prefix = prefix;
        this.namespaceUrl = namespaceUrl;
        this.excludedDraftSection.clear();
        this.excludedDraftSection.add(false);
        this.includeDraft = includeDraft;
        indexDitaProcessor = new IndexDitaProcessor();
        indexGroupProcessor = new IndexGroupProcessor();
    }

    public void setLogger(final DITAOTLogger logger) {
        indexDitaProcessor.setLogger(logger);
        indexGroupProcessor.setLogger(logger);
    }

    /**
     * Process index terms. Walks through source document and builds an array of IndexEntry and builds a new document
     * with pre-processed index entries included.
     *
     * @param input input document
     * @return read index terms
     */
    public IndexPreprocessResult process(final Document input) {
        final DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();
        final Document doc = documentBuilder.newDocument();
        final Node rootElement = input.getDocumentElement();
        final List<IndexEntry> indexes = new ArrayList<>();
        final Node node = processCurrNode(rootElement, doc, indexes::add).get(0);
        doc.appendChild(node);
        doc.getDocumentElement().setAttribute(XMLNS_ATTRIBUTE + ":" + this.prefix, this.namespaceUrl);
        return new IndexPreprocessResult(doc, indexes);
    }

    /**
     * Append index groups to the end of document
     */
    public void createAndAddIndexGroups(final Collection<IndexEntry> indexEntries, final IndexConfiguration configuration,
                                        final Document document, final Locale locale) {
        final IndexComparator indexEntryComparator = new IndexComparator(locale);
        final List<IndexGroup> indexGroups = indexGroupProcessor.process(indexEntries, configuration, locale);
        final Element rootElement = document.getDocumentElement();
        final Element indexGroupsElement = document.createElementNS(namespaceUrl, "index.groups");
        indexGroupsElement.setPrefix(prefix);
        for (final IndexGroup group : indexGroups) {
            //Create group element
            final Node groupElement = document.createElementNS(namespaceUrl, "index.group");
            groupElement.setPrefix(prefix);
            //Create group label element and index entry childs
            final Element groupLabelElement = document.createElementNS(namespaceUrl, "label");
            groupLabelElement.setPrefix(prefix);
            groupLabelElement.appendChild(document.createTextNode(group.getLabel()));
            groupElement.appendChild(groupLabelElement);
            final List<Node> entryNodes = transformToNodes(new ArrayList(group.getEntries()), document, indexEntryComparator);
            for (final Node entryNode : entryNodes) {
                groupElement.appendChild(entryNode);
            }
            indexGroupsElement.appendChild(groupElement);
        }
        rootElement.appendChild(indexGroupsElement);
    }

    /**
     * Processes curr node. Copies node to the target document if its is not a text node of index entry element.
     * Otherwise it process it and creates nodes with "prefix" in given "namespace_url" from the parsed index entry text.
     *
     * @param node                    node to process
     * @param targetDocument          target document used to import and create nodes
     * @param indexEntryFoundListener listener to notify that new index entry was found
     * @return the array of nodes after processing input node
     */
    private List<Node> processCurrNode(final Node node, final Document targetDocument, final IndexEntryFoundListener indexEntryFoundListener) {

        if (isDitaIndexElement(node) && !excludedDraftSection.peek()) {
            return processIndexNode(node, targetDocument, indexEntryFoundListener);
        } else {
            final Node result = targetDocument.importNode(node, false);
            if (!includeDraft && checkDraftNode(node)) {
                excludedDraftSection.add(true);
            }
            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final List<Node> processedNodes = processCurrNode(childNodes.item(i), targetDocument, indexEntryFoundListener);
                for (final Node processedNode : processedNodes) {
                    result.appendChild(processedNode);
                }
            }
            if (!includeDraft && checkDraftNode(node)) {
                excludedDraftSection.pop();
            }
            return Collections.singletonList(result);
        }
    }

    private List<Node> processIndexNode(final Node node, final Document targetDocument, final IndexEntryFoundListener indexEntryFoundListener) {
        node.normalize();

        boolean ditastyle = false;

        final NodeList childNodes = node.getChildNodes();
        final StringBuilder textBuf = new StringBuilder();
        final List<Node> contents = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (isDitaIndexElement(child)) {
                ditastyle = true;
                break;
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                textBuf.append(XMLUtils.getStringValue((Element) child));
                contents.add(child);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                textBuf.append(child.getNodeValue());
                contents.add(child);
            }
        }

        String textNode = IndexStringProcessor.normalizeTextValue(textBuf.toString());
        if (textNode.isEmpty()) {
            textNode = null;
        }

        if (node.getAttributes().getNamedItem(EL_INDEX_RANGE_START_NAME) != null ||
                node.getAttributes().getNamedItem(EL_INDEX_RANGE_END_NAME) != null) {
            ditastyle = true;
        }

        final List<Node> res = new ArrayList<>();
        if (ditastyle) {
            final List<IndexEntry> indexEntries = indexDitaProcessor.processIndexDitaNode(node, "");

            for (final IndexEntry indexEntrie : indexEntries) {
                indexEntryFoundListener.foundEntry(indexEntrie);
            }

            final List<Node> nodes = transformToNodes(indexEntries, targetDocument, null);
            res.addAll(nodes);
        } else if (textNode != null) {
            final List<Node> nodes = processIndexString(textNode, contents, targetDocument, indexEntryFoundListener);
            res.addAll(nodes);
        } else {
            return Collections.emptyList();
        }

        return res;

    }

    /**
     * Check if node is an index term element or specialization of one.
     *
     * @param node element to test
     * @return {@code true} if node is an index term element, otherwise {@code false}
     */
    private boolean isDitaIndexElement(final Node node) {
        return TOPIC_INDEXTERM.matches(node)
                || INDEXING_D_INDEX_SORT_AS.matches(node)
                || INDEXING_D_INDEX_SEE.matches(node)
                || INDEXING_D_INDEX_SEE_ALSO.matches(node);
    }

    private boolean checkDraftNode(final Node node) {
        return TOPIC_DRAFT_COMMENT.matches(node)
                || TOPIC_REQUIRED_CLEANUP.matches(node);
    }

    /**
     * Processes index string and creates nodes with "prefix" in given "namespace_url" from the parsed index entry text.
     *
     * @param theIndexString             index string
     *                                   param contents index contents
     * @param theTargetDocument          target document to create new nodes
     * @param theIndexEntryFoundListener listener to notify that new index entry was found
     * @return the array of nodes after processing index string
     */
    private List<Node> processIndexString(final String theIndexString, final List<Node> contents, final Document theTargetDocument, final IndexEntryFoundListener theIndexEntryFoundListener) {
        final List<IndexEntry> indexEntries = IndexStringProcessor.processIndexString(theIndexString, contents);

        for (final IndexEntry indexEntrie : indexEntries) {
            theIndexEntryFoundListener.foundEntry(indexEntrie);
        }

        return transformToNodes(indexEntries, theTargetDocument, null);
    }

    /**
     * Creates nodes from index entries
     *
     * @param indexEntries         index entries
     * @param targetDocument       target document
     * @param indexEntryComparator comparator to sort the index entries. if it is null the index entries will be unsorted
     * @return nodes for the target document
     */
    private List<Node> transformToNodes(final List<IndexEntry> indexEntries, final Document targetDocument, final Comparator<IndexEntry> indexEntryComparator) {
        if (null != indexEntryComparator) {
            Collections.sort(indexEntries, indexEntryComparator);
        }

        final List<Node> result = new ArrayList<>();
        for (final IndexEntry indexEntry : indexEntries) {
            final Element indexEntryNode = createElement(targetDocument, "index.entry");

            final Element formattedStringElement = createElement(targetDocument, "formatted-value");
            if (indexEntry.getContents() != null) {
                for (final Iterator<Node> i = indexEntry.getContents().iterator(); i.hasNext(); ) {
                    final Node child = i.next();
                    final Node clone = targetDocument.importNode(child, true);
                    if (!i.hasNext() && clone.getNodeType() == Node.TEXT_NODE) {
                        final Text t = (Text) clone;
                        t.setData(t.getData().replaceAll("[\\s\\n]+$", ""));
                    }
                    formattedStringElement.appendChild(clone);
                }
            } else {
                final Text textNode = targetDocument.createTextNode(indexEntry.getFormattedString());
                textNode.normalize();
                formattedStringElement.appendChild(textNode);
            }
            indexEntryNode.appendChild(formattedStringElement);

            final Set<String> refIDs = indexEntry.getRefIDs();
            for (final String refID : refIDs) {
                final Element referenceIDElement = createElement(targetDocument, "refID");
                referenceIDElement.setAttribute("indexid", HASH_PREFIX + refID.hashCode());
                referenceIDElement.setAttribute("value", refID);
                indexEntryNode.appendChild(referenceIDElement);
            }

            final String val = indexEntry.getValue();
            if (null != val) {
                indexEntryNode.setAttribute("value", val);
            }

            final String sort = indexEntry.getSortString();
            if (null != sort) {
                indexEntryNode.setAttribute("sort-string", sort);
            }

            if (indexEntry.isStartingRange()) {
                indexEntryNode.setAttribute("start-range", "true");
            } else if (indexEntry.isEndingRange()) {
                indexEntryNode.setAttribute("end-range", "true");
            }
            if (indexEntry.isSuppressesThePageNumber()) {
                indexEntryNode.setAttribute("no-page", "true");
            } else if (indexEntry.isRestoresPageNumber()) {
                indexEntryNode.setAttribute("single-page", "true");
            }

            final List<IndexEntry> childIndexEntries = indexEntry.getChildIndexEntries();

            final List<Node> nodes = transformToNodes(childIndexEntries, targetDocument, indexEntryComparator);

            for (final Node node : nodes) {
                indexEntryNode.appendChild(node);
            }

            final List<IndexEntry> seeChildIndexEntries = indexEntry.getSeeChildIndexEntries();
            if (seeChildIndexEntries != null) {
                final Element seeElement = createElement(targetDocument, "see-childs");
                final List<Node> seeNodes = transformToNodes(seeChildIndexEntries, targetDocument, indexEntryComparator);
                for (final Node node : seeNodes) {
                    seeElement.appendChild(node);
                }

                indexEntryNode.appendChild(seeElement);
            }

            final List<IndexEntry> seeAlsoChildIndexEntries = indexEntry.getSeeAlsoChildIndexEntries();
            if (seeAlsoChildIndexEntries != null) {
                final Element seeAlsoElement = createElement(targetDocument, "see-also-childs");
                final List<Node> seeAlsoNodes = transformToNodes(seeAlsoChildIndexEntries, targetDocument, indexEntryComparator);
                for (final Node node : seeAlsoNodes) {
                    seeAlsoElement.appendChild(node);
                }

                indexEntryNode.appendChild(seeAlsoElement);
            }

            result.add(indexEntryNode);
        }
        return result;
    }

    /**
     * Creates element with "prefix" in "namespace_url" with given name for the target document
     *
     * @param targetDocument target document
     * @param name           name
     * @return new element
     */
    private Element createElement(final Document targetDocument, final String name) {
        final Element indexEntryNode = targetDocument.createElementNS(namespaceUrl, name);
        indexEntryNode.setPrefix(prefix);
        return indexEntryNode;
    }
}
