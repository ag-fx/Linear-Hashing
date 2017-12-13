package model

import AbstractData.SizeConst
import AbstractData.invalidate
import AbstractData.validate
import LinearHashing.LinearHashingFile
import rnd
import java.time.LocalDate

val instanceOfHospitalization = Hospitalization(LocalDate.of(2000,6,20), LocalDate.of(2000,6,20), "instance")
val instanceOfPatient         = Patient(PatientId(-1), "MENO", "PRIEZVISKO", LocalDate.of(2000,6,20), listOf(instanceOfHospitalization))
val instanceOfVehicle         = Vehicle(key = "123456",nameOfCar = "instance")
val instanceOfPatientRecord   = PatientRecord(instanceOfPatient).apply { invalidate() ; patient.hospitalizations.onEach { invalidate() }}
val instanceOfHospitRecord    = HospitalizationRecord(instanceOfHospitalization).apply { invalidate() }
val instanceOfvehicleRecord   = VehicleRecord(instanceOfVehicle).apply { invalidate() }

val patients = LinearHashingFile(
    pathToFile = "patients",
    instanceOfType = instanceOfPatientRecord,
    numberOfRecordsInAdditionalBlock = 3,
    maxDensity = 0.75,
    minDensity = 0.55,
    numberOfRecordsInBlock = 3,
    blockCount = 3,
    deleteFiles = false
)

val vehicles = LinearHashingFile(
    pathToFile = "vehicles",
    instanceOfType = instanceOfvehicleRecord,
    numberOfRecordsInAdditionalBlock = 2,
    maxDensity = 0.75,
    minDensity = 0.55,
    numberOfRecordsInBlock = 4,
    blockCount = 2,
    deleteFiles = false
)


fun insertPatient(patient: Patient)   = patients.add(patient.toRecord())    .log("inserting patient $patient")

fun getPatient(patient: PatientId)    = patients.get(patient)?.patient      .log("get patient $patient")

fun updatePatient(patient: Patient)   = patients.update(patient.toRecord()) .log("update patient $patient")

fun deletePatient(patient: PatientId) = patients.delete(patient)            .log("delete patient $patient")

fun insertVehicle(vehicle: Vehicle)   = vehicles.add(vehicle.toRecord())    .log("insert vehicle $vehicle")
fun getVehicle   (vehicle: String )   = vehicles.get(vehicle)               .log("getVehicle vehicle $vehicle")//?.vehicle
fun deleteVehicle(vehicle: String)    = vehicles.delete(vehicle)            .log("deleteVehicle vehicle $vehicle")

//region helpers
// i made a huge mistake  ...that's why i have this
fun LinearHashingFile<PatientRecord>.get(patient: PatientId)    = get(instanceOfPatientRecord    .copy(patient = instanceOfPatient.copy(id = patient)))
fun LinearHashingFile<PatientRecord>.delete(patient: PatientId) = delete(instanceOfPatientRecord    .copy(patient = instanceOfPatient.copy(id = patient)))

fun LinearHashingFile<VehicleRecord>.get(vehicle: String   )    = get(instanceOfvehicleRecord       .copy(vehicle = instanceOfVehicle.copy(key = vehicle)))
fun LinearHashingFile<VehicleRecord>.delete(vehicle: String)    = delete(instanceOfvehicleRecord    .copy(vehicle = instanceOfVehicle.copy(key = vehicle)))
fun <A> A.log(desc : String = "",enabled:Boolean = true) = apply { if(enabled) println("$desc | ${this.toString()}") }

//endregion