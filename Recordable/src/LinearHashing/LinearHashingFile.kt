@file:Suppress("MemberVisibilityCanPrivate")

package LinearHashing

import AbstractData.*
import AbstractData.BlockState.Full
import AbstractData.BlockState.NotFull
import HeapFile.HeapFile
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
        suffix: String = "bin") {
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

    fun deleteFiles() =
        File(this.pathToFile).delete() &&
        File(this.pathToAdditionalFile).delete()

    fun ReadWrite.allocate(numberOfBlocks: Int, startAddressInFile: Int = size().toInt()) {
        var startAddress = startAddressInFile
        for (i in 0 until numberOfBlocks){
            val emptyBlock = LinearHashFileBlock(numberOfRecordsInBlock, instanceOfType,startAddress)
            writeFrom(startAddress,emptyBlock.toByteArray())
            startAddress+=emptyBlock.byteSize
        }
    }

    //currentLevel + actualRecordsCount + actualSplitAddress + additionalRecordsCount = SizeOfInt * 4
    private val firstBlockAddress      =0// SizeOfInt * 5
    private val file: ReadWrite
    private var block: LinearHashFileBlock<T>
    val blockByteSize: Int
    private var currentLevel           = 0
    private var actualRecordsCount     = 0
    private val additionalRecordsCount get() = additionalFile.totalNumberOfRecords
    private var actualSplitAddress: Int
    private var actualBlockCount :Int

    fun add(record: T): Boolean {
        println("=== === ===")
        println(record)
        println(record.address)

        val block = getBlock(record)
        if(block.contains(record)) return false
        val result =  when (block.state()) {
            Full    -> block.addToAdditionalFile(record)
            NotFull -> addToNotFullBlock(block, record)
        }
        println("#blocks $numberOfBlocks, additional count $additionalRecordsCount, #record $actualRecordsCount")
        return result
    }

    private fun Block<T>.addToAdditionalFile(record: T): Boolean {
        val address = additionalFile.add(this, record)

        if(address != additionalBlockAddress){
            val t = (this as LinearHashFileBlock<T>).copy()
            t.additionalBlockAddress = address
            write(t)
            if (shouldSplit) {
                println("i should split while inseritng $record")
            }
        }
        return true
    }

    private fun addToNotFullBlock(block: Block<T>, record: T) : Boolean {
        if(block.isFull()) throw IllegalArgumentException("Block is full")

        block.add(record)
        actualRecordsCount++
        if (shouldSplit) {
            println("should split cause densiity is $currentDensity")
            split(record)
            actualSplitAddress += block.byteSize

            if (actualSplitAddress / block.byteSize >= getFirstHashModulo()) {
                actualSplitAddress = 0
                currentLevel++
            }
            return true
        }
        write(block)
        return true
    }

    private val shouldSplit get() = currentDensity > maxDensity

    private fun split(recordToAdd: T) {
        val addressOfNewBlockInFile = actualSplitAddress + getFirstHashModulo() * blockByteSize
        file.allocate(numberOfBlocks = 1, startAddressInFile = addressOfNewBlockInFile)

        val newBlock   = getBlock(addressOfNewBlockInFile / blockByteSize)
        val splitBlock = getBlock(actualSplitAddress/blockByteSize).apply { add(recordToAdd) }
        val additionalBlocks  =  splitBlock.getAdditionalBlocks()
        val additionalRecords =  additionalBlocks.flatten().reversed()
        println(additionalBlocks )
        val recordsToMove =  (additionalRecords + splitBlock.data)
            .filter { it.isValid() }
            .filter { it.hash.getSecondHash() != actualSplitAddress/blockByteSize }
        val recordsThatStayed = ((additionalRecords + splitBlock.data) - recordsToMove).asReversed()

        recordsToMove.forEach {
            newBlock.add(it)
        }
        val block = LinearHashFileBlock(blockSize= blockByteSize,ofType = instanceOfType,data = recordsThatStayed)
            .apply { addressInFile = splitBlock.addressInFile }
        write(newBlock)

        recordsToMove.forEach {
            it.invalidate()
        }
        //so valid values are first :-)
        splitBlock.data.sortBy { it.validity }
        actualBlockCount++
        write(block)
    }

    fun get(record :T): T? = getBlock(record).get(record)

    private fun getFirstHashModulo()  = (numberOfBlocks * pow(2.toDouble(), currentLevel    .toDouble())).toInt()
    private fun getSecondHashModulo() = (numberOfBlocks * pow(2.toDouble(), currentLevel + 1.toDouble())).toInt()
    private fun Int.getFirstHash()    = this % getFirstHashModulo()  //* numberOfRecordsInBlock + firstBlockAddress
    private fun Int.getSecondHash()   = this % getSecondHashModulo() //* numberOfRecordsInBlock + firstBlockAddress
    internal val currentDensity get() = ((actualRecordsCount + additionalRecordsCount).toDouble()) / (actualBlockCount * numberOfRecordsInBlock + additionalRecordsCount).toDouble()

    private val T.address get() = hash.address()

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


    fun getBlock(address: Int): Block<T> = block.fromByteArray(file.read(blockByteSize, address * blockByteSize))

    private fun getBlock(record: Record<T>) = getBlock(record.hash.address())

    fun write(block: Block<T>) =  file.writeFrom(block.addressInFile, block.toByteArray())

    private fun Block<T>.getAdditionalBlocks() : List<List<T>> = additionalFile.getAdditionalBlocks(this.additionalBlockAddress)


}


