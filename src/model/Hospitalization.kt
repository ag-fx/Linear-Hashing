package model

import AbstractData.*
import AbstractData.SizeConst.*
import record.*
import toDate
import toLocalDate
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.time.LocalDate
import java.util.*

data class Hospitalization(
    var start       : LocalDate,
    var end         : LocalDate?,
    var diagnosis   : String
)

inline fun Hospitalization.toRecord() = HospitalizationRecord(this)
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

    override val byteSize = stringByteSize()*2 + SizeOfValidity  + SizeOfDate * 2
    override var validity : Validity = Validity.Valid
    override val stringSize = 40
}
