package view

import com.intellij.remoteServer.util.Column
import controller.AddPacientController
import controller.FindPacientController
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.*
import tornadofx.*
import java.time.LocalDate
import java.util.*

class Generator : View() {
    var numberOfPatients = SimpleIntegerProperty()
    var generated = SimpleIntegerProperty()


    override val root = VBox()
    init {
        with(root) {
            goHome()

            hbox {
                textfield {
                    promptText = "Pocet pacientov"
                }.bind(numberOfPatients)

                button("Generuj pacientov") {
                    action {
                        runAsyncWithProgress{
                            val cur = System.currentTimeMillis()
                            (1..numberOfPatients.value).forEach {
                                val id = PatientId(it)
                                val p = Patient(id)
                                    insertPatient(p)                            }
                            println("done after ${System.currentTimeMillis()-cur} ")
                        }
                    }
                }
                hbox{
                    label().bind(numberOfPatients)
                    label("/")

                }
            }
        }
    }
}