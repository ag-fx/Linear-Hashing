package view

import com.intellij.remoteServer.util.Column
import controller.AddPacientController
import controller.FindPacientController
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.*
import tornadofx.*
import java.time.LocalDate
import java.util.*

class FindPatientView : View() {
    private val controller      : FindPacientController by inject()
    private val patientIdModel  : PatientIdModel         by inject()
    private val hospitalization : HospitalizationModel   by inject()

    override val root = VBox()
    init {
        with(root){
            goHome()

            hbox {
                textfield {
                    promptText = "ID pacienta"
                }.bind(patientIdModel.value)

                button("Najdi") {
                    action {
                        patientIdModel.commit()
                        controller.findPatient(patientIdModel.item.value)
                    }
                }
            }

            vbox {
                label("Pacient : "){
                    textProperty().bind(controller.patient.asString())
                }
//                tableview(controller.foundPatients){
//                    column("Cislo preukazu", Patient::id)
//
//                }
            }

            tableview(controller.hospitalizations ){
                column("Prichod",Hospitalization::start)
                column("Odchod",Hospitalization::end)
                column("Diagnoza",Hospitalization::diagnosis)
                bindSelected(hospitalization)
            }
            button("dummy add"){
                action {
                    val t = Patient(PatientId(5))
                    insertPatient(Patient(PatientId(1)))
                    insertPatient(Patient(PatientId(2)))
                    insertPatient(Patient(PatientId(3)))
                }
            }
            button("Ukonci"){

                action {
                    controller.endHospit(hospitalization)
                }
            }
        }
    }
}