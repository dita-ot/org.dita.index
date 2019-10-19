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

package com.idiominc.ws.opentopic.fo.index2.configuration;

import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IndexConfiguration {

    static final String ELEM_INDEX_CONFIGURATION_SET = "index.configuration.set";
    static final String ELEM_INDEX_CONFIGURATION = "index.configuration";
    static final String INDEX_GROUPS = "index.groups";
    static final String ELEM_INDEX_GROUP = "index.group";
    static final String ELEM_GROUP_KEY = "group.key";
    static final String GROUP_LABEL = "group.label";
    static final String ELEM_GROUP_MEMBERS = "group.members";
    static final String ELEM_CHAR_SET = "char.set";
    static final String ATTR_START_RANGE = "start-range";
    static final String ATTR_END_RANGE = "end-range";

    private final List<ConfigEntry> entries = new ArrayList<>();

    private IndexConfiguration() {
    }

    public List<ConfigEntry> getEntries() {
        return entries;
    }

    private void addEntry(final ConfigEntry entry) {
        this.entries.add(entry);
    }

    public static IndexConfiguration parse(final Document document) throws ParseException {
        String message = "Invalid configuration format";

        final IndexConfiguration indexConfiguration = new IndexConfiguration();

        final NodeList indexConfigurationSet = document.getElementsByTagName(ELEM_INDEX_CONFIGURATION_SET);
        if (indexConfigurationSet.getLength() != 1) {
            throw new ParseException(message);
        }
        final Node indexConfigurationSetNode = indexConfigurationSet.item(0);
        if (indexConfigurationSetNode == null) {
            throw new ParseException(message);
        }

        final Node indexConf = getFirstNodeByName(ELEM_INDEX_CONFIGURATION, indexConfigurationSetNode.getChildNodes());
        if (indexConf == null) {
            throw new ParseException(message);
        }

        final Node indexGroups = getFirstNodeByName(INDEX_GROUPS, indexConf.getChildNodes());
        if (indexGroups == null) {
            throw new ParseException(message);
        }

        final List<Node> indexGroupChilds = XMLUtils.toList(indexGroups.getChildNodes());
        for (final Node node : indexGroupChilds) {
            if (node.getNodeName().equals(ELEM_INDEX_GROUP)) {
                final Node key = getFirstNodeByName(ELEM_GROUP_KEY, node.getChildNodes());
                final Node label = getFirstNodeByName(GROUP_LABEL, node.getChildNodes());
                final Node members = getFirstNodeByName(ELEM_GROUP_MEMBERS, node.getChildNodes());

                final String keyValue = getNodeValue(key);
                final String labelValue = getNodeValue(label);
                List<String> groupMembers = Collections.emptyList();
                final List<CharRange> rangeList = new ArrayList<>();

                if (null != members && members.getChildNodes().getLength() > 0) {
                    final List<String> nodeValues = new ArrayList<>();

                    final NodeList membersChilds = members.getChildNodes();
                    for (int j = 0; j < membersChilds.getLength(); j++) {
                        final Node membersChild = membersChilds.item(j);
                        if (membersChild.getNodeName().equals(ELEM_CHAR_SET)) {
                            if (membersChild.hasAttributes() && membersChild.getAttributes() != null) {
                                final Node startRange = membersChild.getAttributes().getNamedItem(ATTR_START_RANGE);
                                final Node endRange = membersChild.getAttributes().getNamedItem(ATTR_END_RANGE);
                                final String startRangeText = getNodeValue(startRange);
                                final String endRangeText = getNodeValue(endRange);
                                if (startRange != null && startRangeText.length() > 0 &&
                                        endRange != null && endRangeText.length() > 0) {
                                    final CharRange range = new CharRange(startRangeText, endRangeText);
                                    rangeList.add(range);
                                    nodeValues.add(startRangeText);
                                }
                            }
                            final String nodeValue = getNodeValue(membersChild);
                            if (!nodeValue.isEmpty()) {
                                nodeValues.add(nodeValue);
                            }
                        }
                    }
                    groupMembers = nodeValues;
                }
                final ConfigEntryImpl configEntry = new ConfigEntryImpl(labelValue, keyValue, groupMembers);
                for (CharRange charRange : rangeList) {
                    configEntry.addRange(charRange);
                }
                indexConfiguration.addEntry(configEntry);
            }
        }

        return indexConfiguration;
    }

    private static String getNodeValue(final Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue().trim();
        } else {
            final StringBuilder res = new StringBuilder();
            final NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final String nodeValue = getNodeValue(childNodes.item(i));
                res.append(nodeValue);
            }
            return res.toString().trim();
        }
    }

    private static Node getFirstNodeByName(final String nodeName, final NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (nodeName.equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }
}
