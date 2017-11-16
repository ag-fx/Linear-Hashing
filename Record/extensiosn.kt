
import SizeConst.*
import Validity.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

fun DataOutputStream.writeString(string: String) {
    writeInt(string.length)
    writeChars(string)
    (1..MaxStringLength.value - string.length ).forEach { writeChar(0) }
}

fun DataInputStream.readString(): String {
    var result = ""
    val stringLength = readInt()
    for (i in 1..stringLength) {
        result += readChar()
    }
    (1..MaxStringLength.value - stringLength ).forEach { readChar() }
    return result
}

fun DataOutputStream.writeValidity(validity: Validity) = writeInt(validity.value)

fun DataInputStream.readValidity() = readInt().validityValue()

fun DataOutputStream.writeDate(date: Date) = writeLong(date.time)

fun DataInputStream.readDate() = Date(readLong())

fun Int.validityValue()  = if(this==1) Valid else if (this==2) Invalid else throw IllegalArgumentException("Validitiy is either ${Valid.value} or ${Invalid.value}")

enum class Validity(val value:Int){
    Valid  (1),
    Invalid(2)
}
fun <T> emptyMutableList() = LinkedList<T>()

fun <T> Record<T>.isValid() = validity == Valid

operator fun SizeConst.plus(other: SizeConst) = other.value + this.value
operator fun Int      .plus(other: SizeConst) = other.value + this