
package record

import AbstractData.SizeConst
import AbstractData.SizeConst.*
import AbstractData.plus
import AbstractData.times
import com.sun.xml.internal.ws.util.StringUtils
import record.Validity.Invalid
import record.Validity.Valid
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

fun DataOutputStream.writeString(string: String, maxStringSize: Int) {
    writeInt(string.length)
    write(string.toByteArray())
    val sizeLeft = ByteArray((maxStringSize - string.length))
    write(sizeLeft)
}

 fun DataInputStream.readString(maxStringSize: Int): String {
    var result = ""
    val stringLength = readInt()
    val bytes = ByteArray(maxStringSize)
    read(bytes)
    val byt = ByteArray(stringLength)
    System.arraycopy(bytes,0,byt,0,stringLength)
    val read = String(byt)
   ///  skipBytes(maxStringSize-stringLength * SizeOfChar.value)

    return  read// String(bytes)//result
}


fun DataOutputStream.writeValidity(validity: Validity) = writeInt(validity.value)

fun DataInputStream .readValidity() = readInt().validityValue()

fun DataOutputStream.writeDate(date: Date) = writeLong(date.time)

fun DataInputStream .readDate() = Date(readLong())

fun Int.validityValue()  = if(this==1) Valid else if (this==2) Invalid else throw IllegalArgumentException("Validitiy is either ${Valid.value} or ${Invalid.value}")

enum class Validity(val value:Int){
    Valid  (1),
    Invalid(2)
}
fun <T> emptyMutableList() = LinkedList<T>()
