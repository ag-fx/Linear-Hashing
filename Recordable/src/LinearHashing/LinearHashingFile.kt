package LinearHashing

import AbstractData.*
import AbstractData.SizeConst.*
import record.emptyMutableList
import src.ReadWrite
import java.lang.Math.pow

class LinearHashingFile<T : Record<T>>(
    val pathToFile: String,
    val pathToAdditionalFile : String,
    val pathToAdditionalPropertiesFile : String,
    val type: T ,
    val blockSize: Int,
    val blockCount : Int,
    val additioanlBlockSize: Int,
    val minDensity : Float,
    val maxDensity : Float
) {
    //currentLevel + actualRecordsCount + actualSplitAddress + additionalRecordsCount = SizeOfInt * 4
    private val firstBlockAddress      = SizeOfInt * 4
    private val invalidAddress         = Integer.MIN_VALUE
    private val file                   = ReadWrite(pathToFile)
    private var block                  = LinearHashFileBlock(blockSize, type)
    private val blockByteSize          = block.byteSize
    private var currentLevel           = 0
    private var actualRecordsCount     = 0
    private var additionalRecordsCount = 0
    private var actualSplitAddress     = firstBlockAddress

    fun sequentialPrint(){
        val blocksInFile = file.size() / blockByteSize
        val blocks = emptyMutableList<Block<T>>()
        (0 until blocksInFile)
            .map { block.fromByteArray(file.read(blockByteSize, (it * blockByteSize).toInt())) }
            .forEach { blocks += it }
        println(blocks)
    }

    private fun save() {
        val blockBytes = block.toByteArray()
        file.write(blockBytes,blockByteSize,block.addressInFile)
    }


    fun add(record: T): Boolean {
        val success = block.add(record)
        if (success) return success
        else {
            block = LinearHashFileBlock(blockSize, type, block.addressInFile + blockByteSize)
            block.add(record)

        }
        save()
        return true
    }

    private fun firstHashModulo ()  = (blockCount * pow(2.toDouble(), currentLevel  .toDouble())).toInt()
    private fun secondHashModulo()  = (blockCount * pow(2.toDouble(), currentLevel+1.toDouble())).toInt()
}