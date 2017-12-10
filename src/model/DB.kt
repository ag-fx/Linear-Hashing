package model

import AbstractData.invalidate
import LinearHashing.LinearHashingFile
import java.time.LocalDate

val instanceOfHospitalization = Hospitalization(LocalDate.now(), LocalDate.now(), "instance")
val instanceOfPatient         = Patient(PatientId(666), "987", "987", LocalDate.now(), emptyList())
val instanceOfPatientRecord   = PatientRecord(instanceOfPatient).apply { invalidate() }
val instanceOfHospitRecord    = HospitalizationRecord(instanceOfHospitalization).apply { invalidate() }

val patients = LinearHashingFile(
    pathToFile = "patients",
    instanceOfType = instanceOfPatientRecord,
    numberOfRecordsInAdditionalBlock = 3,
    maxDensity = 0.8,
    minDensity = 0.6,
    numberOfRecordsInBlock = 2,
    blockCount = 2
)


fun addPatient(patient: Patient) = patients.add(patient.toRecord())

fun getPatient(patient: PatientId) = patients.get(instanceOfPatientRecord.copy(patient = instanceOfPatient.copy(id = patient)))

fun main(args: Array<String>) {
    println()
    addPatient(Patient(15.pid(),"ahoj","ako",LocalDate.now(), emptyList()))
    println(getPatient(15.pid()))
    println()
}

inline fun Int.pid() = PatientId(this)