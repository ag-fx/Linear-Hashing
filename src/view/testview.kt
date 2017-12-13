package gui

import controller.BaseController
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.patients
import tornadofx.*
import view.goHome


class TestController: BaseController(){
    val sout = SimpleStringProperty()

    fun printblocks(){
        val b = patients.blokInfo()
        var sb = StringBuilder()
        b.forEach {
            sb.append("""

                main : ${it.first}
                addi : ${it.second}

            """.trimIndent())

        }
        sout.set(sb.toString())
    }
}

class TestView : View(){
    private val controller: TestController by inject()

    override val root = VBox()
    init {
        with(root){
            useMaxHeight = true

            goHome()
            button("block info") {
                action {
                  controller.printblocks()
                }
            }
            textarea {
                vgrow = Priority.ALWAYS
                isCache = false
                scrollpane { isCache=false;childrenUnmodifiable.forEach { it.isCache=false } }
                useMaxHeight = true
                useMaxWidth = true
            }.bind(controller.sout)

        }
    }

}
