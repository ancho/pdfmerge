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
package de.calmdevelopment.helper

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem

class SampleDocumentBuilder {
    public static PDDocument createDocument(String pageMessage, int numberOfPages) {
        PDDocument document = new PDDocument()

        for (int i = 0; i < numberOfPages; i++) {
            PDPage page = new PDPage()
            addContent(document, page, "$pageMessage $i")
            document.addPage(page)
        }
        return document
    }

    public static addContent(PDDocument doc, PDPage page, String message) {

        PDFont font = PDType1Font.HELVETICA_BOLD;

        PDPageContentStream contents = new PDPageContentStream(doc, page);
        contents.beginText();
        contents.setFont(font, 12);
        contents.newLineAtOffset(100, 700);
        contents.showText(message);
        contents.endText();
        contents.close();
    }

    static PDDocument createDocumentWithBookmarks(String pageMessage, int numbersOfPages) {
        PDDocument document = createDocument(pageMessage, numbersOfPages)

        PDDocumentOutline outline = new PDDocumentOutline()
        document.getDocumentCatalog().documentOutline = outline
        document.pages.eachWithIndex { page, index ->

            def destination = new PDPageFitWidthDestination()
            destination.setPage(page)
            PDOutlineItem bookmark = new PDOutlineItem()
            bookmark.title = "$pageMessage $index"
            bookmark.destination = destination
            outline.addLast(bookmark)
        }
        return document
    }
}
