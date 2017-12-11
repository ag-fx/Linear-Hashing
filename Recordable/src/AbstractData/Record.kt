package AbstractData

import AbstractData.SizeConst.SizeOfChar
import AbstractData.SizeConst.SizeOfInt
import record.Validity
import record.readString
import record.writeString
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.RandomAccessFile
import java.lang.Math.abs
import java.util.*

interface Record<out T> : Serializable<T>{
    val stringSize : Int
    val hash : Int
        get () = Math.abs(hashCode())
    fun DataOutputStream.writeString(string : String) = writeString(string,stringSize)
    fun DataInputStream .readString()                 = readString(stringSize)
    fun stringByteSize() :Int = stringSize * SizeOfChar + SizeOfInt
}
 fun <T:Serializable<T>> Record<T>.invalidate() {
    validity=Validity.Invalid
}
 fun <T:Serializable<T>> Record<T>.validate() {
    validity=Validity.Valid
}

fun toBytes(f: DataOutputStream.() -> Unit): ByteArray{
    val byteOutStream = ByteArrayOutputStream()
    val outStream     = DataOutputStream(byteOutStream)
    with(outStream){
        f()
    }
    return byteOutStream.toByteArray()
}

 fun <T:Record<T>> T.copy()  =this.toByteArray().let { this.fromByteArray(it) }
