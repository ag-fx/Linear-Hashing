@file:Suppress("MemberVisibilityCanPrivate")

package record

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.reflect.KClass
//import kotlin.reflect.full.createInstance
//import kotlin.reflect.full.primaryConstructor

//
//interface Record {
//    fun toBytes()                   : ByteArray
//    fun fromBytes(bytes: ByteArray) : Record
//    val size: Int
//    var validity: Validity
//    val maxStringLength: Int
//
//
//}
//
//class Block< T: Record> : Record {
//
//    val numberOfRecordsInBlock : Int
//    val data      : List<T>
//    val record    : T
//
//    constructor(data: List<T>, numberOfRecordsInBlock: Int, instanceOfType: KClass<T>) {
//        this.data      = data
//        this.numberOfRecordsInBlock = numberOfRecordsInBlock
//        this.record    = instanceOfType.createInstance()
//    }
//
//
//    override fun toBytes(): ByteArray {
//        val byteOutStream = ByteArrayOutputStream()
//        val outStream     = DataOutputStream(byteOutStream)
//        with(outStream){
//            writeValidity (validity)
//            writeInt      (data.size)
//            data
//                .forEach {
//                write(it.toBytes())
//            }
//        }
//
//        val result =  byteOutStream.toByteArray()
//        if(result.size > numberOfRecordsInBlock) throw IllegalStateException("Block size overflow")
//        return result
//    }
//
//    override fun fromBytes(bytes: ByteArray) : Record {
//        val dis         = DataInputStream(ByteArrayInputStream(bytes))
//        val validity    = dis.readValidity()
//        val numberOfRecordsInBlock   = dis.readInt()
//
//    TODO()
//    }
//
//
//    override val maxStringLength: Int
//        get() = throw IllegalStateException ("Block shoudln't")
//
//    override val size: Int
//        get()       = numberOfRecordsInBlock * record.size + SizeOfInt + SizeOfValidity + SizeOfInt
//    override var validity: Validity
//        get()       = Valid
//        set(value)  = TODO()
//
//    override fun toString() = data.toString()
//}


