@file:Suppress("MemberVisibilityCanPrivate")

package record

import record.SizeConst.*
import record.Validity.Valid
import java.io.ByteArrayInputStream
import java.util.*
import kotlin.reflect.KClass
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.internal.impl.resolve.constants.KClassValue


interface Record {
    fun toBytes()                   : ByteArray
    fun fromBytes(bytes: ByteArray) : Record

    val size: Int
        get() = SizeConst.SizeOfValidity.value

    var validity: Validity

    val maxStringLength: Int
        get() = 50
}

class Block< T: Record>(of: T, val capacity: Int) : Record {
    val type  = of
    val recordSize = type.size
    var validCount = 0
    val data    = emptyMutableList<T>()
    val valid   = emptyMutableList<T>()
    val invalid = emptyMutableList<T>()

    /*    val records      : MutableList<Record>
    val maxCapacity  : Int
    val type         : Record
    val recordSize   : Int
    val blockSize    : Int
    val validCount   : Int
    val invalidCount : Int

    private val valid   = emptyMutableList<Record>()
    private val invalid = emptyMutableList<Record>()

    constructor(of: KClass<Record>, capacity: Int) {
        records = emptyMutableList()

        this.validCount     = 0
        this.invalidCount   = 0
        this.maxCapacity = capacity
        this.type           = of.objectInstance!!
        this.recordSize     = type.size
        blockSize           = this.recordSize * capacity
    }

    constructor(recordSize:Int, capacity: Int){
        this.records = emptyMutableList()
        this.validCount   = 0
        this.invalidCount = 0
        this.maxCapacity  = capacity
        this.type         = null
        this.recordSize   = recordSize
        blockSize         = this.recordSize * capacity
    }

    constructor(records : List<Record>, validCount:Int,  capacity: Int) {
        this.records = emptyMutableList()
        this.records.addAll(records)
        this.validCount   = validCount
        this.invalidCount = capacity - validCount
        this.maxCapacity  = capacity
        this.type         = records.first()
        this.recordSize   = type.size
        blockSize         = this.recordSize * capacity
    }*/

    override fun toBytes(): ByteArray {
        val bytes = ArrayList<List<Byte>>(capacity)
        val byteOutStream = ByteArrayOutputStream()
        val outStream = DataOutputStream(byteOutStream)

        with(outStream){
            writeInt(capacity)
        }

        data.forEach {
            bytes.add(it.toBytes().asList())
        }
        return byteOutStream.toByteArray() + bytes.flatten().toByteArray()
    }

    override fun fromBytes(bytes: ByteArray) : Record {

        val iterator: ByteIterator = bytes.iterator()
        val dis  = DataInputStream(ByteArrayInputStream(bytes))
        val maxCapacity  = dis.readInt()
        iterator.skipInt()
        var counter   = 0
        val oneObject = emptyMutableList<Byte>()
        while (iterator.hasNext()) {
            if (counter < recordSize) {
                oneObject.add(iterator.nextByte())
                counter++
            } else {
                readObject(oneObject)
                oneObject.clear()
                counter = 0
            }
        }
        //iterator doesn't have next, bud there's one last object to add
        readObject(oneObject)
        val newBlock = Block(type,maxCapacity)

           newBlock.data     .addAll (valid+invalid)
           newBlock.valid    .addAll (valid)
           newBlock.invalid  .addAll (invalid)
           newBlock.validCount = valid.size

        println()
        return newBlock

    }


    private fun readObject(bytes : List<Byte>){
        val readObject = type.fromBytes(bytes.toByteArray()) as T
        if(readObject.isValid())
            valid.add(readObject)
        else
            invalid.add(readObject)

    }
    override val size: Int
        get() = capacity*recordSize + SizeOfInt
    override var validity: Validity
        get() = Valid
        set(value) = TODO()

    override fun toString() = data.toString()
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
    SizeOfDate(SizeConst.SizeOfLong.value),
    MaxStringLength(50),
    SizeOfString(SizeConst.MaxStringLength.value * SizeConst.SizeOfChar.value + SizeOfInt.value /*I need int to store info about length of string*/)
}

fun<T: Any> T.getClass() = javaClass.kotlin
fun ByteIterator.skipInt() = (1..SizeOfInt.value).forEach { nextByte() }