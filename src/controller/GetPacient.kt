package controller

import model.HospitalizationModel
import model.updatePatient
import java.time.LocalDate

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