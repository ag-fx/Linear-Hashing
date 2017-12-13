package model

import AbstractData.Record
import AbstractData.SizeConst
import AbstractData.SizeConst.*
import AbstractData.isValid
import AbstractData.toBytes
import record.Validity
import record.readValidity
import record.writeValidity
import java.io.ByteArrayInputStream
import java.io.DataInputStream

data class Vehicle  (val nameOfCar:String, val key : String ) {

}
 fun Vehicle.toRecord() = VehicleRecord(this)

data class VehicleRecord(val vehicle:Vehicle) : Record<VehicleRecord>{

    override fun toByteArray() = toBytes {
        writeString(vehicle.key)
        writeString(vehicle.nameOfCar)
        writeValidity(validity)
    }

    override fun fromByteArray(byteArray: ByteArray): VehicleRecord {
        val dis = DataInputStream(ByteArrayInputStream(byteArray))
        val myKey = dis.readString()
        val name  = dis.readString()
        val vaid  = dis.readValidity()
        return VehicleRecord(Vehicle(name,myKey)).apply { validity=vaid }
    }

    override val stringSize          = 20
    override val byteSize            = (stringByteSize() * 2) + SizeOfValidity.value
    override var validity            = Validity.Valid
    override val hash                = Math.abs(vehicle.key.hashCode())+1
    override fun equals(other: Any?) = (other is VehicleRecord ) && other.vehicle.key.contentEquals(this.vehicle.key) && (this.isValid() && other.isValid())
}
