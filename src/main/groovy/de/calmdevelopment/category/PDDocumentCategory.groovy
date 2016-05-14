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
package de.calmdevelopment.category

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font

/**
 * Apply custom methods to a {@link PDDocument}:
 * <pre>
 * PDDocument document = PDDocument.load("/path/to/some.pdf")
 *
 * use ( PDDocumentCategory ) {
 *      // append a blank page
 *      document.appendBlankPage()
 *
 *      // append a blank page only if page count is odd
 *      document.appendBlankPage() {
 *          document.hasOddPageCount()
 *      }
 * }
 * </pre>
 */
@Category(PDDocument)
class PDDocumentCategory {

    /**
     * Append a blank page to a {@link PDDocument} instance
     *
     * @param condition An optional boolean condition closure whether to append the blank page or not
     */
    void appendBlankPage(Closure<Boolean> condition){

        def execute = true

        if ( condition ) {
            execute = condition()
        }

        if ( execute ) {
            def blankPage = new PDPage()
            this.addPage(blankPage)

            PDFont font = PDType1Font.HELVETICA_BOLD;

            PDPageContentStream contents = new PDPageContentStream(this, blankPage);
            contents.beginText();
            contents.setFont(font, 12);
            contents.showText("");
            contents.endText();
            contents.close();
        }
    }

    /**
     * Has a {@link PDDocument} an odd number of pages
     * @return {@code true} if number of pages is odd otherwise {@code false}
     */
    boolean hasOddPageCount() {
        this.pages.size() % 2
    }
}