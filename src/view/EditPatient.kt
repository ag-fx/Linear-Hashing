package view

import controller.AddPacientController
import javafx.scene.layout.VBox
import model.Patient
import model.PatientIdModel
import model.PatientModel
import tornadofx.*

class EditPatientView : View() {
    private val controller: AddPacientController by inject()
    private val patientModel: PatientModel         by inject()
    private val patientIdModel: PatientIdModel     by inject()


    override val root = VBox()

    init {
        with(root) {
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
            label{
                textProperty().bind(controller.patient.asString())
            }
            form {
                enableWhen { controller.patient.isNotNull }
                fieldset("Personal Information") {
                    field("Meno") {
                        textfield().bind(patientModel.firstName)
                    }

                    field("Priezvisko") {
                        textfield().bind(patientModel.lastName)
                    }

                    field("Cislo preukazu") {
                        textfield().bind(patientIdModel.value)
                    }

                    field("Datum narodenia") {
                        datepicker().bind(patientModel.birthDate)
                    }
                }
            }

            button("Save") {
                enableWhen { controller.patient.isNotNull }
                setOnAction {
                    patientIdModel.commit()

                    patientModel.commit()
                    patientModel.item.id = patientIdModel.item
                    controller.updatePatient(patientModel.item)
                    println(patientModel.item)
                }
            }

            label(controller.status)
        }
    }

}