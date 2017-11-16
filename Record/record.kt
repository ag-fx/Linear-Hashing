@file:Suppress("MemberVisibilityCanPrivate")

import SizeConst.SizeOfValidity
import java.util.*


interface Record<T> {
    fun toBytes(): ByteArray
    fun fromBytes(bytes: ByteArray): Record<T>

    val size: Int
        get() = SizeOfValidity.value

    var validity: Validity

    val maxStringLength: Int
        get() = 50
}

class Block<T>(val records: List<Record<T>>) : Record<T> {

    val valid    = emptyMutableList<Record<T>>()
    val invalid  = emptyMutableList<Record<T>>()

    override fun toBytes(): ByteArray {
        val bytes = ArrayList<List<Byte>>()
        records.forEach {
            bytes.add(it.toBytes().asList())
        }
        return bytes.flatten().toByteArray()
    }

    override fun fromBytes(bytes: ByteArray): Record<T> {
        valid  .clear()
        invalid.clear()

        val t = records.first()
        val it = bytes.iterator()
        var counter   = 0
        val objects   = emptyMutableList<Record<T>>()
        val oneObject = emptyMutableList<Byte>()
        while (it.hasNext()) {
            if (counter < t.size) {
                oneObject.add(it.nextByte())
                counter++
            } else {
                val readObject = t.fromBytes(oneObject.toByteArray())
                objects.add(readObject)

                oneObject.clear()
                counter = 0
            }
        }
        objects.add(t.fromBytes(oneObject.toByteArray()))
        return Block(objects)
    }

    override val size: Int
        get() = TODO()
    override var validity: Validity
        get() = TODO()
        set(value) = TODO()

    override fun toString() = records.toString()
}


enum class SizeConst(val value: Int) {
    SizeOfDouble(64 / 8),
    SizeOfFloat(32 / 8),
    SizeOfLong(64 / 8),
    SizeOfInt(32 / 8),
    SizeOfShort(16 / 8),
    SizeOfByte(8 / 8),
    SizeOfChar(16 / 8),
    SizeOfValidity(SizeOfInt.value),
    SizeOfDate(SizeOfLong.value),
    MaxStringLength(50),
    SizeOfString(MaxStringLength.value * SizeOfChar.value + SizeOfInt.value /*I need int to store info about length of string*/)
}