package model

import AbstractData.*
import AbstractData.SizeConst.*
import nextDate
import nextDiagnosis
import nextLocalDate
import nextLocalDateNullable
import record.*
import rnd
import rndNull
import toDate
import toLocalDate
import tornadofx.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.time.LocalDate
import java.util.*

data class Hospitalization(
    var start       : LocalDate  = LocalDate.now(),
    var end         : LocalDate? = LocalDate.now(),
    var diagnosis   : String     = Random().nextDiagnosis()
)

class HospitalizationModel : ItemViewModel<Hospitalization>() {
    val start = bind(Hospitalization::start)
    val end = bind(Hospitalization::end)
    val diagnosis = bind(Hospitalization::diagnosis)
    override fun onCommit() {
        super.onCommit()
        item = Hospitalization(
            start = start.value,
            end  = end.value,
            diagnosis =  diagnosis.value
        )
    }
}

fun Hospitalization.toRecord() = HospitalizationRecord(this)

data class HospitalizationRecord(val hospitalization: Hospitalization) : Record<HospitalizationRecord> {

    override fun toByteArray(): ByteArray = toBytes{
        writeValidity(validity)
        with(hospitalization){
            writeString(diagnosis)
            writeDate(start.toDate())
            writeDate(end?.toDate() ?: Date(0))
        }
    }

    override fun fromByteArray(byteArray: ByteArray): HospitalizationRecord {
        val dis = DataInputStream(ByteArrayInputStream(byteArray))
        val valid     = dis.readValidity()
        val diagnosis = dis.readString()
        val startDate = dis.readDate().toLocalDate()
        val endDate   = dis.readDate()
        return if (endDate == Date(0))
            HospitalizationRecord(Hospitalization(startDate, null, diagnosis)).apply { validity = valid }
        else
            HospitalizationRecord(Hospitalization(startDate, endDate.toLocalDate(), diagnosis)).apply { validity = valid }
    }

    override val stringSize = 40
    override val byteSize = stringByteSize() + SizeOfValidity  + SizeOfDate * 2
    override var validity : Validity = Validity.Valid
}
