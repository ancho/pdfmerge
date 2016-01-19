package de.calmdevelopment

import de.calmdevelopment.helper.SampleDocumentBuilder
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import spock.lang.Specification

/**
 * Created by frank on 18.01.16.
 */
class PdfMergerSpec extends Specification {
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

        firstDocument = SampleDocumentBuilder.createDocument("First Documemt", 2)
        secondDocument = SampleDocumentBuilder.createDocument("Second Document", 3)

        allpagesBookmarkedDocument = SampleDocumentBuilder.createDocumentWithBookmarks("Bookmarked Document", 4)
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
        merger.addSource(new ByteArrayInputStream(fistDocumentOutputStream.toByteArray()), new FirstPageBookmarker("First Document"))
        merger.addSource(new ByteArrayInputStream(secondDocumentOutputStream.toByteArray()), new FirstPageBookmarker("Second Document"))
        merger.destination = destinationStream

        when:
        merger.merge()

        then:
        def expectedDocument = PDDocument.load(new ByteArrayInputStream(destinationStream.toByteArray()))
        expectedDocument.getPages().size() == 5

        PDOutlineItem firstBookmark = expectedDocument.getDocumentCatalog().getDocumentOutline().getFirstChild()
        firstBookmark.getTitle() == "First Document"

        firstBookmark.getNextSibling().getTitle() == "Second Document"
    }

    def "should preserve bookmarks from sources"() {
        given:
        PdfMerger merger = new PdfMerger()
        merger.addSource(new ByteArrayInputStream(fistDocumentOutputStream.toByteArray()), new FirstPageBookmarker("First Document"))
        merger.addSource(new ByteArrayInputStream(allPagesOutputStream.toByteArray()))
        merger.destination = destinationStream

        when:
        merger.merge()

        then:
        def expectedDocument = PDDocument.load(new ByteArrayInputStream(destinationStream.toByteArray()))
        expectedDocument.getPages().size() == 6
        expectedDocument.getDocumentCatalog().documentOutline.children().size() == 5
        expectedDocument.save(new File("/tmp/preservedBookmarks.pdf"))

        def firstChild = expectedDocument.getDocumentCatalog().documentOutline.firstChild
        firstChild.getTitle() == "First Document"
        firstChild.nextSibling.getTitle() == "Bookmarked Document 0"
    }
}
