package io.github.s8a.pdfmerger


import javafx.beans.binding.Bindings
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import tornadofx.*
import java.io.File
import java.util.concurrent.Callable


class PDFMergerApp : App(PDFMergerView::class)


class PDFMergerView : View() {
    private var table: TableView<Document> by singleAssign()

    private var duplicateBtn: Button by singleAssign()
    private var removeBtn: Button by singleAssign()
    private var moveUpBtn: Button by singleAssign()
    private var moveDownBtn: Button by singleAssign()
    private var mergeBtn: Button by singleAssign()

    private var documents = observableList<Document>()
    private val model = DocumentModel(Document(File("")))

    override val root = borderpane {
        title = "PDF Merger"
        setMinSize(600.0, 400.0)
        setStageIcon(Image("pdfmerger.png"))
        top = hbox {
            spacing = 5.0
            padding = insets(5.0)
            button(messages["button.add"]) {
                action { addFiles() }
            }
            button(messages["button.clear"]) {
                action { removeAll() }
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            button(messages["button.duplicate"]) {
                duplicateBtn = this
            }
            button(messages["button.remove"]) {
                removeBtn = this
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            button(messages["button.moveup"]) {
                moveUpBtn = this
            }
            button(messages["button.movedown"]) {
                moveDownBtn = this
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            region {
                prefWidth = 20.0
                hgrow = Priority.ALWAYS
            }
            button(messages["button.merge"]) {
                mergeBtn = this
            }
        }
        center = tableview(documents) {
            table = this
            readonlyColumn(messages["table.file"], Document::pathname) {
                minWidth = 400.0
                hgrow = Priority.ALWAYS
                isSortable = false
            }
            column(messages["table.start"], Document::start) {
                isSortable = false
                makeEditable()
                setOnEditCommit {
                    if (it.newValue > 0 && it.newValue <= model.end.value as Int) {
                        model.start.value = it.newValue
                    } else {
                        model.start.value = it.oldValue
                    }
                    model.commit()
                    table.refresh()
                }
            }
            column(messages["table.end"], Document::end) {
                isSortable = false
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


    init {
        val selectedIndex = table.selectionModel.selectedIndexProperty()

        duplicateBtn.action { duplicateFile() }
        duplicateBtn.disableProperty().bind(selectedIndex.lessThan(0))

        removeBtn.action { removeFile() }
        removeBtn.disableProperty().bind(selectedIndex.lessThan(0))

        moveUpBtn.action { moveUp(selectedIndex.get()) }
        moveUpBtn.disableProperty().bind(selectedIndex.lessThanOrEqualTo(0))

        moveDownBtn.action { moveDown(selectedIndex.get()) }
        moveDownBtn.disableProperty().bind(Bindings.createBooleanBinding(
                Callable {
                    val index = selectedIndex.get()
                    index < 0 || index + 1 >= table.items.size
                },
                selectedIndex, table.items
        ))

        mergeBtn.action { mergeFiles() }
        mergeBtn.disableProperty().bind(Bindings.createBooleanBinding(
                Callable { table.items.isEmpty() }, table.items))
    }

    private fun addFiles() {
        val filesToOpen = chooseFile(
                title = messages["title.openfile"],
                filters = arrayOf(FileChooser.ExtensionFilter("PDF", "*.pdf")),
                mode = FileChooserMode.Multi
        )
        for (file in filesToOpen) {
            documents.add(Document(file))
        }
    }

    private fun removeAll() {
        //TODO("Implement confirmation dialog")
        documents.clear()
    }

    private fun duplicateFile() {
        documents.add(Document(File(model.pathname.value), model.start.value as Int, model.end.value as Int))
    }

    private fun removeFile() {
        documents.removeAt(documents.indexOf(model.document))
    }

    private fun swap(fromIndex: Int, toIndex: Int) {
        documents = documents.apply { add(toIndex, removeAt(fromIndex)) }
        table.selectionModel.select(toIndex)
    }

    private fun moveUp(fromIndex: Int) {
        swap(fromIndex, fromIndex - 1)
    }

    private fun moveDown(fromIndex: Int) {
        swap(fromIndex, fromIndex + 1)
    }

    private fun mergeFiles() {
        val merger = PDFMergerUtility()
        val out = PDDocument()

        for (document in documents) {
            val splitter = Splitter()
            splitter.setStartPage(document.start)
            splitter.setEndPage(document.end)
            val splits = splitter.split(document.pdf)
            for (split in splits) {
                merger.appendDocument(out, split)
                split.close()
            }
        }

        val fileToSave = chooseFile(title = messages["title.savefile"], filters = arrayOf(), mode = FileChooserMode.Save)
        if (fileToSave.isNotEmpty()) {
            out.documentInformation = PDDocumentInformation()
            out.save(fileToSave[0])
        }

        out.close()
    }
}


fun main(args: Array<String>) {
    launch<PDFMergerApp>(args)
}
