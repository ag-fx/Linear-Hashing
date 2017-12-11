package controller

import model.Patient
import model.insertPatient

class EditPacientController: BaseController(){

    fun addPatient(item: Patient?) = item?.let{
        val success =insertPatient(item)
        if(success)
            status.set("Added")
        else
            status.set("Not Added")
    } ?:    status.set("Not Added")

}

