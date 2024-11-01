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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.index.configuration.IndexConfiguration;
import org.dita.index.configuration.ParseException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class IndexPreprocessorTask extends Task {

  private static final String PREFIX = "opentopic-index";
  private static final String NAMESPACE_URL = "http://www.idiominc.com/opentopic/index";

  public static boolean failOnError = false;
  public static boolean processingFaild = false;

  private File input;
  private File output;
  private Locale locale;
  private File indexConfig;
  private boolean draft;

  @Override
  public void execute() throws BuildException {
    checkParameters();

    final DocumentBuilder documentBuilder;
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      documentBuilder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new BuildException(e);
    }

    final Document doc;
    try {
      doc = documentBuilder.parse(input);
    } catch (SAXException | IOException e) {
      throw new BuildException(e);
    }

    final IndexConfiguration configuration;
    try {
      final Document configDocument = documentBuilder.parse(indexConfig);
      configuration = IndexConfiguration.parse(configDocument);
    } catch (ParseException | SAXException | IOException e) {
      throw new BuildException(e);
    }

    final IndexPreprocessor preprocessor = new IndexPreprocessor(PREFIX, NAMESPACE_URL, draft);
    preprocessor.setLogger(new DITAOTAntLogger(getProject()));

    final IndexPreprocessResult result = preprocessor.process(doc);
    final Document resultDoc = result.document;

    final Collection<IndexEntry> indexEntries = result.indexEntries;
    preprocessor.createAndAddIndexGroups(indexEntries, configuration, resultDoc, locale);
    if (processingFaild) {
      setActiveProjectProperty("ws.runtime.index.preprocess.fail", "true");
    }

    // Serialize processed document
    try (OutputStream out = new FileOutputStream(output)) {
      final TransformerFactory transformerFactory = TransformerFactory.newInstance();
      final Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
      if (doc.getDoctype() != null) {
        if (null != doc.getDoctype().getPublicId()) {
          transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doc.getDoctype().getPublicId());
        }
        if (null != doc.getDoctype().getSystemId()) {
          transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
        }
      }

      final DOMSource source = new DOMSource(resultDoc);
      final StreamResult streamResult = new StreamResult(out);
      transformer.transform(source, streamResult);
    } catch (final Exception e) {
      throw new BuildException(e);
    }
  }

  private void checkParameters() throws BuildException {
    if (null == locale || null == input || null == output || null == indexConfig) {
      throw new BuildException("locale, indexConfig, input, output attributes are required");
    }
  }

  public void setInput(final File input) {
    this.input = input;
  }

  public void setOutput(final File output) {
    this.output = output;
  }

  public void setLocale(final String locale) {
    if (locale.indexOf("-") == 2 || locale.indexOf("_") == 2) {
      this.locale = new Locale(locale.substring(0, 2), locale.substring(3));
    } else {
      this.locale = new Locale(locale);
    }
  }

  public void setIndexConfig(final File indexConfig) {
    this.indexConfig = indexConfig;
  }

  public void setFailOnError(final boolean failOnError) {
    IndexPreprocessorTask.failOnError = failOnError;
  }

  public void setDraft(final boolean draftValue) {
    this.draft = draftValue;
  }

  private void setActiveProjectProperty(final String propertyName, final String propertyValue) {
    final Project activeProject = getProject();
    if (activeProject != null) {
      activeProject.setProperty(propertyName, propertyValue);
    }
  }
}
