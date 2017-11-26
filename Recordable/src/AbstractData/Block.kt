package AbstractData

import record.Validity
import record.Validity.*

interface Block<T>  : Serializable<Block<T>>{
    var data          : MutableList<T>
    var addressInFile : Int
    val blockSize     : Int
    var recordCount   : Int
    val ofType        : T
}

fun <T : Serializable<T>> Block<T>.add(item: T): Boolean {
    if (isFull()) return false
    else for (i in 0 until data.size) {
        if (data[i].isInvalid()) {
            data[i] = item
            return true
        }
    }
    data.add(item)
    return true
}

fun <T : Serializable<T> > Block<T>.get   (item: T) : T? = data.firstOrNull{item == it }
fun <T : Serializable<T> > Block<T>.delete(item: T) : Boolean = get(item)?.let { it.validity = Invalid; true  } ?: false
fun <T : Serializable<T> > Block<T>.isFull() : Boolean = data.size==blockSize && data.all{it.isValid()}
fun <T : Serializable<T> > Block<T>.isNotFull() : Boolean = !isFull()