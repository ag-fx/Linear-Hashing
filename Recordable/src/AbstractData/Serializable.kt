package AbstractData

import record.Validity

interface Serializable<out T> {
    fun toByteArray(): ByteArray
    fun fromByteArray(byteArray: ByteArray): T
    val byteSize: Int
    var validity: Validity
}

fun <T> Serializable<T>.isValid  () = validity == Validity.Valid
fun <T> Serializable<T>.isInvalid() = validity == Validity.Invalid