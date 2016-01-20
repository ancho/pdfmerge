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
