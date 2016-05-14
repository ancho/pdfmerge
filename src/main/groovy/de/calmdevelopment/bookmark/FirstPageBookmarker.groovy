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
package de.calmdevelopment.bookmark

import de.calmdevelopment.bookmark.Bookmarker
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode

class FirstPageBookmarker implements Bookmarker{
    PDDocument document
    def bookmarkTitle

    def FirstPageBookmarker(String title) {
        this.bookmarkTitle = title
    }

    @Override
    def bookmark() {
        if ( document != null ) {

            if (document.getPages().size() > 0) {

                def originalOutline = this.document.documentCatalog.documentOutline
                def newOutline = new PDDocumentOutline()
                def destination = new PDPageFitWidthDestination()
                def bookmark = new PDOutlineItem()

                destination.setPage(document.getPages().get(0))

                bookmark.title = bookmarkTitle
                bookmark.destination = destination

                if(originalOutline) {
                    appendOriginalBookmarks(originalOutline, bookmark)
                }

                newOutline.addLast(bookmark)
                document.documentCatalog.documentOutline = newOutline
            }
        }
    }

    private Iterable<PDOutlineItem> appendOriginalBookmarks(PDOutlineNode originalOutline, PDOutlineItem bookmark) {
        originalOutline.children().each { oldBookmark ->
            PDOutlineItem bookmarkClone = cloneBookmark(oldBookmark)

            if(oldBookmark.children().size() > 0){
                appendOriginalBookmarks(oldBookmark,bookmarkClone)
            }
            bookmark.addLast(bookmarkClone)
        }
    }

    private PDOutlineItem cloneBookmark(PDOutlineItem bookmark) {
        def bookmarkClone = new PDOutlineItem()
        bookmarkClone.title = bookmark.title
        bookmarkClone.destination = bookmark.destination
        bookmarkClone.action = bookmark.action
        bookmarkClone.structureElement = bookmark.structureElement
        bookmarkClone.bold = bookmark.bold
        bookmarkClone.italic = bookmark.italic
        bookmarkClone.textColor = bookmark.textColor
        bookmarkClone
    }

    @Override
    def addDocument(PDDocument document) {
        this.document = document
    }
}
