package view

import controller.AddPacientController
import javafx.scene.layout.VBox
import model.PatientModel
import tornadofx.*

class AddPatientView : View(){
    private val controller   : AddPacientController by inject()
    private val patientModel : PatientModel         by inject()


    override val root = VBox()
    init {
        with(root){
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
                        textfield().bind(patientModel.id)
                    }

                    field("Datum narodenia") {
                        datepicker().bind(patientModel.birthDate)
                    }
                }
            }

            button("Save") {
                setOnAction {
                    patientModel.commit()
                    controller.addPatient(patientModel.item)
                    println(patientModel.item)
                }
            }
            label(controller.status)
        }
    }

}