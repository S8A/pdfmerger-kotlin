package io.github.s8a.pdfmerger


import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import org.apache.pdfbox.pdmodel.PDDocument
import tornadofx.*
import java.io.File


class Document(file: File) {
    val pathnameProperty = SimpleStringProperty(file.absolutePath)
    var pathname by pathnameProperty

    val pdf = if (file.path.isNotEmpty()) PDDocument.load(file) else PDDocument()

    val startProperty = SimpleIntegerProperty(0)
    var start by startProperty

    val endProperty = SimpleIntegerProperty(pdf.numberOfPages)
    var end by endProperty
}


class DocumentModel(var document: Document) : ViewModel() {
    val pathname = bind{document.pathnameProperty}
    var start = bind{document.startProperty}
    var end = bind{document.endProperty}
}
