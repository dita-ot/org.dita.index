package com.idiominc.ws.opentopic.fo.index2.util;

import com.idiominc.ws.opentopic.fo.index2.IndexEntry;
import org.w3c.dom.Node;

import java.util.*;

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
class IndexEntryImpl implements IndexEntry {

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

    private final ArrayList<String> refIDs = new ArrayList<>();

    /**
     * Index entry constructor.
     *
     * @param theValue           string value
     * @param theSortString      sort-as value
     * @param theFormattedString formatter string value
     * @param contents           markup value, may be {@code null}
     */
    public IndexEntryImpl(final String theValue, final String theSortString, final String theFormattedString,
                          final List<Node> contents) {
        this.value = theValue;
        this.sortString = theSortString;
        this.formattedString = theFormattedString;
        this.contents = contents;
    }

    @Override
    public Set<String> getRefIDs() {
        return new HashSet(refIDs);
    }

    public String getValue() {
        return this.value;
    }

    public String getFormattedString() {
        return this.formattedString;
    }

    public List<Node> getContents() {
        return contents;
    }

    public String getSortString() {
        return this.sortString;
    }

    public List<IndexEntry> getChildIndexEntries() {
        return new ArrayList(childs.values());
    }

    public boolean isStartingRange() {
        return this.startRange;
    }

    public boolean isEndingRange() {
        return this.endsRange;
    }

    public boolean isSuppressesThePageNumber() {
        return this.suppressesThePageNumber;
    }

    public boolean isRestoresPageNumber() {
        return this.restoresPageNumber;
    }

    public void addRefID(final String theID) {
        if (!this.refIDs.contains(theID)) {
            this.refIDs.add(theID);
        }
    }

    public void addSeeChild(final IndexEntry entry) {
        final String entryValue = entry.getValue();
        if (!this.seeChilds.containsKey(entryValue)) {
            this.seeChilds.put(entryValue, entry);
            return;
        }
        //The index with same value already exists
        //Add seeChilds of given entry to existing entry
        final IndexEntry existingEntry = this.seeChilds.get(entryValue);

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

    public void addSeeAlsoChild(final IndexEntry entry) {
        final String entryValue = entry.getValue();
        if (!this.seeAlsoChilds.containsKey(entryValue)) {
            this.seeAlsoChilds.put(entryValue, entry);
            return;
        }
        //The index with same value already exists
        //Add seeAlsoChilds of given entry to existing entry
        final IndexEntry existingEntry = this.seeAlsoChilds.get(entryValue);

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

    public void setSortString(final String sortString) {
        this.sortString = sortString;
    }

    public void setStartRange(final boolean theStartRange) {
        if (theStartRange && endsRange) {
            endsRange = false;
        }
        startRange = theStartRange;
    }

    public void setEndsRange(final boolean theEndsRange) {
        if (theEndsRange && startRange) {
            startRange = false;
        }
        endsRange = theEndsRange;
    }

    public void setSuppressesThePageNumber(final boolean theSuppressesThePageNumber) {
        if (theSuppressesThePageNumber && restoresPageNumber) {
            restoresPageNumber = false;
        }

        suppressesThePageNumber = theSuppressesThePageNumber;
    }

    public void setRestoresPageNumber(final boolean theRestoresPageNumber) {
        if (theRestoresPageNumber && suppressesThePageNumber) {
            suppressesThePageNumber = false;
        }
        restoresPageNumber = theRestoresPageNumber;
    }

    public List<IndexEntry> getSeeChildIndexEntries() {
        return new ArrayList(seeChilds.values());
    }

    public List<IndexEntry> getSeeAlsoChildIndexEntries() {
        return new ArrayList(seeAlsoChilds.values());
    }

    @Override
    public String toString() {
        String result = "";
        result += getValue();
        if (this.isSuppressesThePageNumber()) {
            result += "<$nopage>";
        }
        if (this.isRestoresPageNumber()) {
            result += "<$singlepage>";
        }
        if (this.isStartingRange()) {
            result += "<$startrange>";
        }
        if (this.isEndingRange()) {
            result += "<$endrange>";
        }
        if (this.getSortString() != null && this.getSortString().length() > 0) {
            result += "[" + getSortString() + "]";
        }
        return result;
    }

}
