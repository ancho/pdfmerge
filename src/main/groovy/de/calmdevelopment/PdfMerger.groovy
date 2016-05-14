/*
 * Copyright 2016 Frank Becker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.calmdevelopment

import de.calmdevelopment.bookmark.Bookmarker
import de.calmdevelopment.bookmark.NoneBookmarker
import de.calmdevelopment.category.PDDocumentCategory
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument

/**
 * Merge a given set of pdf documents to a new pdf destination
 */
class PdfMerger {
    /**
     * The configuration
     */
    Config config

    /**
     * A map of source files with the Bookmarker to apply to
     * <pre>
     *     [[document: doc, bookmarker: bookmarker], ...]
     * </pre>
     */
    def sources = []

    /**
     * The destination OutputStream
     */
    OutputStream destination

    /**
     * Construct a PdfMerger instance with a {@link MergeConfig} as configuration
     */
    PdfMerger() {
        this.config = new MergeConfig()
    }

    /**
     * Construct a PdfMerger instance
     *
     * Adds the {@link MemoryUsageSetting} to the configuration
     * @param setting
     */
    PdfMerger(MemoryUsageSetting setting) {
        this()
        this.config.memoryUsageSetting = setting
    }

    /**
     * Add a source pdf document with a {@link Bookmarker} to apply to the list of source files
     *
     * @param sourceDocument An inputstream to the source pdf document
     * @param bookmarker A {@link Bookmarker} to apply to the sourceDocument
     */
    def addSource(InputStream sourceDocument, Bookmarker bookmarker) {
        PDDocument document = PDDocument.load(sourceDocument, config.memoryUsageSetting)
        this.sources << [document: document, bookmarker: bookmarker]
    }

    /**
     * Add a source pdf document to the list of source files.
     * <p>
     *     Applies a {@link NoneBookmarker} to the given sourceDocument.
     * </p>
     * @param sourceDocument An inputstream to the source pdf document
     */
    def addSource(InputStream sourceDocument) {
        addSource(sourceDocument, new NoneBookmarker())
    }

    /**
     * merge a set of given source files to the destination file.
     * <p>
     *     <ul>
     *          <li>Applies given {@link Bookmarker} to the source files.</li>
     *          <li>Applies configured {@link MemoryUsageSetting} </li>
     *     </ul>
     * </p>
     *
     * <p>
     *     Inserts a blank Page to documents with an odd number of pages if {@code insertBlankPages}
     *     is configured.
     *
     *     <pre>
     *         PdfMerger merger = new PdfMerger()
     *         merger.config.insertBlankPages = true
     *     </pre>
     * </p>
     *
     * @throws IllegalStateException If no destination file is configured or source files are less then two
     */
    def merge() throws IllegalStateException {

        if( destination ) {
            PDFMergerUtility merger = new PDFMergerUtility()

            if ( sources.size() > 1 ) {
                sources.each { source ->
                    ByteArrayOutputStream content = new ByteArrayOutputStream()

                    Bookmarker bookmarker = source.bookmarker
                    PDDocument document = source.document

                    insertBlankPagesIfPagesCountIsOdd(document)

                    bookmarker.addDocument(document)
                    bookmarker.bookmark()

                    document.save(content)
                    merger.addSource(new ByteArrayInputStream(content.toByteArray()))
                    document.close()
                }
            }
            else {
                throw new IllegalStateException("A minimum of two sources files are required to merge.")
            }

            merger.setDestinationStream(destination)
            merger.mergeDocuments(config.memoryUsageSetting)
        }
        else {
            throw new IllegalStateException("The destination for the merged documents is undefined.")
        }
    }

    private void insertBlankPagesIfPagesCountIsOdd(PDDocument document) {

        use(PDDocumentCategory) {
            document.appendBlankPage() {
                config.insertBlankPages && document.hasOddPageCount()
            }
        }

    }

}
