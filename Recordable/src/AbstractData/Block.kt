package AbstractData

import record.Validity.*

interface Block<T>  : Serializable<Block<T>>{
    var data          : MutableList<T>
    var addressInFile : Int
    val blockSize     : Int
    var recordCount   : Int
    val ofType        : T
    var additionalBlockAddress :Int
}

fun <T : Serializable<T>> Block<T>.contains(item: T) = data.firstOrNull { it == item }?.let { true } ?: false
fun <T : Serializable<T>> Block<T>.add(item: T): Boolean {
    if (isFull()) return false
    if(data.contains(item)) return false
    else for (i in 0 until data.size) {
        if (data[i].isInvalid()) {
            data[i] = item
            return true
        }
    }
    data.add(item)
    return true
}

 fun <T : Serializable<T>> Block<T>.update(item: T): Boolean {
     for (i in 0 until data.size) {
        if (data[i] == item) {
            data[i] = item
            return true
        }
    }
    return false
}

enum class BlockState { Full, NotFull }

fun <T : Serializable<T> > Block<T>.state() : BlockState = when{
        isFull()    -> BlockState.Full
        isNotFull() -> BlockState.NotFull
        else -> throw IllegalStateException()
}

val NoAdditionalBlockAddress = -1
fun <T : Serializable<T>> Block<T>.get(item: T)            = data.firstOrNull { item == it }
fun <T : Serializable<T>> Block<T>.delete(item: T)         = get(item)?.let { it.validity = Invalid; true  } ?: false
fun <T : Serializable<T>> Block<T>.isFull()                = data.all{it.isValid()}
fun <T : Serializable<T>> Block<T>.isNotFull()             = !isFull()
fun <T : Serializable<T>> Block<T>.isEmpty()               = data.all{it.isInvalid()}
fun <T : Serializable<T>> Block<T>.hasAdditionalBlock()    = additionalBlockAddress != NoAdditionalBlockAddress
fun <T : Serializable<T>> Block<T>.hasNotAdditionalBlock() = !hasAdditionalBlock()