package view

import controller.AddHospitController
import controller.AddPacientController
import javafx.scene.layout.VBox
import model.*
import tornadofx.*

class AddHospitalizationView : View() {
    private val controller      : AddHospitController   by inject()
    private val hospitalizaion  : HospitalizationModel  by inject()
    private val patientIdModel  : PatientIdModel        by inject()


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
            }

            hbox {
                vbox {
                    label("Prichod")
                    datepicker().bind(hospitalizaion.start)
                }
                vbox {
                    label("Odchod")
                    datepicker().bind(hospitalizaion.end)
                }
                textfield {
                    promptText = "Diagnoza"
                }.bind(hospitalizaion.diagnosis)
            }

            button("Pridat"){
                enableWhen { controller.patient.isNotNull }
                action {
                    hospitalizaion.commit()
                    controller.addHospitalization(hospitalizaion.item)
                }
            }
            label(controller.status)
        }
    }


}