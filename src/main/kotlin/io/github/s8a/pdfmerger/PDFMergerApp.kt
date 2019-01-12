package io.github.s8a.pdfmerger


import javafx.scene.control.TableCell
import javafx.scene.control.TableView
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File


class PDFMergerApp : App(PDFMergerView::class)


class PDFMergerView : View("PDF Merger") {
    var documents = observableList(
            Document(File("/home/s8a/Documents/Umc/UmcDocumentosInscripcion.pdf")),
            Document(File("/home/s8a/Documents/Umc/UmcIndicesAcademicos2018-02.completo.pdf")),
            Document(File("/home/s8a/Documents/Umc/UmcIndicesAcademicos2018-02.nuevo.pdf"))
    )
    val model = DocumentModel(Document(File("")))


    override val root = borderpane {
        top = hbox {
            button("Add") {
                hboxConstraints {
                    margin = insets(5.0)
                }
                action { addFiles() }
            }
            button("Duplicate") {
                hboxConstraints {
                    margin = insets(5.0)
                }
                action { duplicateFile() }
            }
        }
        center = tableview(documents) {
            readonlyColumn("File", Document::pathname)
            column("Start", Document::start).makeEditable()
            column("End", Document::end).makeEditable()
        }
    }

    private fun addFiles() {
        val filesToOpen = chooseFile(
                title = "Open file",
                filters = arrayOf(FileChooser.ExtensionFilter("PDF", "*.pdf")),
                mode = FileChooserMode.Multi
        )
        for (file in filesToOpen) {
            documents.add(Document(file))
        }
    }

    private fun duplicateFile() {
        //TODO("Implement duplicateFile function")
    }
}


fun main(args: Array<String>) {
    launch<PDFMergerApp>(args)
}
