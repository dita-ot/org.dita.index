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

import org.w3c.dom.Node;

import java.util.*;

import static java.util.Collections.emptyList;

/**
 * Mutable index entry.
 */
public class IndexEntryImpl implements IndexEntry {

    private final String value;
    private final String formattedString;
    private final List<Node> contents;
    private String sortString;

    private final Map<String, IndexEntry> childs = new HashMap<>();
    private final Map<String, IndexEntry> seeChilds = new HashMap<>();
    private final Map<String, IndexEntry> seeAlsoChilds = new HashMap<>();

    private boolean startRange = false;
    private boolean endsRange = false;
    private boolean suppressesThePageNumber = false;
    private boolean restoresPageNumber = false;

    private final Set<String> refIDs = new HashSet<>();

    /**
     * Index entry constructor.
     *
     * @param value           string value
     * @param sortString      sort-as value
     * @param formattedString formatter string value
     * @param contents        markup value, may be {@code null}
     */
    public IndexEntryImpl(final String value, final String sortString,
                          final String formattedString, final List<Node> contents) {
        this.value = value;
        this.sortString = sortString;
        this.formattedString = formattedString;
        this.contents = contents;
    }

    @Override
    public Set<String> getRefIDs() {
        return refIDs;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getFormattedString() {
        return formattedString;
    }

    @Override
    public List<Node> getContents() {
        return contents;
    }

    @Override
    public String getSortString() {
        return sortString;
    }

    @Override
    public List<IndexEntry> getChildIndexEntries() {
        return new ArrayList(childs.values());
    }

    @Override
    public boolean isStartingRange() {
        return startRange;
    }

    @Override
    public boolean isEndingRange() {
        return endsRange;
    }

    @Override
    public boolean isSuppressesThePageNumber() {
        return suppressesThePageNumber;
    }

    @Override
    public boolean isRestoresPageNumber() {
        return restoresPageNumber;
    }

    @Override
    public void addRefID(final String id) {
        refIDs.add(id);
    }

    @Override
    public void addSeeChild(final IndexEntry entry) {
        final String entryValue = entry.getValue();
        if (!seeChilds.containsKey(entryValue)) {
            seeChilds.put(entryValue, entry);
            return;
        }
        //The index with same value already exists
        //Add seeChilds of given entry to existing entry
        final IndexEntry existingEntry = seeChilds.get(entryValue);

        final List<IndexEntry> childIndexEntries = entry.getChildIndexEntries();
        for (final IndexEntry childIndexEntry : childIndexEntries) {
            existingEntry.addChild(childIndexEntry);
        }
        //supress some attributes of given entry to the existing one
        if (entry.isRestoresPageNumber()) {
            existingEntry.setRestoresPageNumber(true);
        }
        if (!entry.isSuppressesThePageNumber()) {
            existingEntry.setSuppressesThePageNumber(false);
        }
        if (entry.isStartingRange()) {
            existingEntry.setStartRange(true);
        }
        if (entry.getSortString() != null) {
            existingEntry.setSortString(entry.getSortString());
        }
    }

    @Override
    public void addSeeAlsoChild(final IndexEntry entry) {
        final String entryValue = entry.getValue();
        if (!seeAlsoChilds.containsKey(entryValue)) {
            seeAlsoChilds.put(entryValue, entry);
            return;
        }
        //The index with same value already exists
        //Add seeAlsoChilds of given entry to existing entry
        final IndexEntry existingEntry = seeAlsoChilds.get(entryValue);

        final Collection<IndexEntry> childIndexEntries = entry.getChildIndexEntries();
        for (final IndexEntry childIndexEntry : childIndexEntries) {
            existingEntry.addChild(childIndexEntry);
        }
        //supress some attributes of given entry to the existing one
        if (entry.isRestoresPageNumber()) {
            existingEntry.setRestoresPageNumber(true);
        }
        if (!entry.isSuppressesThePageNumber()) {
            existingEntry.setSuppressesThePageNumber(false);
        }
        if (entry.isStartingRange()) {
            existingEntry.setStartRange(true);
        }
        if (entry.getSortString() != null) {
            existingEntry.setSortString(entry.getSortString());
        }
    }

    @Override
    public void addChild(final IndexEntry entry) {
        final String entryValue = entry.getValue();
        if (!childs.containsKey(entryValue)) {
            childs.put(entryValue, entry);
            return;
        }
        //The index with same value already exists
        //Add childs of given entry to existing entry
        final IndexEntry existingEntry = childs.get(entryValue);

        final Collection<IndexEntry> childIndexEntries = entry.getChildIndexEntries();
        for (final IndexEntry childIndexEntry : childIndexEntries) {
            existingEntry.addChild(childIndexEntry);
        }
        //supress some attributes of given entry to the existing one
        if (entry.isRestoresPageNumber()) {
            existingEntry.setRestoresPageNumber(true);
        }
        if (!entry.isSuppressesThePageNumber()) {
            existingEntry.setSuppressesThePageNumber(false);
        }
        if (entry.isStartingRange()) {
            existingEntry.setStartRange(true);
        }
        if (entry.getSortString() != null) {
            existingEntry.setSortString(entry.getSortString());
        }
    }

    @Override
    public void setSortString(final String sortString) {
        this.sortString = sortString;
    }

    @Override
    public void setStartRange(final boolean startRange) {
        if (startRange && endsRange) {
            endsRange = false;
        }
        this.startRange = startRange;
    }

    @Override
    public void setEndsRange(final boolean endsRange) {
        if (endsRange && startRange) {
            startRange = false;
        }
        this.endsRange = endsRange;
    }

    @Override
    public void setSuppressesThePageNumber(final boolean suppressesThePageNumber) {
        if (suppressesThePageNumber && restoresPageNumber) {
            restoresPageNumber = false;
        }

        this.suppressesThePageNumber = suppressesThePageNumber;
    }

    @Override
    public void setRestoresPageNumber(final boolean restoresPageNumber) {
        if (restoresPageNumber && suppressesThePageNumber) {
            suppressesThePageNumber = false;
        }
        this.restoresPageNumber = restoresPageNumber;
    }

    @Override
    public List<IndexEntry> getSeeChildIndexEntries() {
        return seeChilds.isEmpty() ? emptyList() : new ArrayList(seeChilds.values());
    }

    @Override
    public List<IndexEntry> getSeeAlsoChildIndexEntries() {
        return seeAlsoChilds.isEmpty() ? emptyList() : new ArrayList(seeAlsoChilds.values());
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(getValue());
        if (isSuppressesThePageNumber()) {
            result.append("<$nopage>");
        }
        if (isRestoresPageNumber()) {
            result.append("<$singlepage>");
        }
        if (isStartingRange()) {
            result.append("<$startrange>");
        }
        if (isEndingRange()) {
            result.append("<$endrange>");
        }
        if (getSortString() != null && getSortString().length() > 0) {
            result.append("[").append(getSortString()).append("]");
        }
        return result.toString();
    }

}
