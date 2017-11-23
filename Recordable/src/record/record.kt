@file:Suppress("MemberVisibilityCanPrivate")

package record

import record.SizeConst.SizeOfInt
import record.SizeConst.SizeOfValidity
import record.Validity.Valid
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

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
//    val blockSize : Int
//    val data      : List<T>
//    val record    : T
//
//    constructor(data: List<T>, blockSize: Int, type: KClass<T>) {
//        this.data      = data
//        this.blockSize = blockSize
//        this.record    = type.createInstance()
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
//        if(result.size > blockSize) throw IllegalStateException("Block size overflow")
//        return result
//    }
//
//    override fun fromBytes(bytes: ByteArray) : Record {
//        val dis         = DataInputStream(ByteArrayInputStream(bytes))
//        val validity    = dis.readValidity()
//        val blockSize   = dis.readInt()
//
//    TODO()
//    }
//
//
//    override val maxStringLength: Int
//        get() = throw IllegalStateException ("Block shoudln't")
//
//    override val size: Int
//        get()       = blockSize * record.size + SizeOfInt + SizeOfValidity + SizeOfInt
//    override var validity: Validity
//        get()       = Valid
//        set(value)  = TODO()
//
//    override fun toString() = data.toString()
//}


