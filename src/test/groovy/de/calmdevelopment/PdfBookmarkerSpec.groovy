package de.calmdevelopment

import de.calmdevelopment.helper.SampleDocumentBuilder
import org.apache.pdfbox.pdmodel.PDDocument
import spock.lang.Specification

class PdfBookmarkerSpec extends Specification {
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
        Bookmarker bookmarker = new FirstPageBookmarker(sampleDocument,bookmarkTitle);

        when:
        bookmarker.bookmark()
        sampleDocument.save(outputStream)

        then:
        def firstBookmark = sampleDocument.getDocumentCatalog().documentOutline.getFirstChild()
        firstBookmark.findDestinationPage(sampleDocument) == sampleDocument.getPage(0)

        PDDocument loaded = PDDocument.load(new ByteArrayInputStream(outputStream.toByteArray()))
        loaded.getDocumentCatalog().getDocumentOutline() != null
    }

}
