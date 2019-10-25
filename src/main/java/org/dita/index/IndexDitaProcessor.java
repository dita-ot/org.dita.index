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

package org.dita.index;

import com.google.common.annotations.VisibleForTesting;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dita.index.IndexPreprocessor.VALUE_SEPARATOR;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.toList;

public final class IndexDitaProcessor {

    private static final String ATTR_START = "start";
    private static final String ATTR_END = "end";
    private static final String LT = "<";
    private static final String GT = ">";
    private static final String SORT_START = "[";
    private static final String SORT_END = "]";

    private DITAOTLogger logger;

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Read index terms from source XML.
     *
     * @param node        source indexterm element
     * @param parentValue parent value
     * @return index entries
     */
    public List<IndexEntry> processIndexDitaNode(final Node node, final String parentValue) {
        final NodeList childNodes = node.getChildNodes();
        final StringBuilder textValueBuffer = new StringBuilder();
        final List<Node> contents = new ArrayList<>();
        final StringBuilder sortStringBuffer = new StringBuilder();
        final boolean startRange = node.getAttributes().getNamedItem(ATTR_START) != null;
        final boolean endRange = node.getAttributes().getNamedItem(ATTR_END) != null;
        final List<IndexEntry> childEntrys = new ArrayList<>();
        final List<IndexEntry> seeEntry = new ArrayList<>();
        final List<IndexEntry> seeAlsoEntry = new ArrayList<>();

        for (int i = 0; i < childNodes.getLength(); i++) { //Go through child nodes to find text nodes
            final Node child = childNodes.item(i);
            switch (child.getNodeType()) {
                case Node.TEXT_NODE:
                    contents.add(child);
                    final String val = child.getNodeValue();
                    if (null != val) {
                        textValueBuffer.append(val);
                    }
                    break;
                case Node.ELEMENT_NODE:
                    if (TOPIC_INDEXTERM.matches(child)) {
                        final String currentTextValue = normalizeTextValue(textValueBuffer.toString());
                        final String currentRefId = currentTextValue.isEmpty() ? "" : (currentTextValue + VALUE_SEPARATOR);
                        childEntrys.addAll(processIndexDitaNode(child, parentValue + currentRefId));
                    } else if (INDEXING_D_INDEX_SORT_AS.matches(child)) {
                        final List<Node> children = toList(child.getChildNodes());
                        for (final Node sortChildNode : children) {
                            if (sortChildNode.getNodeType() == Node.TEXT_NODE) {
                                final String text = sortChildNode.getNodeValue();
                                if (text != null) {
                                    sortStringBuffer.append(text);
                                }
                            }
                        }
                    } else if (INDEXING_D_INDEX_SEE.matches(child)) {
                        seeEntry.addAll(processIndexDitaNode(child, ""));
                    } else if (INDEXING_D_INDEX_SEE_ALSO.matches(child)) {
                        seeAlsoEntry.addAll(processIndexDitaNode(child, ""));
                    } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                        contents.add(child);
                        textValueBuffer.append(XMLUtils.getStringValue((Element) child));
                    }
                    break;
            }
        }
        /*
        if (normalizeTextValue(textValueBuffer.toString()).length() == 0) {
            if (startRange) {
                textValueBuffer.append(node.getAttributes().getNamedItem(elIndexRangeStartName).getNodeValue());
            } else if (endRange) {
                textValueBuffer.append(node.getAttributes().getNamedItem(elIndexRangeEndName).getNodeValue());
            }
        }
         */
        String textValue = normalizeTextValue(textValueBuffer.toString());
        String sortString = sortStringBuffer.toString();
        if (textValue.contains(SORT_START) && textValue.contains(SORT_END) && sortString.length() == 0) {
            if (textValue.indexOf(SORT_START) < textValue.indexOf(SORT_END)) {
                sortString = textValue.substring(textValue.indexOf(SORT_START) + 1, textValue.indexOf(SORT_END));
                textValue = textValue.substring(0, textValue.indexOf(SORT_START));
            }
        }

        if (!childEntrys.isEmpty() && !seeEntry.isEmpty()) {
            for (final IndexEntry e : seeEntry) {
                logger.warn(MessageUtils.getMessage("DOTA067W", e.getFormattedString(), textValue).toString());
            }
            seeEntry.clear();
        }
        if (!childEntrys.isEmpty() && !seeAlsoEntry.isEmpty()) {
            for (final IndexEntry e : seeAlsoEntry) {
                logger.warn(MessageUtils.getMessage("DOTA068W", e.getFormattedString(), textValue).toString());
            }
            seeAlsoEntry.clear();
        }

        final IndexEntry result = new IndexEntryImpl(textValue, sortString.isEmpty() ? null : sortString, textValue, contents);
        if (!result.getValue().isEmpty() || endRange || startRange) {
            result.setStartRange(startRange);
            result.setEndsRange(endRange);
            if (startRange) {
                result.addRefID(node.getAttributes().getNamedItem(ATTR_START).getNodeValue());
            } else if (endRange) {
                result.addRefID(node.getAttributes().getNamedItem(ATTR_END).getNodeValue());
            } else {
                result.addRefID(normalizeTextValue(parentValue + textValue + VALUE_SEPARATOR));
            }
            if (!seeEntry.isEmpty()) {
                for (final IndexEntry seeIndexEntry : seeEntry) {
                    result.addSeeChild(seeIndexEntry);
                }
                result.setSuppressesThePageNumber(true);
            }
            if (!seeAlsoEntry.isEmpty()) {
                for (final IndexEntry seeAlsoIndexEntry : seeAlsoEntry) {
                    result.addSeeAlsoChild(seeAlsoIndexEntry);
                }
            }
            for (final IndexEntry child : childEntrys) {
                result.addChild(child);
            }
            return Collections.singletonList(result);
        } else {
            return childEntrys;
        }
    }

    @VisibleForTesting
    static String stripFormatting(final String value) {
        final int ltPos = value.indexOf(LT);
        final int gtPos = value.indexOf(GT);
        if ((ltPos == -1) && (gtPos == -1)) {
            return value;
        } else if (ltPos == -1 || gtPos == -1 || (ltPos > gtPos)) {
            System.err.println("Possibly bad formatting in string \"" + value + "\"");
            return value;
        }
        return stripFormatting(value.substring(0, ltPos) + value.substring(gtPos + 1));
    }

    @VisibleForTesting
    static String normalizeTextValue(final String string) {
        if (null != string && !string.isEmpty()) {
            String res = string.replaceAll("[\\s\\n]+", " ").trim();
            res = res.replaceAll("[\\s]+$", ""); //replace in the end of string
            return res;
        }
        return string;
    }

}
