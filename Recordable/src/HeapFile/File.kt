package HeapFile

import AbstractData.*
import record.emptyMutableList
import src.ReadWrite
import java.util.*

class HeapFile<T : Record<T>> {

    private val blockRecordCount: Int
    private val blockSize: Int get () = instanceOfBlock.byteSize
    val heapFile: ReadWrite
    private val instanceOfRecord: T
    private val instanceOfBlock: HeapFileBlock<T>

    val emptyBlockAddresses       = emptyMutableList<Int>()

    constructor(path: String, instanceOfRecord: T, numberOfRecordsInBlock: Int) {
        this.heapFile         = ReadWrite(path)
        this.instanceOfRecord = instanceOfRecord
        this.instanceOfBlock  = HeapFileBlock(numberOfRecordsInBlock, instanceOfRecord)
        this.blockRecordCount = 0
    }


    /**
     * @return returns address of the block that records has been inserted to
     */

    fun add(record: T): Int {
        val currentBlock = getBlock(getAddress())
        currentBlock.add(record)
        emptyBlockAddresses.remove(currentBlock.addressInFile)
        val addressInHeapFile = currentBlock.addressInFile
        heapFile.writeFrom(currentBlock.addressInFile, currentBlock.toByteArray())
        return addressInHeapFile
    }

    /**
     * @return returns the very first address of additional block
     * if the block has additional block address X and the additional
     * we inserted to was n-th additional block of the original block
     * this functions returns X
     */
    @Suppress("UNCHECKED_CAST")
    fun add(block: Block<T>, record: T): Int {
        val additionalBlockStartAddress = block.additionalBlockAddress
        val lastEmptyAdditionalBlockNotFound = true

        var block = block
        while(lastEmptyAdditionalBlockNotFound){
            val blockState = block.additionalBlockState(this)
            when (blockState) {
                is AdditionalBlockState.Full<*> -> {
                    val currentBlock = (blockState.block as Block<T>)
                    if(currentBlock.contains(record))
                        return additionalBlockStartAddress

                    if (currentBlock.hasAdditionalBlock()){
                        block = getBlock(currentBlock.additionalBlockAddress)
                    }
                    else {
                        currentBlock.additionalBlockAddress = add(record)
                        heapFile.writeFrom(currentBlock.addressInFile, currentBlock.toByteArray())

                        return additionalBlockStartAddress
                    }

                }
                is AdditionalBlockState.NotFull<*> -> {
                    (blockState.block as Block<T>).add(record)
                    heapFile.writeFrom(blockState.block.addressInFile, blockState.block.toByteArray())
                    return additionalBlockStartAddress
                }
                is AdditionalBlockState.NotExisting ->  return add(record)

                }
            }
        throw  IllegalStateException("Record should be added to the last or new additional block")
    }

    fun get(address: Int, record: T) = getBlock(address).data.firstOrNull { it == record }

    fun getAddress(): Int = if (emptyBlockAddresses.isNotEmpty())
            emptyBlockAddresses.removeFirst()
        else {
            val lastPositionInFile = heapFile.size().toInt()
            val newBlock = instanceOfBlock.copy()
            newBlock.addressInFile = lastPositionInFile
            heapFile.writeFrom(position = lastPositionInFile, byteArray = newBlock.toByteArray())
            emptyBlockAddresses.add(lastPositionInFile)
            /*return*/ lastPositionInFile
        }


    fun getBlock(address: Int): Block<T> {
        val blockBytes = heapFile.read(blockSize,address)
        return instanceOfBlock.fromByteArray(blockBytes)
    }

    fun allBlocksInFile() : List<List<T>>{
        val blocksInFile = heapFile.size() / blockSize
        val blocks = emptyMutableList<Block<T>>()
        (0 until blocksInFile)
            .map { instanceOfBlock.fromByteArray(heapFile.read(blockSize, (it * blockSize).toInt())) }
            .forEach { blocks += it }
        return blocks.map { it.data }
    }

    fun allRecordsInFile() = allBlocksInFile().flatten()
    fun allocateBlock(): Int = getAddress()

}