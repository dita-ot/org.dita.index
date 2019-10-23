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

package com.idiominc.ws.opentopic.fo.index2;

import com.idiominc.ws.opentopic.fo.index2.configuration.ConfigEntry;
import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;

import java.util.*;

public final class IndexGroupProcessor {

    private DITAOTLogger logger;

    private static final String SPECIAL_CHARACTER_GROUP_KEY = "Specials";

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Puts index entries to the group they are belongs
     *
     * @param indexEntries       index entries
     * @param indexConfiguration index configuration
     * @param locale             locale used to sort and compare index entries
     * @return groups with sorted index entries inside
     */
    public List<IndexGroup> process(final Collection<IndexEntry> indexEntries, final IndexConfiguration indexConfiguration,
                                    final Locale locale) {
        final IndexCollator collator = new IndexCollator(locale);

        final List<IndexGroupImpl> result = new ArrayList<>();

        final List<ConfigEntry> entries = indexConfiguration.getEntries();

        final Map<String, IndexEntry> indexMap = createMap(indexEntries);

        //Creating array of index groups
        for (final ConfigEntry configEntry : entries) {
            final String label = configEntry.getLabel();
            final IndexGroupImpl group = new IndexGroupImpl(label, configEntry);
            result.add(group);
        }
        final List<IndexGroupImpl> indexGroups = result;

        //Adding dependecies to group array
        for (int i = 0; i < indexGroups.size(); i++) {
            final IndexGroupImpl thisGroup = indexGroups.get(i);
            final List<String> thisGroupMembers = thisGroup.getConfigEntry().getGroupMembers();
            for (int j = 0; j < indexGroups.size(); j++) {
                if (j != i) {
                    final IndexGroupImpl compGroup = indexGroups.get(j);
                    final List<String> compGroupMembers = compGroup.getConfigEntry().getGroupMembers();
                    if (doesStart(compGroupMembers, thisGroupMembers)) {
                        thisGroup.addChild(compGroup);
                    }
                }
            }
        }

        /*
        for (int i = 0; i < IndexGroups.length; i++) {
            IndexGroups[i].printDebug();
        }
         */

        for (int i = 0; i < indexGroups.size(); i++) {
            final IndexGroupImpl group = indexGroups.get(i);
            final ConfigEntry configEntry = group.getConfigEntry();

            final List<String> groupMembers = configEntry.getGroupMembers();

            if (!groupMembers.isEmpty()) {
                final ArrayList<String> keys = new ArrayList<>(indexMap.keySet());
                //Find entries by comaping first letter with a chars in current config entry
                for (final String key : keys) {
                    if (!key.isEmpty()) {
                        final String value = getValue(indexMap.get(key));
                        //                        final char c = value.charAt(0);
                        if (configEntry.isInRange(value, collator)) {
                            final IndexEntry entry = indexMap.remove(key);
                            group.addEntry(entry);
                        }
                    }
                }
            } else {
                //Get index entries by range specified by two keys
                final String key1 = configEntry.getKey();
                String key2 = null;
                if (entries.size() > (i + 1)) {
                    final ConfigEntry nextEntry = entries.get(i + 1);
                    key2 = nextEntry.getKey();
                }
                final List<String> indexMapKeys = getIndexKeysOfIndexesInRange(key1, key2, collator, indexMap);

                for (final String mapKey : indexMapKeys) {
                    final IndexEntry entry = indexMap.remove(mapKey);
                    group.addEntry(entry);
                }
            }
            /*
            if (group.getEntries().length > 0) {
                result.add(group);
            }
             */
        }

        //If some terms remain uncategorized, and a recognized special character
        //group is available, place remaining terms in that group
        for (final IndexGroupImpl group : indexGroups) {
            final ConfigEntry configEntry = group.getConfigEntry();
            final String configKey = configEntry.getKey();
            if (configKey.equals(SPECIAL_CHARACTER_GROUP_KEY)) {
                for (final String key : new ArrayList<>(indexMap.keySet())) {
                    if (!key.isEmpty()) {
                        final String value = getValue(indexMap.get(key));
                        //                        final char c = value.charAt(0);
                        logger.info(MessageUtils.getMessage("PDFJ003I", value).toString());
                        final IndexEntry entry = indexMap.remove(key);
                        group.addEntry(entry);
                    }
                }
            }
        }

        //No recognized "Special characters" group; uncategorized terms have no place to go, must be dropped
        if (!indexMap.isEmpty()) {
            for (final String key : new ArrayList<>(indexMap.keySet())) {
                if (!key.isEmpty()) {
                    final IndexEntry entry = indexMap.get(key);
                    logger.error(MessageUtils.getMessage("PDFJ001E", entry.toString()).toString());
                }
            }
            if (IndexPreprocessorTask.failOnError) {
                logger.error(MessageUtils.getMessage("PDFJ002E").toString());
                IndexPreprocessorTask.processingFaild = true;
            }
        }

        final List<IndexGroup> cleanResult = new ArrayList<>();
        for (final IndexGroupImpl indexGroup : indexGroups) {
            if (!indexGroup.getEntries().isEmpty()) {
                cleanResult.add(indexGroup);
            }
        }
        return cleanResult;
    }

    private static String getValue(final IndexEntry entry) {
        final String sortValue = entry.getSortString();
        if (sortValue != null && sortValue.length() > 0) {
            return sortValue;
        } else {
            return entry.getValue();
        }
    }

    private static List<String> getIndexKeysOfIndexesInRange(final String key1, final String key2,
                                                             final IndexCollator collator,
                                                             final Map<String, IndexEntry> indexEntryMap) {
        final List<String> res = new ArrayList<>();
        for (final Map.Entry<String, IndexEntry> e : indexEntryMap.entrySet()) {
            final int res1 = collator.compare(key1, getValue(e.getValue()));
            if (res1 <= 0) {
                if (key2 == null) {
                    //the right range is not specified
                    res.add(e.getKey());
                    continue;
                }
                final int res2 = collator.compare(key2, e.getKey());
                if (res2 > 0) {
                    res.add(e.getKey());
                }
            }
        }
        return res;
    }

    @Deprecated
    private static boolean doesStart(final String sourceString, final List<String> compStrings) {
        for (final String compString : compStrings) {
            if (sourceString.startsWith(compString)) {
                return true;
            }
        }
        return false;
    }

    private static boolean doesStart(final List<String> sourceStrings, final List<String> compStrings) {
        for (final String sourceString2 : sourceStrings) {
            for (String compString : compStrings) {
                if (sourceString2.startsWith(compString) && !sourceString2.equals(compString)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Map<String, IndexEntry> createMap(final Collection<IndexEntry> indexEntries) {
        final Map<String, IndexEntry> map = new HashMap<>();
        for (final IndexEntry indexEntrie : indexEntries) {
            final String value = indexEntrie.getValue();

            if (!map.containsKey(value)) {
                map.put(value, indexEntrie);
            } else {
                final IndexEntry existingEntry = map.get(value);
                final Collection<IndexEntry> childIndexEntries = indexEntrie.getChildIndexEntries();
                for (final IndexEntry childIndexEntry : childIndexEntries) {
                    existingEntry.addChild(childIndexEntry);
                }
                final Collection<IndexEntry> seeChildIndexEntries = indexEntrie.getSeeChildIndexEntries();
                for (final IndexEntry seeChildIndexEntry : seeChildIndexEntries) {
                    existingEntry.addSeeChild(seeChildIndexEntry);
                }
                final Collection<IndexEntry> seeAlsoChildIndexEntries = indexEntrie.getSeeAlsoChildIndexEntries();
                for (final IndexEntry seeAlsoChildIndexEntry : seeAlsoChildIndexEntries) {
                    existingEntry.addSeeAlsoChild(seeAlsoChildIndexEntry);
                }
                //supress some attributes of given entry to the existing one
                if (indexEntrie.isRestoresPageNumber()) {
                    existingEntry.setRestoresPageNumber(true);
                }
                final Set<String> refIDs = indexEntrie.getRefIDs();
                for (final String refID : refIDs) {
                    existingEntry.addRefID(refID);
                }
                if (!indexEntrie.isSuppressesThePageNumber()) {
                    existingEntry.setSuppressesThePageNumber(false);
                }
                if (indexEntrie.isStartingRange()) {
                    existingEntry.setStartRange(true);
                }
            }
        }
        return map;
    }

}
