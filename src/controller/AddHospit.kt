package controller

import model.Hospitalization
import model.Patient
import model.insertPatient
import model.updatePatient

class AddHospitController: BaseController(){

    fun addHospitalization(item: Hospitalization) {
        val patient = patient.value!!
        val newPatient = patient.copy(hospitalizations = patient.hospitalizations + item )
        status.set(updatePatient(newPatient).toString())
        this.patient.set(null)
    }

}