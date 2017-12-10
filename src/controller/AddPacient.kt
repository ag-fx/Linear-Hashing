package controller

import model.Patient
import model.addPatient

class AddPacientController: BaseController(){


    fun addPatient(item: Patient?) = item?.let{
        //val success = //addPatient(item)
       // if(success)
            status.set("Added")
      //  else
            status.set("Not Added")

    } ?: false

}
