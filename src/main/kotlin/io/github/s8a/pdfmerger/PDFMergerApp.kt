package io.github.s8a.pdfmerger


import java.io.File
import java.util.concurrent.Callable
import javafx.beans.binding.Bindings
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import tornadofx.*


class PDFMergerApp : App(PDFMergerView::class)


class PDFMergerView : View("PDF Merger") {
    private var table : TableView<Document> by singleAssign()
    private var moveUpBtn : Button by singleAssign()
    private var moveDownBtn : Button by singleAssign()

    private var documents = observableList<Document>()
    private val model = DocumentModel(Document(File("")))

    override val root = borderpane {
        setMinSize(600.0, 400.0)
        top = hbox {
            spacing = 5.0
            padding = insets(5.0)
            button("Add") {
                action { addFiles() }
            }
            button("Clear") {
                action { removeAll() }
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            button("Duplicate") {
                action { duplicateFile() }
            }
            button("Remove") {
                action { removeFile() }
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            button("Move Up") {
                moveUpBtn = this
            }
            button("Move Down") {
                moveDownBtn = this
            }
            separator {
                orientation = Orientation.VERTICAL
            }
            region {
                prefWidth = 20.0
                hgrow = Priority.ALWAYS
            }
            button("Merge") {
                action { mergeFiles() }
            }
        }
        center = tableview(documents) {
            table = this
            readonlyColumn("File", Document::pathname) {
                minWidth = 400.0
                hgrow = Priority.ALWAYS
                isSortable = false
            }
            column("Start", Document::start) {
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
            column("End", Document::end) {
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

        moveUpBtn.action { moveUp(selectedIndex.get()) }
        moveUpBtn.disableProperty().bind(selectedIndex.lessThanOrEqualTo(0))

        moveDownBtn.action { moveDown(selectedIndex.get()) }
        moveDownBtn.disableProperty().bind(Bindings.createBooleanBinding(Callable{
            val index = selectedIndex.get()
            index < 0 || index + 1 >= table.items.size
        }, selectedIndex, table.items))
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
        //TODO("Implement merging")
    }
}


fun main(args: Array<String>) {
    launch<PDFMergerApp>(args)
}
