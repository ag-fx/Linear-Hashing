package view

import gui.TestView
import javafx.application.Application.launch
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.layout.BorderPane
import model.Patient
import model.PatientId
import model.insertPatient
import model.patients
import tornadofx.*

fun main(args: Array<String>) = launch<MyApp>(args)


class MyApp : App(MainView::class)

class MainView : View() {

    override val root = BorderPane()

    val centerView = find(CenterView::class)

    init {
        with(root) {
            center = centerView.root
        }
    }
}

fun View.goHome(f: () -> Unit = {}) = hbox {

    button("Back") {
        action {
            f()
            replaceWith(
                CenterView::class,
                ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.RIGHT)
                , true, true
            )
            close()
        }
    }
}

class CenterView : View() {

    val menu = listOf(
        Pair("1. Vyhľadanie záznamov pacienta ", FindPatientView::class),
        Pair("x. Hospitalizacia "              , AddHospitalizationView::class),
        Pair("6. Pridanie pacienta"            , AddPatientView ::class),
        Pair("x. Edit pacienta"                , EditPatientView ::class),
        Pair("Generator"                       , Generator ::class),
        Pair("bloky"                           , TestView ::class)
    )

    override val root = vbox {
        prefWidth = 800.toDouble()
        prefHeight = 600.toDouble()
        useMaxHeight = true

        style {
            padding = box(20.px)
        }
        menu.forEach {
            hbox {
                button(it.first) {
                    hboxConstraints { margin = Insets(5.0) }
                    action {
                        replaceWith(it.second, ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.LEFT))
                    }
                }
            }
        }
        button("exit"){
            action {
                patients.close()
                Platform.exit()
                System.exit(0)


            }
        }

    }
}
