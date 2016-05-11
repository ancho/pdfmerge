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

import de.calmdevelopment.helper.SampleDocumentBuilder
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class PdfMergerIntegrationSpec extends Specification {

    @Rule
    TemporaryFolder folder = new TemporaryFolder()

    def firstDocument
    def allpagesBookmarkedDocument
    def document

    void setup() {
        firstDocument = SampleDocumentBuilder.createDocument("First Documemt", 2)
        allpagesBookmarkedDocument = SampleDocumentBuilder.createDocumentWithBookmarks("Bookmarked Document", 4)
    }

    void cleanup() {
        document.close()
        firstDocument.close()
        allpagesBookmarkedDocument.close()
    }

    def "should merger documents"() {

        File firstFile = folder.newFile("firstDocument.pdf")
        File allpagesFile = folder.newFile("allPages.pdf")
        File destination = folder.newFile("destination.pdf")
        destination.exists() == false

        firstDocument.save(firstFile)
        allpagesBookmarkedDocument.save(allpagesFile)

        PdfMerger merger = new PdfMerger()

        merger.addSource(new FileInputStream(firstFile), new FirstPageBookmarker("First Document"))
        merger.addSource(new FileInputStream(allpagesFile), new FirstPageBookmarker("Second Document"))
        merger.destination = new FileOutputStream(destination)

        when:
        merger.merge()
        document = PDDocument.load(new FileInputStream(destination))

        then:
        destination.exists()
        Files.probeContentType(Paths.get(destination.toURI())) == "application/pdf"

        document.pages.size() == 6

        def outlineRoot = document.documentCatalog.documentOutline
        hasValidNumberOfChildren(outlineRoot, 2)
        def secondBookmark = outlineRoot.children().getAt(1)
        hasValidNumberOfChildren(secondBookmark, 4)
        def subBookmarkOfSecondBookmark = secondBookmark.getFirstChild()
        hasValidNumberOfChildren(subBookmarkOfSecondBookmark, 1)

        merger.sources[0].document.document.isClosed()

    }

    def "should apply memory usage settings"() {

        File firstFile = folder.newFile("firstDocument.pdf")
        File allpagesFile = folder.newFile("allPages.pdf")
        File destination = folder.newFile("destination.pdf")
        destination.exists() == false

        firstDocument.save(firstFile)
        allpagesBookmarkedDocument.save(allpagesFile)
        Config mockConfig = Mock(Config)

        mockConfig.getMemoryUsageSetting() >> MemoryUsageSetting.setupMainMemoryOnly()

        PdfMerger merger = new PdfMerger()
        merger.config = mockConfig

        merger.addSource(new FileInputStream(firstFile), new FirstPageBookmarker("First Document"))
        merger.addSource(new FileInputStream(allpagesFile), new FirstPageBookmarker("Second Document"))
        merger.destination = new FileOutputStream(destination)

        when:
        merger.merge()
        document = PDDocument.load(new FileInputStream(destination))

        then:
        1 * mockConfig.getMemoryUsageSetting()
    }

    def "should add blank page to documents with odd page count"() {
        firstDocument = SampleDocumentBuilder.createDocument("First Documemt", 3)

        File firstFile = folder.newFile("firstDocument.pdf")
        File allpagesFile = folder.newFile("allPages.pdf")
        File destination = folder.newFile("destination.pdf")
        destination.exists() == false

        firstDocument.save(firstFile)
        allpagesBookmarkedDocument.save(allpagesFile)

        PdfMerger merger = new PdfMerger()
        merger.config.insertBlankPages = true

        merger.addSource(new FileInputStream(firstFile), new FirstPageBookmarker("First Document"))
        merger.addSource(new FileInputStream(allpagesFile), new FirstPageBookmarker("Second Document"))
        merger.destination = new FileOutputStream(destination)

        when:
        merger.merge()
        document = PDDocument.load(new FileInputStream(destination))

        then:
        document.pages.count == 8

        document.save(new File("/tmp/blankpages.pdf"))
    }

    private boolean hasValidNumberOfChildren(PDOutlineNode node, expectedSize) {
        node.children().size() == expectedSize
    }

}
