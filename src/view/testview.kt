package gui

import controller.BaseController
import javafx.scene.layout.VBox
import tornadofx.*
import view.goHome


class TestController: BaseController()

class TestView : View(){
    private val controller: TestController by inject()

    override val root = VBox()
    init {
        with(root){
            goHome()
            button("Do stuff") {
                action {
                    println("did stuff")
                }
            }
        }
    }

}
