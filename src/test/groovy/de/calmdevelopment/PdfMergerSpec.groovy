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
    private ByteArrayOutputStream destinationStream
    private PDDocument firstDocument
    private PDDocument secondDocument

    def setup() {
        fistDocumentOutputStream = new ByteArrayOutputStream()
        secondDocumentOutputStream = new ByteArrayOutputStream()
        destinationStream = new ByteArrayOutputStream()

        firstDocument = SampleDocumentBuilder.createDocument("First Documemt", 2)
        secondDocument = SampleDocumentBuilder.createDocument("Second Document", 3)

        new FirstPageBookmarker(firstDocument, "First Document").bookmark()
        new FirstPageBookmarker(secondDocument, "Second Document").bookmark()

        firstDocument.save(fistDocumentOutputStream)
        secondDocument.save(secondDocumentOutputStream)
        
    }

    void cleanup() {
        fistDocumentOutputStream.close()
        secondDocumentOutputStream.close()
        destinationStream.close()

        firstDocument.close()
        secondDocument.close()
    }

    def "shouldMergeDocumentsWithBookmarks"() {
        given:
        PdfMerger merger = new PdfMerger()
        merger.addSource(new ByteArrayInputStream(fistDocumentOutputStream.toByteArray()))
        merger.addSource(new ByteArrayInputStream(secondDocumentOutputStream.toByteArray()))
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
}
