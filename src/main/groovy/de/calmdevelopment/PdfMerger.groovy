package de.calmdevelopment

import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility

class PdfMerger {
    def sources = []
    OutputStream destination

    def addSource(InputStream sourceDocument) {
        this.sources << sourceDocument
    }

    def merge() {
        PDFMergerUtility merger = new PDFMergerUtility()
        merger.addSources(sources)
        merger.setDestinationStream(destination)
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
    }
}
