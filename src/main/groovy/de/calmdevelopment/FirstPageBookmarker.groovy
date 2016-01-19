package de.calmdevelopment

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem

class FirstPageBookmarker implements Bookmarker{
    PDDocument document
    def bookmarkTitle

    def FirstPageBookmarker(String title) {
        this.bookmarkTitle = title
    }

    @Override
    def bookmark() {
        if ( document != null ) {
            def documentOutline = new PDDocumentOutline()
            this.document.getDocumentCatalog().documentOutline = documentOutline

            if (document.getPages().size() > 0) {
                def destination = new PDPageFitWidthDestination()
                destination.setPage(document.getPages().get(0))
                def bookmark = new PDOutlineItem()
                bookmark.title = bookmarkTitle
                bookmark.destination = destination
                documentOutline.addLast(bookmark)
            }
        }
    }

    @Override
    def addDocument(PDDocument document) {
        this.document = document
    }
}
