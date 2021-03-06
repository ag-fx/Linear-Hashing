package view

import controller.AddPacientController
import javafx.scene.layout.VBox
import model.PatientIdModel
import model.PatientModel
import tornadofx.*

class AddPatientView : View() {
    private val controller: AddPacientController by inject()
    private val patientModel: PatientModel         by inject()
    private val patientIdModel: PatientIdModel     by inject()


    override val root = VBox()

    init {
        with(root) {
            goHome()
            form {
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
                setOnAction {
                    patientIdModel.commit()

                    patientModel.commit()
                    patientModel.item.id = patientIdModel.item
                    controller.addPatient(patientModel.item)
                    println(patientModel.item)
                }
            }

            label(controller.status)
        }
    }

}