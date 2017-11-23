package AbstractData

import record.*
import record.SizeConst.*
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

interface Record<T> : Serializable<T>{
    val stringSize : Int
    fun DataOutputStream.writeString(string : String) = writeString(string,stringSize)
    fun DataInputStream .readString() = readString(stringSize)
    fun stringByteSize() :Int =   stringSize * SizeOfChar + SizeOfInt
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