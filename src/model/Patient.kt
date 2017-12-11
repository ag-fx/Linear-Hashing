package model

import AbstractData.Record
import AbstractData.SizeConst.*
import AbstractData.invalidate
import AbstractData.plus
import AbstractData.toBytes
import com.intellij.util.containers.isNullOrEmpty
import filterInvalid
import javafx.collections.FXCollections
import nextFirstname
import nextLocalDate
import nextSurname
import record.*
import record.Validity.Valid
import toDate
import toLocalDate
import tornadofx.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import rnd
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


data class Patient(
    var id               : PatientId,
    var firstName        : String    = rnd.nextFirstname(),
    var lastName         : String    = rnd.nextSurname(),
    var birthDate        : LocalDate = LocalDate.now(),
    var hospitalizations : List<Hospitalization> = Collections.nCopies(rnd.nextInt(49),Hospitalization())
){
    override fun toString() = "$id $lastName $firstName ${birthDate} ${hospitalizations.size} hospitalizations"
}

class PatientModel : ItemViewModel<Patient>() {
    val id               = bind(Patient::id)
    val firstName        = bind(Patient::firstName)
    val lastName         = bind(Patient::lastName)
    val birthDate        = bind(Patient::birthDate)
    val hospitalizations = bind(Patient::hospitalizations)

    override fun onCommit() {
        super.onCommit()
        item = Patient(
                id                  = PatientId(0),//id        .value,
                firstName           = firstName .value,
                lastName            = lastName  .value,
                birthDate           = birthDate .value,
                hospitalizations    = if(hospitalizations.value.isNullOrEmpty()) FXCollections.observableArrayList() else hospitalizations.value
            )

    }
}


data class PatientId(val value:Int){
    override fun toString() = value.toString()
}

class PatientIdModel : ItemViewModel<PatientId>() {
    val value = bind(PatientId::value)
    override fun onCommit() {
        super.onCommit()
        item = PatientId(value.value)
    }
}

inline fun Patient.toRecord() = PatientRecord(this)

data class PatientRecord(val patient: Patient) : Record<PatientRecord>{
    val maxHospitalizationCount = 100
    override fun toByteArray(): ByteArray = toBytes{
        writeValidity(validity)
        with(patient){
            writeInt(id.value)
            writeString(firstName)
            writeString(lastName)
            writeDate(birthDate.toDate())
            hospitalizations.forEach {
                write(it.toRecord().toByteArray())
            }
            val dummy = Collections.nCopies(maxHospitalizationCount - hospitalizations.size, instanceOfHospitRecord.toByteArray())
            dummy.forEach {
                write(it)
            }
        }
    }

    override fun fromByteArray(byteArray: ByteArray): PatientRecord {
        val dis = DataInputStream(ByteArrayInputStream(byteArray))
        val valid = dis.readValidity()
        val id    = dis.readInt()
        val name  = dis.readString()
        val surn  = dis.readString()
        val birht = dis.readDate().toLocalDate()
        val readList = emptyMutableList<HospitalizationRecord>()
        val recordBytes = emptyMutableList<ByteArray>()
        for (i in 0 until maxHospitalizationCount) {
            val bytes = ByteArray(instanceOfHospitRecord.byteSize)
            for (j in 0 until instanceOfHospitRecord.byteSize)
                bytes[j] = dis.readByte()
            recordBytes.add(bytes)//readList.add(instanceOfHospitRecord.fromByteArray(bytes))
        }
        recordBytes.forEach {
            readList.add(instanceOfHospitRecord.fromByteArray(it))
        }
        val toReturnHospit: List<Hospitalization> = readList.filterInvalid().map { it.hospitalization }
        return PatientRecord(Patient(PatientId(id), name, surn, birht, toReturnHospit)).apply { validity = valid }
    }

    override val hash = patient.id.value
    override val stringSize = 25
    override val byteSize
        get ()= SizeOfValidity.value + SizeOfInt.value + (2 * stringByteSize()) + SizeOfDate + ( 100 * instanceOfHospitRecord.byteSize )
    override var validity   = Valid

     override fun equals(other: Any?): Boolean {
         if(other is PatientRecord){
            return other.patient.id==this.patient.id
         }else return false

    }
}
