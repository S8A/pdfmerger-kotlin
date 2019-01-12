package io.github.s8a.pdfmerger


import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File


class PDFMergerApp : App(PDFMergerView::class)


class PDFMergerView : View("PDF Merger") {
    private var table : TableView<Document> by singleAssign()
    private var documents = observableList(
            Document(File("/home/s8a/Documents/Umc/UmcDocumentosInscripcion.pdf")),
            Document(File("/home/s8a/Documents/Umc/UmcIndicesAcademicos2018-02.completo.pdf")),
            Document(File("/home/s8a/Documents/Umc/UmcIndicesAcademicos2018-02.nuevo.pdf"))
    )
    private val model = DocumentModel(Document(File("")))
    /*TODO("Fix to enable buttons only when the document can actually be move up or down")
    private var canMoveUp = SimpleBooleanProperty(documents.indexOf(model.document) > 0)
    private var canMoveDown = SimpleBooleanProperty(documents.indexOf(model.document) < documents.size - 1)*/

    override val root = borderpane {
        setPrefSize(600.0, 400.0)
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
            button("Remove") {
                hboxConstraints {
                    margin = insets(5.0)
                }
                action { removeFile() }
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            button("Move Up") {
                hboxConstraints {
                    margin = insets(5.0)
                }
                //TODO("Fix enableWhen(canMoveUp)")
                action { moveUp() }
            }
            button("Move Down") {
                hboxConstraints {
                    margin = insets(5.0)
                }
                //TODO("Fix enableWhen(canMoveDown)")
                action { moveDown() }
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            region {
                prefWidth = 20.0
                hgrow = Priority.ALWAYS
            }
            button("Merge") {
                hboxConstraints {
                    margin = insets(5.0)
                }
                action { mergeFiles() }
            }
        }
        center = tableview(documents) {
            table = this
            readonlyColumn("File", Document::pathname)
            column("Start", Document::start) {
                makeEditable()
                setOnEditCommit {
                    if (it.newValue >= 0 && it.newValue <= model.end.value as Int) {
                        model.start.value = it.newValue
                    } else {
                        model.start.value = it.oldValue
                    }
                    model.commit()
                    table.refresh()
                }
            }
            column("End", Document::end) {
                makeEditable()
                setOnEditCommit {
                    val max = model.document.pdf.numberOfPages
                    if (it.newValue >= model.start.value as Int && it.newValue <= max) {
                        model.end.value = it.newValue
                    } else {
                        model.end.value = it.oldValue
                    }
                    model.commit()
                    table.refresh()
                }
            }

            model.rebindOnChange(this) { selectedDocument ->
                document = selectedDocument ?: Document(File(""))
            }
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
        documents.add(Document(File(model.pathname.value)))
    }

    private fun removeFile() {
        documents.removeAt(documents.indexOf(model.document))
    }

    private fun swap(fromIndex: Int, toIndex: Int) {
        documents = documents.apply { add(toIndex, removeAt(fromIndex)) }
    }

    private fun moveUp() {
        val fromIndex = documents.indexOf(model.document)
        if (fromIndex == 0) return
        val toIndex = fromIndex - 1
        swap(fromIndex, toIndex)
    }

    private fun moveDown() {
        val fromIndex = documents.indexOf(model.document)
        if (fromIndex == documents.size - 1) return
        val toIndex = fromIndex + 1
        swap(fromIndex, toIndex)
    }

    private fun mergeFiles() {
        //TODO("Implement merging")
    }
}


fun main(args: Array<String>) {
    launch<PDFMergerApp>(args)
}
