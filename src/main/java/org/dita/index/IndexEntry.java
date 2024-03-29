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

import java.util.List;
import java.util.Set;
import org.w3c.dom.Node;

/**
 * Respresents the Adobe Framemaker's index entry.
 *
 * <pre>
 * See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
 * </pre>
 */
public interface IndexEntry {

  /** @return index reference ids */
  Set<String> getRefIDs();

  /** @return index entry value */
  String getValue();

  /**
   * @return The string with formatting<br>
   *     <code>
   *     See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   *     </code>
   */
  String getFormattedString();

  /**
   * Get index term markup content.
   *
   * @return DITA markup content, {@code null} if not available
   */
  List<Node> getContents();

  /**
   * @return the sort string for the entry<br>
   *     <code>
   *     See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   *     </code>
   */
  String getSortString();

  /** @return child entries of this entry */
  List<IndexEntry> getChildIndexEntries();

  /**
   * @return if this entry starts range<br>
   *     <code>
   *     See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   *     </code>
   */
  boolean isStartingRange();

  /**
   * @return if this entry ends range<br>
   *     <code>
   *     See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   *     </code>
   */
  boolean isEndingRange();

  /**
   * @return if this entry suppresses page number<br>
   *     <code>
   *     See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   *     </code>
   */
  boolean isSuppressesThePageNumber();

  /**
   * @return if this entry restores page number<br>
   *     <code>
   *     See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   *     </code>
   */
  boolean isRestoresPageNumber();

  /**
   * Adds reference id to the index entry
   *
   * @param id reference id
   */
  void addRefID(String id);

  /**
   * Adds child to the entry
   *
   * @param entry index entry
   */
  void addChild(IndexEntry entry);

  /**
   * Sets if the index entry restores page number <code>
   * See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   * </code>
   *
   * @param restoresPageNumber
   */
  void setRestoresPageNumber(boolean restoresPageNumber);

  /**
   * Sets if the index entry suppresses page number <code>
   * See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   * </code>
   *
   * @param suppressesThePageNumber
   */
  void setSuppressesThePageNumber(boolean suppressesThePageNumber);

  /**
   * Sets if the index entry starts range <code>
   * See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   * </code>
   *
   * @param startRange
   */
  void setStartRange(boolean startRange);

  /**
   * Sets if the index entry ends range <code>
   * See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details
   * </code>
   *
   * @param endsRange
   */
  void setEndsRange(boolean endsRange);

  /**
   * Sets sort string for the value
   *
   * @param sortString
   */
  void setSortString(String sortString);

  void addSeeChild(IndexEntry entry);

  void addSeeAlsoChild(IndexEntry entry);

  List<IndexEntry> getSeeChildIndexEntries();

  List<IndexEntry> getSeeAlsoChildIndexEntries();
}
