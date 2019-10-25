/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 Idiom Technologies, Inc
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.index.configuration;

import org.dita.index.IndexCollator;

class CharRange {

    private final String start;
    private final String end;

    public CharRange(final String start, final String end) {
        this.start = start;
        this.end = end;
    }

    public boolean isInRange(final String value, final IndexCollator collator) {
        return (collator.compare(value, start) > 0) && (collator.compare(value, end) < 0);
    }
}
