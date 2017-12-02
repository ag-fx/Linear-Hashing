package AbstractData

import AbstractData.AdditionalBlockState.NotExisting
import HeapFile.HeapFile
import record.Validity.*


interface Block<T>  : Serializable<Block<T>>{
    var data          : MutableList<T>
    var addressInFile : Int
    val blockSize     : Int
    var recordCount   : Int
    val ofType        : T
    var additionalBlockAddress :Int
}

fun <T : Serializable<T>> Block<T>.contains(item:T) = data.firstOrNull{it==item}?.let { true } ?: false
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

enum class BlockState { Full, NotFull }

sealed class AdditionalBlockState {
    class Full        <T : Record<T>>(val block: Block<T>) : AdditionalBlockState()
    class NotFull<T : Record<T>>(val block: Block<T>) : AdditionalBlockState()
    object NotExisting : AdditionalBlockState()
}
fun <T : Record<T> > Block<T>.additionalBlockState(additionalFile : HeapFile<T>) = when{
        hasAdditionalBlock() -> {
            val additionalBlock: Block<T> = additionalFile.getBlock(additionalBlockAddress)
            when{
                additionalBlock.isFull()    -> AdditionalBlockState.Full(additionalBlock)
                additionalBlock.isNotFull() -> AdditionalBlockState.NotFull(additionalBlock)
                else -> throw IllegalStateException()
            }
        }
        hasNotAdditionalBlock() -> NotExisting
        else -> throw IllegalStateException()

}
fun <T : Serializable<T> > Block<T>.state() : BlockState = when{
        isFull()    -> BlockState.Full
        isNotFull() -> BlockState.NotFull
        else -> throw IllegalStateException()
    }

val NoAdditionalBlockAddress = -1
fun <T : Serializable<T> > Block<T>.get   (item: T) : T? = data.firstOrNull{item == it }
fun <T : Serializable<T> > Block<T>.delete(item: T) : Boolean = get(item)?.let { it.validity = Invalid; true  } ?: false
fun <T : Serializable<T> > Block<T>.isFull() : Boolean = data.all{it.isValid()}
fun <T : Serializable<T> > Block<T>.isNotFull() : Boolean = !isFull()
fun <T : Serializable<T> > Block<T>.hasAdditionalBlock()    : Boolean = additionalBlockAddress != NoAdditionalBlockAddress
fun <T : Serializable<T> > Block<T>.hasNotAdditionalBlock() : Boolean = !hasAdditionalBlock()