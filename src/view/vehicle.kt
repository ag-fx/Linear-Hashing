package gui

import controller.BaseController
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.*
import tornadofx.*
import view.goHome


class VehicleController: BaseController(){
    val sout   = SimpleStringProperty()
    val get    = SimpleStringProperty()
    val insert = SimpleStringProperty()
    val delete = SimpleStringProperty()

    fun getCar(key:String){
        val found = getVehicle(key)
        get.set(found.toString())
    }

    fun insertCar(carKey: String, carName: String) {
        val success = insertVehicle(Vehicle(carName,carKey))
        insert.set("insert :$success")
    }
    fun deleteCar(carKey: String) {
        val success = deleteVehicle(carKey)
        delete.set("delete :$success")
    }

    fun genDummy() {
        val dum = listOf(
            "00000" to "mercedes benz",
            "00001" to "mercedes rew","10001" to "mercedes rew","11001" to "mercedes rew",
            "00002" to "mercedes eqw","20002" to "mercedes eqw","22002" to "mercedes eqw",
            "00003" to "mercedes eqw","30003" to "mercedes eqw","33003" to "mercedes eqw",
            "00004" to "mercedes eqw","40004" to "mercedes eqw","44004" to "mercedes eqw",
            "00005" to "mercedes asd","50005" to "mercedes asd","55005" to "mercedes asd",
            "00006" to "mercedes vw"
        )

        dum.forEach {
            insertCar(it.first,it.second)
        }
    }
}

class VehicleView : View(){
    private val controller: VehicleController by inject()
    val carKey = SimpleStringProperty()
    val newCarName = SimpleStringProperty()
    val newCarKey  = SimpleStringProperty()

    override val root = VBox()
    init {
        with(root){
            useMaxHeight = true
            goHome()
            button ("gen dummy"){
               action{ controller.genDummy() }
            }
            ///////////////GET
            vbox {
                hbox {
                    textfield("car key").bind(carKey)
                    button("GET") {
                        action {
                            println("get car ${carKey.value}")
                            controller.getCar(carKey.value)
                        }
                    }
                }

                hbox {
                    label { textProperty().bind(controller.get) }
                }
            }
            /////////////INSERT
            vbox {
                hbox {
                    textfield{promptText = "car key"} .bind(newCarKey)
                    textfield{promptText = "car name"}.bind(newCarName)
                    button("INSERT") {
                        action {
                            println("new car ${newCarKey.value},${newCarName.value}")
                            controller.insertCar(newCarKey.value,newCarName.value)
                        }
                    }
                }

                hbox {
                    label { textProperty().bind(controller.insert) }
                }
            }
            ////////////DELETE
            vbox {
                hbox {
                    textfield("car key").bind(carKey)
                    button("DELETE") {
                        action {
                            controller.deleteCar(carKey.value)
                        }
                    }
                }

                hbox {
                    label { textProperty().bind(controller.delete) }
                }
            }



        }
    }

}
