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

import de.calmdevelopment.helper.SampleDocumentBuilder
import org.apache.pdfbox.pdmodel.PDDocument
import spock.lang.Specification

class FirstPageBookmarkerSpec extends Specification {
    private ByteArrayOutputStream outputStream
    private PDDocument sampleDocument

    void setup() {
        this.outputStream = new ByteArrayOutputStream()
        this.sampleDocument = SampleDocumentBuilder.createDocument("Test Message", 2)
    }

    void cleanup() {
        this.outputStream.close()
        this.sampleDocument.close()
    }

    def "shouldAddBookmarkToFirstPage"() {
        given:
        def bookmarkTitle = "Sample Document"
        Bookmarker bookmarker = new FirstPageBookmarker(bookmarkTitle);
        bookmarker.addDocument(sampleDocument)

        when:
        bookmarker.bookmark()
        sampleDocument.save(outputStream)

        then:
        def firstBookmark = sampleDocument.getDocumentCatalog().documentOutline.getFirstChild()
        firstBookmark.findDestinationPage(sampleDocument) == sampleDocument.getPage(0)

        PDDocument loaded = PDDocument.load(new ByteArrayInputStream(outputStream.toByteArray()))
        loaded.getDocumentCatalog().getDocumentOutline() != null
    }

    def "should preserve original outline"() {
        given:
        sampleDocument = SampleDocumentBuilder.createDocumentWithBookmarks("Original", 4)
        Bookmarker bookmarker = new FirstPageBookmarker("Bookmarked Page")
        bookmarker.addDocument(sampleDocument)

        when:
        bookmarker.bookmark()

        then:
        def firstBookmer = sampleDocument.getDocumentCatalog().getDocumentOutline().getFirstChild()
        def firstOriginalBookmark = firstBookmer.getFirstChild()

        firstBookmer.title == "Bookmarked Page"
        firstOriginalBookmark.title == "Original 0"
        firstOriginalBookmark.getFirstChild().title == "subOriginal 0"
    }
}
