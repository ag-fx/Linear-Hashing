package HeapFile

import AbstractData.*
import record.emptyMutableList
import src.ReadWrite
import java.io.RandomAccessFile
import java.util.*

class HeapFile<T : Record<T>> {

    private val blockRecordCount: Int
    private val blockSize: Int get () = instanceOfBlock.byteSize
    private val heapFile: ReadWrite
    private val instanceOfRecord: T
    private val instanceOfBlock: HeapFileBlock<T>
    var totalNumberOfRecords = 0

    val emptyBlockAddresses  = emptyMutableList<Int>()

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
        val emptyAddress = getAddress()
        val heapBlock    = getBlock(emptyAddress)
        var success      = heapBlock.add(record)
        if(success)
            totalNumberOfRecords++
        else
            println("$record has not been added")

        emptyBlockAddresses.remove(heapBlock.addressInFile)
        val addressInHeapFile = heapBlock.addressInFile
        heapBlock.writeToFile()
        return addressInHeapFile
    }



    private fun Block<T>.writeToFile() = heapFile.writeFrom(addressInFile,toByteArray())

    /**
     * @return returns the very first address of additional linearHashBlock
     * if the linearHashBlock has additional linearHashBlock address X and the additional
     * we inserted to was n-th additional linearHashBlock of the original linearHashBlock
     * this functions returns X
     */
    @Suppress("UNCHECKED_CAST")
    fun add(linearHashBlock: Block<T>, record: T): AddResult {
        //val additionalBlockStartAddress = linearHashBlock.additionalBlockAddress

        if (linearHashBlock.hasAdditionalBlock()) {
            val additionalBlockNotFound = true
            var additionalBlock = getBlock(linearHashBlock.additionalBlockAddress)

            while(additionalBlockNotFound){
                if(additionalBlock.contains(record)) return AddResult.RecordWasNotAdded

                if (additionalBlock.isFull()) {
                    if (additionalBlock.hasAdditionalBlock()) {
                        additionalBlock = getBlock(additionalBlock.additionalBlockAddress)
                    } else {
                        additionalBlock.additionalBlockAddress = add(record)
                        additionalBlock.writeToFile()
                        return AddResult.RecordAddedToNewBlock(additionalBlock.additionalBlockAddress)
                    }
                } else {
                    val success = additionalBlock.add(record)
                    if (success) {
                        additionalBlock.writeToFile()
                        return AddResult.RecordAddedToExistingBlock
                    } else {
                        return AddResult.RecordWasNotAdded
                    }

                }

            }
        } else {
            return AddResult.FirstAdditionalBlock(newBlockAddress = add(record))
        }
        throw IllegalStateException("This should not end up here")
    }

    sealed class AddResult{
        object RecordAddedToExistingBlock                           :AddResult()
        data class RecordAddedToNewBlock(val newBlockAddress:Int)   :AddResult()
        data class FirstAdditionalBlock (val newBlockAddress:Int)   :AddResult()
        object RecordWasNotAdded                                    :AddResult()
    }
    private fun getAddress(): Int = if (emptyBlockAddresses.isNotEmpty())
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

    fun getAdditionalBlocks(additionalBlockAddress: Int, invalidateThem:Boolean = false): List<List<T>> {
        if(additionalBlockAddress== NoAdditionalBlockAddress ) return emptyList()
        var additionalBlockAddress = additionalBlockAddress
        val foundRecords = emptyMutableList<List<T>>()
        val lastNotFound = true
        while (lastNotFound) {
            val block = getBlock(additionalBlockAddress)
            if (block.hasAdditionalBlock()) {
                foundRecords.add(block.data)
                additionalBlockAddress = block.additionalBlockAddress
                if(invalidateThem){
                    /*                                               i'm doing this to create a duplicate of object to get rid of reference                                   */
                    val copy = (block as HeapFileBlock).copy().apply{ data = data.map { x ->  x.toByteArray().let(x::fromByteArray) }.onEach { it.invalidate() ; totalNumberOfRecords-- }.toMutableList()}
                    copy.writeToFile()
                    emptyBlockAddresses.add(block.addressInFile) //TODO poratat tie bloky ktore som zmazal
                }
            } else {
                foundRecords.add(block.data)
                if(invalidateThem){
                    val copy = (block as HeapFileBlock).copy().apply{ data = data.map { x ->  x.toByteArray().let(x::fromByteArray) }.onEach { it.invalidate() ; totalNumberOfRecords--  }.toMutableList()}
                    copy.writeToFile()
                    emptyBlockAddresses.add(block.addressInFile)
                    tryTrim()
                }

                return foundRecords
            }
        }
        throw IllegalStateException("this should not end up here")
    }

    fun getRecord(additionalBlockAddress: Int, record: T): T? {
        var additionalBlockAddress = additionalBlockAddress
        val lastNotFound = true
        while(lastNotFound){
            val block = getBlock(additionalBlockAddress)
            if(block.hasAdditionalBlock()){
                if(block.contains(record)) return block.get(record)
                else additionalBlockAddress = block.additionalBlockAddress
            }else
            if(block.hasNotAdditionalBlock()){
                return block.get(record)
            }
        }
        return null
    }

    fun deleteAndShake(additionalBlockAddress: Int, record: T) : Boolean{
        val partlyEmptyBlocks   = emptyMutableList<Block<T>>()
        val readBlocks          = emptyMutableList<Block<T>>()
        var block               = getBlock(additionalBlockAddress)

        while (block.hasAdditionalBlock()) {
            readBlocks.add(block)

            if (block.contains(record)) {
                block.delete(record)
            }

            if(block.isNotFull())
                partlyEmptyBlocks.add(block)

            block = getBlock(block.additionalBlockAddress)
        }

        val lastBlock                    = readBlocks.pop()
        val lastBlockData                = LinkedList(lastBlock.data)
        val blockThatPointsToLastBlock   = readBlocks.pop()
        var loopSecurity                 = 0
        var pointerToLastBlockNotRemoved = true

        while(lastBlockData.isNotEmpty()){
            if(loopSecurity++ > 2000) throw IllegalStateException("I'm probably stuck in a a loop")
            var partlyEmptyBlock = partlyEmptyBlocks.pop()
            while(partlyEmptyBlock.isNotFull()){
                partlyEmptyBlock.add(lastBlockData.pop())
            }

            //in case that this block points to the last block, I can clear the pointer at once
            if (partlyEmptyBlock.addressInFile == blockThatPointsToLastBlock.addressInFile){
                partlyEmptyBlock.additionalBlockAddress = NoAdditionalBlockAddress
                pointerToLastBlockNotRemoved = false
            }
            partlyEmptyBlock.writeToFile()
        }

        if(pointerToLastBlockNotRemoved){
            blockThatPointsToLastBlock.additionalBlockAddress = NoAdditionalBlockAddress
            blockThatPointsToLastBlock.writeToFile()
        }
        emptyBlockAddresses.add(lastBlock.addressInFile)
        tryTrim()
        return true
    }

    fun delete(additionalBlockAddress: Int, record: T): Boolean {
        var additionalBlockAddress = additionalBlockAddress
        val lastNotFound = true
        while (lastNotFound) {
            val block = getBlock(additionalBlockAddress)
            if(block.contains(record)){
                block.delete(record)
                block.writeToFile()
                return true
            }

            if (block.hasAdditionalBlock()) {
                additionalBlockAddress = block.additionalBlockAddress
            } else {
                return false
            }
        }
        throw IllegalStateException("this should not end up here")
    }

    fun getLastBlock(startAddress : Int) : Block<T>{
        var address = startAddress
        while(true){
            val block = getBlock(address)
            if(block.hasNotAdditionalBlock())
                return block
            else
                address = block.additionalBlockAddress
        }

    }

    private fun tryTrim() {
        while(heapFile.size() > 0) {

            val lastBlock = getBlock((heapFile.size() - instanceOfBlock.byteSize).toInt())
            if(lastBlock.isEmpty()){
                heapFile.shrink(instanceOfBlock.byteSize)
                emptyBlockAddresses.remove(lastBlock.addressInFile)
            }
            else
                return
        }
    }

}
