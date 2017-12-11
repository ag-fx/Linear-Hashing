package controller

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import model.Hospitalization
import model.Patient
import model.PatientId
import model.getPatient
import tornadofx.*

open class BaseController : Controller(){
    var status = SimpleStringProperty()
    val patient          = SimpleObjectProperty<Patient?>()
    val hospitalizations = mutableListOf<Hospitalization>().observable()

    fun findPatient(id : Int){
        val foundPatient = getPatient(PatientId(id))
        if(foundPatient!=null){
            patient.set(foundPatient)
            hospitalizations.setAll(foundPatient.hospitalizations)
        }
        else{
            patient.set(null)
            hospitalizations.clear()
        }

    }
}
