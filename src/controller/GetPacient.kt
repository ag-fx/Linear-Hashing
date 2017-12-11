package controller

import javafx.beans.property.SimpleObjectProperty
import model.*
import record.emptyMutableList
import tornadofx.*
import java.time.LocalDate
import java.util.*

class FindPacientController: BaseController() {

    fun endHospit(hospitalization: HospitalizationModel) {
        val p = patient.value!!
        val oldH = hospitalization.item
        val newH = hospitalization.item.copy(end=LocalDate.now())
        val newP = p.copy(hospitalizations = (p.hospitalizations - oldH) + newH)
        val success = updatePatient(newP)
        if(success){
            findPatient(p.id.value)
        }else{
            status.set("fail")
        }


    }
}