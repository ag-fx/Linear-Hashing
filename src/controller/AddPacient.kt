package controller

import model.Patient
import model.deletePatient
import model.insertPatient

class AddPacientController: BaseController(){

    fun addPatient(item: Patient?) = item?.let{
        val success =insertPatient(item)
        if(success)
            status.set("Added")
        else
            status.set("Not Added")
    } ?:    status.set("Not Added")

    fun updatePatient(newP: Patient) {
        val oldP = patient.value!!
        if(oldP.id==newP.id){
            val new = newP.copy(hospitalizations = oldP.hospitalizations)
            status.set(model.updatePatient(new).toString())
        }
        else{
            val new = newP.copy(hospitalizations = oldP.hospitalizations)
            val delete = deletePatient(oldP.id)
            val insert = insertPatient(new)
            status.set("Delete : $delete \n Insert : $insert")

        }
    }

}

