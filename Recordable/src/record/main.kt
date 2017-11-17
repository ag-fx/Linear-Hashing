package record

import record.SizeConst.*
import record.Validity.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

class Person : Record {

    var name: String
    var age: Int
    var date: Date

    constructor(){
        name = ""
        age  = -1
        date = Date(0)
    }

    constructor(name: String, age: Int, date: Date) {
        this.age = age
        this.date = date
        if (name.length < maxStringLength)
            this.name = name
        else
            throw IllegalArgumentException("Name must be shorter than $maxStringLength")
    }


    override fun toBytes(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val outStream    = DataOutputStream(outputStream)
        with(outStream) {
            writeString   (name)
            writeInt      (age)
            writeDate     (date)
            writeValidity (validity)
        }
        return outputStream.toByteArray()
    }

    override fun fromBytes(bytes: ByteArray): Person {
        val dis  = DataInputStream(ByteArrayInputStream(bytes))
        val name = dis.readString()
        val age  = dis.readInt()
        val date = dis.readDate()
        return Person(name, age, date).apply { validity = dis.readValidity() }
    }

    override val size: Int
        get() = super.size + SizeOfString + SizeOfInt + SizeOfDate

    override var validity = Valid

    override fun toString() = "$validity $name $age ${date.time}"

}
