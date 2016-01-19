package de.calmdevelopment

import de.calmdevelopment.helper.SampleDocumentBuilder
import org.apache.pdfbox.pdmodel.PDDocument
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by frank on 20.01.16.
 */
class PdfMergerIntegrationSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    def "should merger documents"() {
        def firstDocument = SampleDocumentBuilder.createDocument("First Documemt", 2)
        def allpagesBookmarkedDocument = SampleDocumentBuilder.createDocumentWithBookmarks("Bookmarked Document", 4)

        File firstFile = folder.newFile("firstDocument.pdf")
        File allpagesFile = folder.newFile("allPages.pdf")
        File destination = folder.newFile("destination.pdf")
        destination.exists() == false

        firstDocument.save(firstFile)
        allpagesBookmarkedDocument.save(allpagesFile)

        PdfMerger merger = new PdfMerger()

        merger.addSource(new FileInputStream(firstFile), new FirstPageBookmarker("First Document"))
        merger.addSource(new FileInputStream(allpagesFile))
        merger.destination = new FileOutputStream(destination)

        when:
        merger.merge()

        then:
        destination.exists()
        Files.probeContentType(Paths.get(destination.toURI())) == "application/pdf"
        PDDocument document = PDDocument.load(new FileInputStream(destination))

        document.pages.size() == 6
        document.documentCatalog.documentOutline.children().size() == 5

    }


}
