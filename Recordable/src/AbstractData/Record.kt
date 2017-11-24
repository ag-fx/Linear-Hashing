package AbstractData

import AbstractData.SizeConst.SizeOfChar
import AbstractData.SizeConst.SizeOfInt
import record.readString
import record.writeString
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

interface Record<T> : Serializable<T>{
    val stringSize : Int
    val hash : Int
        get () = Math.abs(hashCode())
    fun DataOutputStream.writeString(string : String) = writeString(string,stringSize)
    fun DataInputStream .readString()                 = readString(stringSize)
    fun stringByteSize() :Int = stringSize * SizeOfChar + SizeOfInt
}


private operator fun Int.times(sizeOf: SizeConst): Int = this * sizeOf.value

inline fun toBytes(f: DataOutputStream.() -> Unit): ByteArray{
    val byteOutStream = ByteArrayOutputStream()
    val outStream     = DataOutputStream(byteOutStream)
    with(outStream){
        f()
    }
    return byteOutStream.toByteArray()
}

