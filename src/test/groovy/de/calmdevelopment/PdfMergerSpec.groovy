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
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import spock.lang.Specification

class PdfMergerSpec extends Specification {
    public static final String FIRST_DOCUMENT_TITLE = "First Document"
    public static final String SECOND_DOCUMENT_TITLE = "Second Document"
    public static final String BOOKMARKED_PAGE_TITLE = "Bookmarked Page"
    private ByteArrayOutputStream fistDocumentOutputStream
    private ByteArrayOutputStream secondDocumentOutputStream
    private ByteArrayOutputStream allPagesOutputStream
    private ByteArrayOutputStream destinationStream
    private PDDocument firstDocument
    private PDDocument secondDocument
    private PDDocument allpagesBookmarkedDocument

    def setup() {
        fistDocumentOutputStream = new ByteArrayOutputStream()
        secondDocumentOutputStream = new ByteArrayOutputStream()
        allPagesOutputStream = new ByteArrayOutputStream()
        destinationStream = new ByteArrayOutputStream()

        firstDocument = SampleDocumentBuilder.createDocument(FIRST_DOCUMENT_TITLE, 2)
        secondDocument = SampleDocumentBuilder.createDocument(SECOND_DOCUMENT_TITLE, 3)

        allpagesBookmarkedDocument = SampleDocumentBuilder.createDocumentWithBookmarks(BOOKMARKED_PAGE_TITLE, 4)
        firstDocument.save(fistDocumentOutputStream)
        secondDocument.save(secondDocumentOutputStream)
        allpagesBookmarkedDocument.save(allPagesOutputStream)

    }

    void cleanup() {
        fistDocumentOutputStream.close()
        secondDocumentOutputStream.close()
        allPagesOutputStream.close()
        destinationStream.close()

        firstDocument.close()
        secondDocument.close()
    }

    def "should merge documents and apply bookmarker"() {
        given:
        PdfMerger merger = new PdfMerger()
        merger.addSource(asInputStream(fistDocumentOutputStream), new FirstPageBookmarker(FIRST_DOCUMENT_TITLE))
        merger.addSource(asInputStream(secondDocumentOutputStream), new FirstPageBookmarker(SECOND_DOCUMENT_TITLE))
        merger.destination = destinationStream

        when:
        merger.merge()

        then:
        def expectedDocument = PDDocument.load(new ByteArrayInputStream(destinationStream.toByteArray()))
        expectedDocument.getPages().size() == 5

        PDOutlineItem firstBookmark = expectedDocument.getDocumentCatalog().getDocumentOutline().getFirstChild()
        firstBookmark.getTitle() == FIRST_DOCUMENT_TITLE
        firstBookmark.getNextSibling().getTitle() == SECOND_DOCUMENT_TITLE
    }

    def "should preserve bookmarks from sources"() {
        given:
        PdfMerger merger = new PdfMerger()
        merger.addSource(asInputStream(fistDocumentOutputStream), new FirstPageBookmarker(FIRST_DOCUMENT_TITLE))
        merger.addSource(asInputStream(allPagesOutputStream), new FirstPageBookmarker(SECOND_DOCUMENT_TITLE))
        merger.addSource(asInputStream(allPagesOutputStream))
        merger.destination = destinationStream

        when:
        merger.merge()

        then:
        def expectedDocument = PDDocument.load(new ByteArrayInputStream(destinationStream.toByteArray()))
        expectedDocument.getPages().size() == 10
        expectedDocument.getDocumentCatalog().documentOutline.children().size() == 6

        def firstChild = expectedDocument.getDocumentCatalog().documentOutline.firstChild
        firstChild.getTitle() == FIRST_DOCUMENT_TITLE
        firstChild.nextSibling.getTitle() == SECOND_DOCUMENT_TITLE
        firstChild.nextSibling.children().size() == 4
        firstChild.nextSibling.nextSibling.title == "$BOOKMARKED_PAGE_TITLE 0"
    }

    private ByteArrayInputStream asInputStream(ByteArrayOutputStream outputStream) {
        new ByteArrayInputStream(outputStream.toByteArray())
    }
}
