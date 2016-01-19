package de.calmdevelopment

import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument

class PdfMerger {
    List<PDDocument> sources = []
    OutputStream destination

    def addSource(InputStream sourceDocument, Bookmarker bookmarker) {
        PDDocument document = PDDocument.load(sourceDocument)
        bookmarker.addDocument(document)
        bookmarker.bookmark()

        this.sources << document
    }

    def addSource(InputStream sourceDocument) {
        PDDocument document = PDDocument.load(sourceDocument)
        this.sources << document
    }

    def merge() {
        PDFMergerUtility merger = new PDFMergerUtility()

        sources.each { document ->
            ByteArrayOutputStream content = new ByteArrayOutputStream()
            document.save(content)
            merger.addSource(new ByteArrayInputStream(content.toByteArray()))
        }
        merger.setDestinationStream(destination)
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
    }
}
