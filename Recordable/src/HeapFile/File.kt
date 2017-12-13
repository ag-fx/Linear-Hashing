package HeapFile

import AbstractData.*
import LinearHashing.*
import com.google.gson.Gson
import record.emptyMutableList
import src.ReadWrite
import java.io.File

class HeapFile<T : Record<T>> {

    private val blockSize                : Int
        get () = instanceOfBlock.byteSize
    private val heapFile                 : ReadWrite
    private val instanceOfRecord         : T
    private val instanceOfBlock          : HeapFileBlock<T>
    private val path                     : String
    private val numberOfRecordsInBlock   : Int
     var totalNumberOfRecords     : Int
     var totalNumberOfBlocks      : Int
    private val emptyBlockAddresses = emptyMutableList<Int>()

    constructor(path: String, instanceOfRecord: T, numberOfRecordsInBlock: Int) {
        this.heapFile         = ReadWrite(path)
        this.path             = path
        this.instanceOfRecord = instanceOfRecord
        this.numberOfRecordsInBlock = numberOfRecordsInBlock
        this.instanceOfBlock  = HeapFileBlock(numberOfRecordsInBlock, instanceOfRecord)
        val init = readState()
        totalNumberOfRecords  = init?.totalNumberOfRecords ?: 0
        totalNumberOfBlocks   = init?.totalNumberOfBlocks ?: 0
        emptyBlockAddresses.addAll(init?.emptyBlockAddresses?: emptyList())
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
        if (linearHashBlock.hasAdditionalBlock()) {
            val additionalBlockNotFound = true
            var additionalBlock = getBlock(linearHashBlock.additionalBlockAddress)
            insertCount.totalDiskAccess++

            while(additionalBlockNotFound){

                if (additionalBlock.contains(record)) {
                    return AddResult.RecordWasNotAdded
                }

                if (additionalBlock.isFull()) {
                    if (additionalBlock.hasAdditionalBlock()) {
                        additionalBlock = getBlock(additionalBlock.additionalBlockAddress)
                        insertCount.totalDiskAccess++
                    } else {
                        additionalBlock.additionalBlockAddress = add(record)
                        additionalBlock.writeToFile()
                        insertCount.totalDiskAccess++
                        return AddResult.RecordAddedToNewBlock(additionalBlock.additionalBlockAddress)
                    }
                } else {
                    val success = additionalBlock.add(record)
                    if (success) {
                        totalNumberOfRecords++
                        additionalBlock.writeToFile()
                        insertCount.totalDiskAccess++
                        return AddResult.RecordAddedToExistingBlock
                    } else {
                        return AddResult.RecordWasNotAdded
                    }

                }

            }
        } else {
            val emptyAddress = getAddress()
            val heapBlock    = getBlock(emptyAddress)
            insertCount.totalDiskAccess++
            var success      = heapBlock.add(record)
            if(success)
                totalNumberOfRecords++
            else
                return AddResult.RecordWasNotAdded

            emptyBlockAddresses.remove(heapBlock.addressInFile)
            val addressInHeapFile = heapBlock.addressInFile
            heapBlock.writeToFile()
            insertCount.totalDiskAccess++

            return AddResult.FirstAdditionalBlock(newBlockAddress = addressInHeapFile)
        }
        throw IllegalStateException("This should not end up here")
    }

    sealed class AddResult{
        object RecordAddedToExistingBlock                           : AddResult()
        data class RecordAddedToNewBlock(val newBlockAddress:Int)   : AddResult()
        data class FirstAdditionalBlock (val newBlockAddress:Int)   : AddResult()
        object RecordWasNotAdded                                    : AddResult()
        object RecordWasUpdated                                     : AddResult()
    }

    private fun getAddress(): Int {
        return if (emptyBlockAddresses.isNotEmpty())
            emptyBlockAddresses.removeFirst()
        else {
            val lastPositionInFile = heapFile.size().toInt()
            val newBlock = instanceOfBlock.copy()
            newBlock.addressInFile = lastPositionInFile
            heapFile.writeFrom(position = lastPositionInFile, byteArray = newBlock.toByteArray())
            emptyBlockAddresses.add(lastPositionInFile)
            totalNumberOfBlocks++
            lastPositionInFile
        }
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
                    //tryTrim()
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
            findCount.totalDiskAccess++

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

    fun updateRecord(additionalBlockAddress: Int, record: T): Boolean {
        var additionalBlockAddress = additionalBlockAddress
        val lastNotFound = true
        while(lastNotFound){
            val block = getBlock(additionalBlockAddress)
            if(block.contains(record)){
                block.update(record)
                block.writeToFile()
                return true
            }
            if(block.hasAdditionalBlock()){
                 additionalBlockAddress = block.additionalBlockAddress
            }else
                return true
        }
        return false
    }

    fun delete(additionalBlockAddress: Int, record: T): DeleteResult {
        var additionalBlockAddress = additionalBlockAddress
        val readBlocks = emptyMutableList<Block<T>>()
        val lastNotFound = true
        while (lastNotFound) {
            val block = getBlock(additionalBlockAddress)
            deleteCount.totalDiskAccess++

            readBlocks.add(block)
            if(block.contains(record)){
                block.delete(record)
                totalNumberOfRecords--
                block.writeToFile()
                deleteCount.totalDiskAccess++

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
                        blockThatPointsToTheLast.additionalBlockAddress = block.additionalBlockAddress //NoAdditionalBlockAddress
                        blockThatPointsToTheLast.writeToFile()
                        deleteCount.totalDiskAccess++

                        emptyBlockAddresses.add(block.addressInFile)
                        totalNumberOfBlocks--
                        return DeleteResult.BlockAndRecordDeleted
                    }
                }
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


    fun tryTrim() {
        while(heapFile.size() > 0) {
            val lastBlock = getBlock((heapFile.size() - instanceOfBlock.byteSize).toInt())
            insertCount.totalDiskAccess++

            if(lastBlock.isEmpty()){
                heapFile.shrink(instanceOfBlock.byteSize)
                insertCount.totalDiskAccess++

                emptyBlockAddresses.remove(lastBlock.addressInFile)
                totalNumberOfBlocks--
            }
            else
                return
        }
    }

    fun close() {
        heapFile.close()
        val state = HeapFileInfo(
            totalNumberOfRecords = this.totalNumberOfRecords,
            totalNumberOfBlocks  = this.totalNumberOfBlocks,
            emptyBlockAddresses  = this.emptyBlockAddresses
            )
        val stateJson = Gson().toJson(state)
        File("info_$path").writeText(stateJson)
    }

    private fun readState(): HeapFileInfo? {
        val file = File("info_$path")
        if(!file.exists()) return null

        val json = File("info_$path").readText()
        val gson = Gson()
        return gson.fromJson(json,HeapFileInfo::class.java)
    }

    fun addWholeBlock(block: Block<T>, records: List<T>): AddBlockResult {
        val blockToAdd = HeapFileBlock(numberOfRecordsInBlock,instanceOfRecord)
        records.forEach{
            blockToAdd.add(it)
        }

        if (block.hasNotAdditionalBlock()) {
            blockToAdd.addressInFile = getAddress()
            blockToAdd.writeToFile()
            insertCount.operationCount++

            totalNumberOfRecords+=records.size
            return AddBlockResult.AddedToTheStart(blockToAdd.addressInFile, records.size)
        } else {
            var nextAddress = block.additionalBlockAddress
            while (true) {
                val nextBlock = getBlock(nextAddress)
                insertCount.operationCount++

                if (nextBlock.hasAdditionalBlock()) {
                    nextAddress = nextBlock.additionalBlockAddress
                } else {
                    blockToAdd.addressInFile = getAddress()
                    nextBlock.additionalBlockAddress= blockToAdd.addressInFile
                    blockToAdd.writeToFile()
                    nextBlock.writeToFile()
                    insertCount.operationCount++
                    insertCount.operationCount++

                    totalNumberOfRecords+=records.size
                    return AddBlockResult.Added(records.size)
                }
            }
        }

    }

    sealed class AddBlockResult{
        data class AddedToTheStart(val address:Int, val numberOfRecords: Int) : AddBlockResult()
        data class Added(val numberOfRecords: Int) : AddBlockResult()
    }

}
data class HeapFileInfo(
    val totalNumberOfRecords : Int,
    val totalNumberOfBlocks : Int,
    val emptyBlockAddresses:List<Int>
)
