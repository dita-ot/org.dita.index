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

import static org.dita.index.IndexPreprocessor.VALUE_SEPARATOR;

import java.util.Collections;
import java.util.List;
import org.w3c.dom.Node;

public class IndexStringProcessor {

  private IndexStringProcessor() {}

  /**
   * Parse the index marker string and create IndexEntry object from one.
   *
   * @param indexMarkerString index marker string
   * @param contents IndexPreprocessorTask instance
   * @return IndexEntry objects created from the index string
   */
  public static List<IndexEntry> processIndexString(
      final String indexMarkerString, final List<Node> contents) {
    final IndexEntry indexEntry = createIndexEntry(indexMarkerString, contents, null, false);
    final String referenceIDBuf = indexEntry.getValue() + VALUE_SEPARATOR;
    indexEntry.addRefID(referenceIDBuf);
    return Collections.singletonList(indexEntry);
  }

  /**
   * Method equals to the normalize-space xslt function
   *
   * @param string string to normalize
   * @return normalized string
   */
  public static String normalizeTextValue(final String string) {
    if (null != string && string.length() > 0) {
      return string.replaceAll("[\\s\\n]+", " ").trim();
    }
    return string;
  }

  private static IndexEntry createIndexEntry(
      final String value,
      final List<Node> contents,
      final String sortString,
      final boolean isParentNoPage) {
    final IndexEntry indexEntry = new IndexEntryImpl(value, sortString, value, contents);
    indexEntry.setSuppressesThePageNumber(isParentNoPage);
    indexEntry.setRestoresPageNumber(false);
    indexEntry.setStartRange(false);
    indexEntry.setEndsRange(false);
    indexEntry.setSortString(sortString);
    return indexEntry;
  }
}
