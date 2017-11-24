package LinearHashing

import AbstractData.*
import AbstractData.SizeConst.*
import record.emptyMutableList
import src.ReadWrite
import java.lang.Math.pow

class LinearHashingFile<T : Record<T>>(
    val pathToFile: String,
    val pathToAdditionalFile : String = "",
    val pathToAdditionalPropertiesFile : String = "",
    val ofType: T,
    val numberOfRecordsInBlock: Int,
    val blockCount : Int = 2,
    val additioanlBlockSize: Int = 0,
    val minDensity : Float = 0f,
    val maxDensity : Float = 0f
) {
    //currentLevel + actualRecordsCount + actualSplitAddress + additionalRecordsCount = SizeOfInt * 4
    private val firstBlockAddress      = 0 //SizeOfInt * 4
    private val file                   = ReadWrite(pathToFile)
    private var block                  = LinearHashFileBlock(numberOfRecordsInBlock, ofType)
    private val blockByteSize          = block.byteSize
    private var currentLevel           = 0
    private var actualRecordsCount     = 0
    private var additionalRecordsCount = 0
    private var actualSplitAddress     = firstBlockAddress


    internal fun save() : Boolean {
        val blockBytes = block.toByteArray()
        return file.write(blockBytes,blockByteSize,block.addressInFile)
    }



    fun add(record: T): Boolean {
        println(""""
          record.address   ${record.address}
          record.hash.firstHash   ${record.hash.firstHash}
          record.hash.secondHash   ${record.hash.secondHash}
        """.trimMargin())
        println("-------")
        return true
    }

    private val firstHashModulo  get() = (blockCount * pow(2.toDouble(), currentLevel    .toDouble())).toInt()
    private val secondHashModulo get() = (blockCount * pow(2.toDouble(), currentLevel + 1.toDouble())).toInt()
    private val Int.firstHash    get() = this % firstHashModulo //* numberOfRecordsInBlock + firstBlockAddress
    private val Int.secondHash   get() = this % secondHashModulo//* numberOfRecordsInBlock + firstBlockAddress
    private val currentDensity   get() = actualRecordsCount + additionalRecordsCount / (
        (actualSplitAddress - firstBlockAddress) / ( numberOfRecordsInBlock + firstHashModulo) * numberOfRecordsInBlock + additionalRecordsCount  ) .toFloat() // god help me that this is correct  

    private val T.address get() = hash.address()
    private fun Int.address() : Int {
        val first = firstHash
        if (first < actualSplitAddress)
             return secondHash
        else return firstHash
    }


    internal fun allBlocksInFile(): List<Block<T>> {
        save()
        val blocksInFile = file.size() / blockByteSize
        val blocks = emptyMutableList<Block<T>>()
        (0 until blocksInFile)
            .map { block.fromByteArray(file.read(blockByteSize, (it * blockByteSize).toInt())) }
            .forEach { blocks += it }
        return blocks
    }
}