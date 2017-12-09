@file:Suppress("MemberVisibilityCanPrivate")

package LinearHashing

import AbstractData.*
import AbstractData.BlockState.Full
import AbstractData.BlockState.NotFull
import HeapFile.HeapFile
import HeapFile.HeapFile.AddResult
import record.emptyMutableList
import src.ReadWrite
import java.io.File
import java.lang.Math.pow

class LinearHashingFile<T : Record<T>> {
    val pathToFile: String
    val pathToAdditionalFile: String
    val instanceOfType: T
    val numberOfRecordsInBlock: Int
    val numberOfBlocks: Int
    val minDensity: Double
    val maxDensity: Double
    internal val additionalFile : HeapFile<T>

    constructor(
        pathToFile: String,
        instanceOfType: T,
        numberOfRecordsInBlock: Int = 3,
        numberOfRecordsInAdditionalBlock: Int = 2,
        blockCount: Int = 4,
        minDensity: Double = 0.0,
        maxDensity: Double = 0.75,
        suffix: String = "uds") {
            this.pathToFile             = "$pathToFile.$suffix"
            this.pathToAdditionalFile   = "${pathToFile}_additionalFile.$suffix"
            this.instanceOfType         = instanceOfType
            this.numberOfRecordsInBlock = numberOfRecordsInBlock
            this.numberOfBlocks         = blockCount // this is the constant M value in pdf from Jankovic.
            this.actualBlockCount       = blockCount // current number of records in hash file
            this.minDensity             = minDensity
            this.maxDensity             = maxDensity
            this.file                   = ReadWrite(this.pathToFile)
            this.block                  = LinearHashFileBlock(numberOfRecordsInBlock, instanceOfType)
            this.blockByteSize          = block.byteSize
            this.actualSplitAddress     = firstBlockAddress
            this.additionalFile         = HeapFile(pathToAdditionalFile,instanceOfType,numberOfRecordsInAdditionalBlock)
            file.allocate(blockCount,firstBlockAddress)
    }

    internal fun deleteFiles() =
        File(this.pathToFile)          .delete() &&
        File(this.pathToAdditionalFile).delete()

    private fun ReadWrite.allocate(numberOfBlocks: Int, startAddressInFile: Int = size().toInt()) {
        var startAddress = startAddressInFile
        for (i in 0 until numberOfBlocks){
            val emptyBlock = LinearHashFileBlock(numberOfRecordsInBlock, instanceOfType,startAddress)
            writeFrom(startAddress,emptyBlock.toByteArray())
            startAddress+=emptyBlock.byteSize
        }
    }

    //currentLevel + actualRecordsCount + actualSplitAddress + additionalRecordsCount = SizeOfInt * 4
    private val firstBlockAddress  = 0// SizeOfInt * 5
    private val file:  ReadWrite
    private var block: LinearHashFileBlock<T>
    private var currentLevel       = 0
    private var actualRecordsCount = 0
    private val additionalRecordsCount
        get() = additionalFile.totalNumberOfRecords
    private var actualSplitAddress  : Int
    private var actualBlockCount    : Int
    val blockByteSize               : Int

    fun add(record: T): Boolean {
        val block = getBlock(record)
        if(block.contains(record)) return false
        val result =  when (block.state()) {
            Full    -> block.addToAdditionalFile(record)
            NotFull -> addToNotFullBlock(block, record)
        }
        return result
    }

    fun get(record: T): T? {
        val blockOfRecord = getBlock(record)
        when{
            blockOfRecord.contains(record)        -> return blockOfRecord.get(record)
            blockOfRecord.hasNotAdditionalBlock() -> return null
            else                                  -> return blockOfRecord.getRecordFromAdditional(record)
        }
    }

    fun delete(record: T): Boolean {
        val block = getBlock(record)
        when{
            block.contains(record)          -> return block.delete(record).also{ write(block) }
            block.hasNotAdditionalBlock()   -> return false
            else                            -> return block.deleteFromAdditional(record)
        }
    }

    private fun Block<T>.deleteFromAdditional(record: T): Boolean {
        val address = additionalFile.delete(additionalBlockAddress,record)
        return true
    }

    private fun Block<T>.addToAdditionalFile(record: T): Boolean {
        val addResult = additionalFile.add(this, record)
        val thisBlock = (this as LinearHashFileBlock<T>)

        when(addResult){
            is AddResult.RecordAddedToExistingBlock -> {
                thisBlock.additionalRecordCount++
            }
            is AddResult.RecordAddedToNewBlock      -> {
                thisBlock.additionalRecordCount++
                thisBlock.additionalBlockCount++
            }
            is AddResult.RecordWasNotAdded          -> {
                return false
            }
            is AddResult.FirstAdditionalBlock       -> {
                thisBlock.additionalRecordCount++
                thisBlock.additionalBlockCount++
                this.additionalBlockAddress = addResult.newBlockAddress

            }
        }
        write(thisBlock)
        splitCheck()
        return true
    }

    private fun addToNotFullBlock(block: Block<T>, record: T) : Boolean {
        if(block.isFull()) throw IllegalArgumentException("Block is full")

        block.add(record)
        write(block)
        actualRecordsCount++
        splitCheck()
        return true
    }

    //I'm reversing lists in the function so records are in the same order as they are in the papers from Mr.Jankovic
    private fun split() {
        val addressOfNewBlockInFile = actualSplitAddress + getFirstHashModulo() * blockByteSize
        file.allocate(numberOfBlocks = 1, startAddressInFile = addressOfNewBlockInFile)
        val newBlock          = getBlock(addressOfNewBlockInFile / blockByteSize) as LinearHashFileBlock
        val splitBlock        = getBlock(actualSplitAddress / blockByteSize)      as LinearHashFileBlock
        val additionalBlocks  = splitBlock.getAdditionalBlocks(true)
        val additionalRecords = additionalBlocks.flatten().reversed()

        val recordsToMove = (additionalRecords + splitBlock.data)
            .filter (Serializable<T>::isValid)
            .filter { it.hash.getSecondHash() != actualSplitAddress / blockByteSize }
            .reversed()

        val recordsThatStayed = ((additionalRecords + splitBlock.data) - recordsToMove).filter { it.isValid() }.asReversed()

        recordsToMove.forEach {
            newBlock.add(it)
            if(additionalRecords.contains(it)) actualRecordsCount++
        }

        val recordsToAddToAdditionalForNewBlock = (recordsToMove - newBlock.data).filter { it.isValid() }
        recordsToAddToAdditionalForNewBlock.forEach {
            val addResult = additionalFile.add(newBlock, it)
            when (addResult) {
                is AddResult.RecordAddedToExistingBlock -> {
                    newBlock.additionalRecordCount++
                }
                is AddResult.RecordAddedToNewBlock -> {
                    newBlock.additionalBlockCount ++
                    newBlock.additionalRecordCount++
                }
                is AddResult.FirstAdditionalBlock -> {
                    newBlock.additionalBlockAddress = addResult.newBlockAddress
                    newBlock.additionalBlockCount ++
                    newBlock.additionalRecordCount++
                }
                is AddResult.RecordWasNotAdded -> {

                }
            }

        }

        val block = LinearHashFileBlock(blockSize = numberOfRecordsInBlock, ofType = instanceOfType, addressInFile = splitBlock.addressInFile)
        recordsThatStayed.forEach {
            block.add(it)
        }

        val recordsToAddToAdditionalFile = recordsThatStayed - block.data
        recordsToAddToAdditionalFile.forEach {
            val addResult = additionalFile.add(block,it)
            when(addResult){
                is AddResult.RecordAddedToExistingBlock -> {
                    block.additionalRecordCount++
                }
                is AddResult.RecordAddedToNewBlock      -> {
                    block.additionalRecordCount++
                    block.additionalBlockCount++
                }
                is AddResult.FirstAdditionalBlock       -> {
                    block.additionalBlockAddress = addResult.newBlockAddress
                    block.additionalRecordCount++
                    block.additionalBlockCount++
                }
                is AddResult.RecordWasNotAdded          -> {

                }
            }
        }
        write(newBlock)
        actualBlockCount++

        write(block)
        actualSplitAddress += block.byteSize
    }

    private fun splitCheck() {
        while (shouldSplit) {
            split()
            if (actualSplitAddress / block.byteSize >= getFirstHashModulo()) {
                actualSplitAddress = 0
                currentLevel++
                split()
            }
        }
    }

    private val shouldSplit get() = currentDensity > maxDensity

    private fun getFirstHashModulo()  = (numberOfBlocks * pow(2.toDouble(), currentLevel    .toDouble())).toInt()

    private fun getSecondHashModulo() = (numberOfBlocks * pow(2.toDouble(), currentLevel + 1.toDouble())).toInt()

    private fun Int.getFirstHash()    = this % getFirstHashModulo()  //* numberOfRecordsInBlock + firstBlockAddress

    private fun Int.getSecondHash()   = this % getSecondHashModulo() //* numberOfRecordsInBlock + firstBlockAddress

    internal val currentDensity get() = ((actualRecordsCount + additionalRecordsCount).toDouble()) / (actualBlockCount * numberOfRecordsInBlock + additionalRecordsCount).toDouble()

    private fun Int.address() : Int {
        val first = getFirstHash()
        if (first < actualSplitAddress / blockByteSize)
             return getSecondHash()

        else return getFirstHash()
    }

    internal fun allRecordsInFile() = allBlocksInFile().flatten()

    internal fun allBlocksInFile() : List<List<T>>{
        val blocksInFile = file.size() / blockByteSize
        val blocks = emptyMutableList<Block<T>>()
        (0 until blocksInFile)
            .map { block.fromByteArray(file.read(blockByteSize, (it * blockByteSize).toInt())) }
            .forEach { blocks += it }
        return blocks.map { it.data }
    }

    private fun getBlock(address: Int) = block.fromByteArray(file.read(blockByteSize, address * blockByteSize))

    private fun getBlock(record: Record<T>) = getBlock(record.hash.address())

    private fun write(block: Block<T>) = file.writeFrom(block.addressInFile, block.toByteArray())

    private fun Block<T>.getAdditionalBlocks(invalidateThem: Boolean = false) = additionalFile.getAdditionalBlocks(this.additionalBlockAddress, invalidateThem)

    private fun Block<T>.getRecordFromAdditional(record: T)                   = additionalFile.getRecord(additionalBlockAddress, record)

}

