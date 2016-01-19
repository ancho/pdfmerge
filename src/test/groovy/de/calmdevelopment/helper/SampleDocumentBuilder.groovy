package de.calmdevelopment.helper

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font

/**
 * Created by frank on 18.01.16.
 */
class SampleDocumentBuilder {
    public static PDDocument createDocument(String pageMessage, int numberOfPages) {
        PDDocument document = new PDDocument()

        for (int i = 0; i < numberOfPages; i++) {
            PDPage page = new PDPage()
            addContent(document,page,"$pageMessage $i")
            document.addPage(page)
        }
        return document
    }

    public static addContent(PDDocument doc, PDPage page, String message){

        PDFont font = PDType1Font.HELVETICA_BOLD;

        PDPageContentStream contents = new PDPageContentStream(doc, page);
        contents.beginText();
        contents.setFont(font, 12);
        contents.newLineAtOffset(100, 700);
        contents.showText(message);
        contents.endText();
        contents.close();
    }

}
