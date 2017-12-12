package model

import AbstractData.SizeConst
import AbstractData.invalidate
import LinearHashing.LinearHashingFile
import rnd
import java.time.LocalDate

val instanceOfHospitalization = Hospitalization(LocalDate.of(2000,6,20), LocalDate.of(2000,6,20), "instance")
val instanceOfPatient         = Patient(PatientId(-1), "MENO", "PRIEZVISKO", LocalDate.of(2000,6,20), listOf(instanceOfHospitalization))
val instanceOfPatientRecord   = PatientRecord(instanceOfPatient).apply { invalidate() ; patient.hospitalizations.onEach { invalidate() }}
val instanceOfHospitRecord    = HospitalizationRecord(instanceOfHospitalization).apply { invalidate() }

val patients = LinearHashingFile(
    pathToFile = "patients",
    instanceOfType = instanceOfPatientRecord,
    numberOfRecordsInAdditionalBlock = 2,
    maxDensity = 0.75,
    minDensity = 0.55,
    numberOfRecordsInBlock = 3,
    blockCount = 2,
    deleteFiles = false
)
inline fun <A> A.log(desc : String = "",enabled:Boolean = false) = apply { if(enabled) println("$desc | ${this.toString()}") }

fun insertPatient(patient: Patient)   = patients.add(patient.toRecord())                                                                        .log("inserting patient $patient")

fun getPatient(patient: PatientId)    = patients.get(instanceOfPatientRecord    .copy(patient = instanceOfPatient.copy(id = patient)))?.patient .log("get patient $patient")

fun updatePatient(patient: Patient)   = patients.update(patient.toRecord())                                                                     .log("update patient $patient")

fun deletePatient(patient: PatientId) = patients.delete(instanceOfPatientRecord .copy(patient = instanceOfPatient.copy(id = patient)))          .log("delete patient $patient")

fun updatePatient(patient: PatientId) = patients.delete(instanceOfPatientRecord .copy(patient = instanceOfPatient.copy(id = patient)))          .log("update patient $patient")



inline fun Int.pid() = PatientId(this)