/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.index;

import org.dita.index.configuration.ConfigEntry;

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
        return label;
    }

    @Override
    public Collection<IndexEntry> getEntries() {
        return entries;
    }

    ConfigEntry getConfigEntry() {
        return configEntry;
    }

    @Override
    public void addEntry(final IndexEntry entry) {
        boolean isInserted = false;
        if (!childList.isEmpty()) {
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

    private boolean doesStart(final String sourceString, final List<String> compStrings) {
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

    // FIXME: What is this used for, because this class doesn't expose childList in any way
    void addChild(final IndexGroupImpl group) {
        // FIXME: this only guards against adding the same instance
        if (!childList.contains(group)) {
            childList.add(group);
        }
        for (int i = 0; i < childList.size(); i++) {
            final IndexGroupImpl thisChild = childList.get(i);
            for (int j = 0; j < childList.size(); j++) {
                if (i != j) {
                    final IndexGroupImpl compChild = childList.get(j);
                    final List<String> thisGroupMembers = thisChild.getConfigEntry().getGroupMembers();
                    final List<String> compGroupMembers = compChild.getConfigEntry().getGroupMembers();
                    if (doesStart(thisGroupMembers, compGroupMembers)) {
                        childList.remove(thisChild);
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
