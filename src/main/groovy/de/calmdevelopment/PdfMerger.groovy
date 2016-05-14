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

class PdfMerger {
    Config config
    def sources = []
    OutputStream destination

    PdfMerger() {
        this.config = new MergeConfig()
    }

    PdfMerger(MemoryUsageSetting setting) {
        this()
        this.config.memoryUsageSetting = setting
    }

    def addSource(InputStream sourceDocument, Bookmarker bookmarker) {
        PDDocument document = PDDocument.load(sourceDocument, config.memoryUsageSetting)
        this.sources << [document: document, bookmarker: bookmarker]
    }

    def addSource(InputStream sourceDocument) {
        addSource(sourceDocument, new NoneBookmarker())
    }

    def merge() {

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
