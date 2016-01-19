package de.calmdevelopment

import org.apache.pdfbox.pdmodel.PDDocument

/**
 * Created by frank on 18.01.16.
 */
interface Bookmarker {

    def bookmark()
    def addDocument(PDDocument document)
}