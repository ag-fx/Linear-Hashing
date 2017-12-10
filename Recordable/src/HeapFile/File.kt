package HeapFile

import AbstractData.*
import record.emptyMutableList
import src.ReadWrite

class HeapFile<T : Record<T>> {

    private val blockRecordCount: Int
    private val blockSize: Int get () = instanceOfBlock.byteSize
    private val heapFile: ReadWrite
    private val instanceOfRecord: T
    private val instanceOfBlock: HeapFileBlock<T>
    var totalNumberOfRecords = 0
    var totalNumberOfBlocks = 0

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
                        totalNumberOfRecords++
                        additionalBlock.writeToFile()
                        return AddResult.RecordAddedToExistingBlock
                    } else {
                        return AddResult.RecordWasNotAdded
                    }

                }

            }
        } else {
            val emptyAddress = getAddress()
            val heapBlock    = getBlock(emptyAddress)
            var success      = heapBlock.add(record)
            if(success)
                totalNumberOfRecords++
            else
                return AddResult.RecordWasNotAdded

            emptyBlockAddresses.remove(heapBlock.addressInFile)
            val addressInHeapFile = heapBlock.addressInFile
            heapBlock.writeToFile()
            return AddResult.FirstAdditionalBlock(newBlockAddress = addressInHeapFile)
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
            totalNumberOfBlocks++
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
        var deleteCount = 0
        while (lastNotFound) {
            val block = getBlock(additionalBlockAddress)
            if (block.hasAdditionalBlock()) {
                foundRecords.add(block.data)
                additionalBlockAddress = block.additionalBlockAddress
                if(invalidateThem){
                    deleteCount += block.data.filter { it.isValid() }.size
                    /*                                               i'm doing this to create a duplicate of object to get rid of reference                                   */
                    val copy = (block as HeapFileBlock).copy().apply{ data = data.map { x ->  x.toByteArray().let(x::fromByteArray) }.onEach { /*if(it.isValid()) totalNumberOfRecords--;*/ it.invalidate()  /*; totalNumberOfRecords-- */ }.toMutableList()}
                    copy.additionalBlockAddress= NoAdditionalBlockAddress
                    copy.writeToFile()
                    emptyBlockAddresses.add(block.addressInFile) //TODO poratat tie bloky ktore som zmazal
                }
            } else {
                foundRecords.add(block.data)
                if(invalidateThem){
                    deleteCount += block.data.filter { it.isValid() }.size
                    val copy = (block as HeapFileBlock).copy().apply{ data = data.map { x ->  x.toByteArray().let(x::fromByteArray) }.onEach {/* if(it.isValid()) totalNumberOfRecords--;*/ it.invalidate()  /*; totalNumberOfRecords--*/  }.toMutableList()}
                    copy.additionalBlockAddress = NoAdditionalBlockAddress
                    copy.writeToFile()
                    emptyBlockAddresses.add(block.addressInFile)
                    tryTrim()
                    totalNumberOfRecords -= deleteCount
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

    fun delete(additionalBlockAddress: Int, record: T): DeleteResult {
        var additionalBlockAddress = additionalBlockAddress
        val readBlocks = emptyMutableList<Block<T>>()
        val lastNotFound = true
        while (lastNotFound) {
            val block = getBlock(additionalBlockAddress)
            readBlocks.add(block)
            if(block.contains(record)){
                block.delete(record)
                totalNumberOfRecords--
                block.writeToFile()
                if(block.isEmpty()){
                    if(readBlocks.size==1 && block.hasNotAdditionalBlock()){
                        emptyBlockAddresses.add(block.addressInFile)
                        totalNumberOfBlocks--
                        return DeleteResult.AllAdditionalDeleted
                    }else if(readBlocks.size==1 && block.hasAdditionalBlock()){
                        emptyBlockAddresses.add(block.addressInFile)
                        totalNumberOfBlocks--
                        return DeleteResult.AdditionalStartAddressMoved(block.additionalBlockAddress)
                    }
                    else{
                        val blockThatPointsToTheLast = readBlocks.first { it.additionalBlockAddress == block.addressInFile }
                        //if(block.additionalBlockAddress != NoAdditionalBlockAddress)
                        blockThatPointsToTheLast.additionalBlockAddress = block.additionalBlockAddress//NoAdditionalBlockAddress
                        blockThatPointsToTheLast.writeToFile()
                        emptyBlockAddresses.add(block.addressInFile)
                        totalNumberOfBlocks--
                        return DeleteResult.BlockAndRecordDeleted
                    }
                }
//                tryTrim()
                return DeleteResult.Deleted
            }

            if (block.hasAdditionalBlock()) {
                additionalBlockAddress = block.additionalBlockAddress
            } else {
                return DeleteResult.NotDeleted
            }
        }
        throw IllegalStateException("this should not end up here")
    }

    sealed class DeleteResult{
        object Deleted               : DeleteResult()
        object NotDeleted            : DeleteResult()
        object BlockAndRecordDeleted : DeleteResult()
        object AllAdditionalDeleted  : DeleteResult()
        data class AdditionalStartAddressMoved(val newAddress:Int) : DeleteResult()
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
                totalNumberOfBlocks--
            }
            else
                return
        }
    }

    fun shake(additionalBlockAddress: Int) {
        val partlyEmptyBlocks = emptyMutableList<Block<T>>()
        val readBlocks        = emptyMutableList<Block<T>>()
        var block             = getBlock(additionalBlockAddress)
        while(block.hasAdditionalBlock()){
            readBlocks.add(block)
            if(block.isNotFull())
                partlyEmptyBlocks.add(block)
            block = getBlock(block.additionalBlockAddress)
        }

        val test = getLastBlock(additionalBlockAddress)
        val bl = block
        println("wiii")
    }

}
