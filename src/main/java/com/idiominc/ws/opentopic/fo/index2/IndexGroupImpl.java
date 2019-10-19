/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package com.idiominc.ws.opentopic.fo.index2;

import com.idiominc.ws.opentopic.fo.index2.configuration.ConfigEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class IndexGroupImpl implements IndexGroup {
    private final String label;
    private final ConfigEntry configEntry;
    private final List<IndexEntry> entries = new ArrayList<>();
    private final List<IndexGroupImpl> childList = new ArrayList<>();

    IndexGroupImpl(final String label, final ConfigEntry configEntry) {
        this.label = label;
        this.configEntry = configEntry;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public Collection<IndexEntry> getEntries() {
        return entries;
    }

    ConfigEntry getConfigEntry() {
        return this.configEntry;
    }

    @Override
    public void addEntry(final IndexEntry entry) {
        boolean isInserted = false;
        if (!childList.isEmpty()) {
            //                MyIndexGroup[] childGroupList = (MyIndexGroup[]) childList.toArray(new MyIndexGroup[childList.size()]);
            for (int i = 0; i < childList.size() && !isInserted; i++) {
                final IndexGroupImpl thisChild = childList.get(i);
                final List<String> thisGroupMembers = thisChild.getConfigEntry().getGroupMembers();
                if (doesStart(entry.getValue(), thisGroupMembers)) {
                    thisChild.addEntry(entry);
                    isInserted = true;
                }
            }
        }
        if (!isInserted) {
            entries.add(entry);
        }
    }
    
    public boolean doesStart(final String sourceString, final List<String> compStrings) {
        for (final String compString : compStrings) {
            if (sourceString.startsWith(compString)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesStart(final List<String> sourceStrings, final List<String> compStrings) {
        for (final String sourceString2 : sourceStrings) {
            for (String compString : compStrings) {
                if (sourceString2.startsWith(compString) && !sourceString2.equals(compString)) {
                    return true;
                }
            }
        }
        return false;
    }

    void addChild(final IndexGroupImpl group) {
        if (!childList.contains(group)) {
            childList.add(group);
        }
        //            MyIndexGroup[] childGroupList = (MyIndexGroup[]) childList.toArray(new MyIndexGroup[childList.size()]);
        for (int i = 0; i < childList.size(); i++) {
            final IndexGroupImpl thisChild = childList.get(i);
            for (int j = 0; j < childList.size(); j++) {
                if (i != j) {
                    final IndexGroupImpl compChild = childList.get(j);
                    final List<String> thisGroupMembers = thisChild.getConfigEntry().getGroupMembers();
                    final List<String> compGroupMembers = compChild.getConfigEntry().getGroupMembers();
                    if (doesStart(thisGroupMembers, compGroupMembers)) {
                        this.childList.remove(thisChild);
                        compChild.addChild(thisChild);
                    }
                }
            }
        }
    }

    @Deprecated
    public void removeChild(final IndexGroupImpl indexGroup) {
        childList.remove(indexGroup);
    }

}
