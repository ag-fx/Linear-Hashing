@file:Suppress("MemberVisibilityCanPrivate")

package LinearHashing

import AbstractData.*
import record.emptyMutableList
import src.ReadWrite
import java.lang.Math.pow

class LinearHashingFile<T : Record<T>> {
    val pathToFile: String
    val pathToAdditionalFile: String
    val pathToAdditionalPropertiesFile: String
    val instanceOfType: T
    val numberOfRecordsInBlock: Int
    var blockCount: Int
    val additionalBlockSize: Int
    val minDensity: Float
    val maxDensity: Float

    constructor(
        pathToFile: String,
        pathToAdditionalFile: String = "",
        pathToAdditionalPropertiesFile: String = "",
        instanceOfType: T,
        numberOfRecordsInBlock: Int,
        blockCount: Int = 4,
        additioanlBlockSize: Int = 0,
        minDensity: Float = 0f,
        maxDensity: Float = 0f) {
            this.pathToFile = pathToFile
            this.pathToAdditionalFile = pathToAdditionalFile
            this.pathToAdditionalPropertiesFile = pathToAdditionalPropertiesFile
            this.instanceOfType = instanceOfType
            this.numberOfRecordsInBlock = numberOfRecordsInBlock
            this.blockCount = blockCount
            this.additionalBlockSize = additioanlBlockSize
            this.minDensity = minDensity
            this.maxDensity = maxDensity
            this.file = ReadWrite(pathToFile)
            this.block = LinearHashFileBlock(numberOfRecordsInBlock, instanceOfType)
            this.blockByteSize = block.byteSize
            this.actualSplitAddress = firstBlockAddress
            file.allocate(blockCount,firstBlockAddress)
    }
    fun ReadWrite.allocate(numberOfBlocks: Int, startAddressInFile: Int = size().toInt()) {
        var startAddress = startAddressInFile
        for (i in 0 until numberOfBlocks){
            val emptyBlock = LinearHashFileBlock(numberOfRecordsInBlock, instanceOfType,startAddress)
            writeFrom(startAddress,emptyBlock.toByteArray())
            startAddress+=emptyBlock.byteSize
        }
    }
    //currentLevel + actualRecordsCount + actualSplitAddress + additionalRecordsCount = SizeOfInt * 4
    private val firstBlockAddress      = 0 //SizeOfInt * 4
    private val file: ReadWrite
    private var block: LinearHashFileBlock<T>
     val blockByteSize: Int
    private var currentLevel           = 0
    private var actualRecordsCount     = 0
    private var additionalRecordsCount = 0
    private var actualSplitAddress: Int

    fun add(record: T): Boolean {
        val block = getBlock(record)
        write(record)
        actualRecordsCount++
        if(currentDensity > maxDensity) {
            split()
            actualSplitAddress+=block.byteSize
            if( actualSplitAddress >= getSecondHashModulo()){
                actualSplitAddress=0
                currentLevel++
            }

        }
        return true
    }

    private fun split(){
        val addressOfNewBlockInFile = actualSplitAddress + getFirstHashModulo() * blockByteSize
        file.allocate(numberOfBlocks = 1, startAddressInFile = addressOfNewBlockInFile)
        val newBlock   = getBlock(addressOfNewBlockInFile/blockByteSize)
        val splitBlock = getBlock(actualSplitAddress)

        val recordsToMove = splitBlock.data
            .filter { it.isValid() }
            .filter { it.hash.getSecondHash() != actualSplitAddress }

        recordsToMove.forEach {
            newBlock.add(it)
        }
        write(newBlock)

        recordsToMove.forEach {
            it.invalidate()
        }
        //so valid values are first:)
        splitBlock.data.sortBy { it.validity }
        write(splitBlock)
    }

    fun get(record :T): T? = getBlock(record).get(record)

    private fun getFirstHashModulo()  = (blockCount * pow(2.toDouble(), currentLevel    .toDouble())).toInt()
    private fun getSecondHashModulo() = (blockCount * pow(2.toDouble(), currentLevel + 1.toDouble())).toInt()
    private fun Int.getFirstHash()    = this % getFirstHashModulo() //* numberOfRecordsInBlock + firstBlockAddress
    private fun Int.getSecondHash()   = this % getSecondHashModulo()//* numberOfRecordsInBlock + firstBlockAddress
    private val currentDensity get() = ((actualRecordsCount + additionalRecordsCount).toDouble()) / (blockCount * numberOfRecordsInBlock).toDouble()

    private val T.address get() = hash.address()
    private fun Int.address() : Int {
        val first = getFirstHash()
        if (first < actualSplitAddress)
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

    fun write(block: Block<T>) {
        block.toByteArray()
        file.writeFrom(block.addressInFile, block.toByteArray())
    }

    fun write(record: T) = getBlock(record.address).apply { add(record) }.also(this::write)



}