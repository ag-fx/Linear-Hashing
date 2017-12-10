@file:Suppress("NOTHING_TO_INLINE")

package record

import AbstractData.SizeConst.SizeOfInt
import record.Validity.Invalid
import record.Validity.Valid
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

inline fun DataOutputStream.writeString(string: String, maxStringSize: Int) {
    writeInt(string.length)
    writeChars(string)
    (1..maxStringSize - string.length).forEach { writeChar(0) }
}

inline fun DataInputStream.readString(maxStringSize: Int): String {
    var result = ""
    val stringLength = readInt()
    for (i in 1..stringLength) {
        result += readChar()
    }
    (1..maxStringSize - stringLength ).forEach { readChar() }
    return result
}

inline fun DataOutputStream.writeValidity(validity: Validity) = writeInt(validity.value)
inline fun DataInputStream .readValidity() = readInt().validityValue()

inline fun DataOutputStream.writeDate(date: Date) = writeLong(date.time)
inline fun DataInputStream .readDate() = Date(readLong())

inline fun Int.validityValue()  = if(this==1) Valid else if (this==2) Invalid else throw IllegalArgumentException("Validitiy is either ${Valid.value} or ${Invalid.value}")

enum class Validity(val value:Int){
    Valid  (1),
    Invalid(2)
}
fun <T> emptyMutableList() = LinkedList<T>()

fun repeat(block: () -> Unit) = block
infix inline fun (() -> Unit).until(cond: () -> Boolean) { while (!cond()) this() }