package model

import AbstractData.Record
import AbstractData.SizeConst.*
import AbstractData.plus
import AbstractData.toBytes
import com.intellij.util.containers.isNullOrEmpty
import javafx.collections.FXCollections
import record.*
import record.Validity.Valid
import toDate
import toLocalDate
import tornadofx.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.time.LocalDate

data class PatientId(val value:Int)

data class Patient(
    var id               : PatientId,
    var firstName        : String,
    var lastName         : String,
    var birthDate        : LocalDate,
    var hospitalizations : List<Hospitalization>
)

class PatientModel : ItemViewModel<Patient>() {
    val id = bind(Patient::id)
    val firstName = bind(Patient::firstName)
    val lastName = bind(Patient::lastName)
    val birthDate = bind(Patient::birthDate)
    val hospitalizations = bind(Patient::hospitalizations)

    override fun onCommit() {
        super.onCommit()
        item = Patient(
                id = id .value,
                firstName = firstName .value,
                lastName = lastName .value,
                birthDate = birthDate .value,
                hospitalizations= if(hospitalizations.value.isNullOrEmpty()) FXCollections.observableArrayList() else hospitalizations.value
            )

    }
}


inline fun Patient.toRecord() = PatientRecord(this)

data class PatientRecord(val patient: Patient) : Record<PatientRecord>{

    override fun toByteArray(): ByteArray = toBytes{
        writeValidity(validity)
        with(patient){
            writeInt(id.value)
            writeString(firstName)
            writeString(lastName)
            writeDate(birthDate.toDate())
        }
    }

    override fun fromByteArray(byteArray: ByteArray): PatientRecord {
        val dis = DataInputStream(ByteArrayInputStream(byteArray))
        val valid = dis.readValidity()
        val id    = dis.readInt()
        val name  = dis.readString()
        val surn  = dis.readString()
        val birht = dis.readDate().toLocalDate()
        return PatientRecord(Patient(PatientId(id),name,surn,birht, emptyList())).apply { validity = valid }
    }

    override val hash       = patient.id.value
    override val byteSize
        get ()= SizeOfValidity.value + SizeOfInt.value + (2 * stringByteSize()) + SizeOfDate //+ 100 * instanceOfHospitRecord.byteSize
    override var validity   = Valid
    override val stringSize = 25

     override fun equals(other: Any?): Boolean {
         if(other is PatientRecord){
            return other.patient.id==this.patient.id
         }else return false

    }
}
