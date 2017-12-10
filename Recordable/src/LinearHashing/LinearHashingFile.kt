@file:Suppress("MemberVisibilityCanPrivate")

package LinearHashing

import AbstractData.*
import AbstractData.BlockState.Full
import AbstractData.BlockState.NotFull
import HeapFile.HeapFile
import HeapFile.HeapFile.*
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
        numberOfRecordsInBlock: Int,
        numberOfRecordsInAdditionalBlock: Int ,
        blockCount: Int ,
        minDensity: Double ,
        maxDensity: Double ,
        suffix: String = "uds") {
            if(true){
                File("$pathToFile.$suffix").delete()
                File("${pathToFile}_additionalFile.$suffix").delete()
            }
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
            this.numberOfRecordsInAdditionalBlock = numberOfRecordsInAdditionalBlock
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
    val numberOfRecordsInAdditionalBlock : Int
    private val firstBlockAddress  = 0// SizeOfInt * 5
    private val file:  ReadWrite
    private var block: LinearHashFileBlock<T>
    internal var currentLevel       = 0
    internal var actualRecordsCount = 0
    internal val additionalRecordsCount
        get() = additionalFile.totalNumberOfRecords
    internal val additionalBlockCount
        get() = additionalFile.totalNumberOfBlocks
    internal var actualSplitAddress  : Int
    internal var actualBlockCount    : Int
    val blockByteSize                : Int

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
        when {
            block.contains(record) -> {
                if (block.delete(record)) {
                    write(block)
                    actualRecordsCount--
                    merge()
                    return true
                } else return false
            }
            block.hasNotAdditionalBlock() -> return false
            else ->  {
                if(block.deleteFromAdditional(record)) {
                    merge()
                    return true
                }
                 else return false
            }
        }
    }

    private val shouldMerge get () = minDensity > currentDensity.roundDown2()

    private fun merge(){
        val s = shouldMerge
        while(shouldMerge) {
            //posledna skupina a= S + M*2^level-1
            val moveFromAddress =((actualSplitAddress + getFirstHashModulo() * blockByteSize - blockByteSize)/blockByteSize)
            val moveFrom = getBlock(moveFromAddress)
            val moveFromRecords = moveFrom.getAllRecords(true).filter { it.isValid() }
            actualRecordsCount -= moveFrom.data.filter { it.isValid() }.size
            if(actualSplitAddress > 0){
                //b = S - 1
                val moveToAddress =  (actualSplitAddress - blockByteSize)/blockByteSize
                val moveTo = getBlock(moveToAddress)

                moveFromRecords.forEach {
                    if(moveTo.add(it))
                        actualRecordsCount++
                }
                write(moveTo)
                val moveToAdditional = moveFromRecords - moveTo.data
                moveToAdditional.forEach {
                    moveTo.addToAdditionalFile(it)
                }
                actualSplitAddress = moveToAddress * blockByteSize
                file.shrink(blockByteSize)
                actualBlockCount--


            } else if(actualSplitAddress==0 && currentLevel > 0){
                // b = M * 2^(level -1) - 1
                val moveToAddress = (((numberOfBlocks * pow(2.0,currentLevel - 1.0) ) * blockByteSize - blockByteSize)/blockByteSize).toInt()
                val moveTo = getBlock(moveToAddress)
                moveFromRecords.forEach {
                   if(moveTo.add(it))
                       actualRecordsCount++
                }
                write(moveTo)
                val moveToAdditional = moveFromRecords - moveTo.data
                moveToAdditional.forEach {
                    moveTo.addToAdditionalFile(it)
                }
                actualSplitAddress = moveToAddress * blockByteSize
                currentLevel--

                file.shrink(blockByteSize)
                actualBlockCount--

            }else
                return
        }


    }



    private fun Block<T>.deleteFromAdditional(record: T): Boolean {
        val editBlock = (this as LinearHashFileBlock).copy()
        with(editBlock){
               val a = additionalBlockCount
               val b = additionalRecordCount
               val c = this@LinearHashingFile.numberOfRecordsInAdditionalBlock
               val success = additionalFile.delete(additionalBlockAddress,record)
               when(success){
                   DeleteResult.Deleted     -> additionalRecordCount--
                   DeleteResult.NotDeleted  -> return false
                   DeleteResult.BlockAndRecordDeleted -> {
                       additionalRecordCount--
                       additionalBlockCount --
                   }
                   DeleteResult.AllAdditionalDeleted -> {
                       additionalRecordCount  = 0
                       additionalBlockCount   = 0
                       additionalBlockAddress = NoAdditionalBlockAddress
                   }
                   is DeleteResult.AdditionalStartAddressMoved ->{
                       additionalRecordCount  = 0
                       additionalBlockCount   = 0
                       additionalBlockAddress = success.newAddress
                   }
               }
               write(this@with)

               if(willSaveBlock()){
                   println()
                  // additionalFile.shake(additionalBlockAddress)
               }
           }
//        val address = additionalFile.delete(additionalBlockAddress,record)
        return true
    }

    private fun LinearHashFileBlock<T>.willSaveBlock() = this@willSaveBlock.additionalBlockCount * this@LinearHashingFile.numberOfRecordsInAdditionalBlock - (this@willSaveBlock.additionalRecordCount) >= this@LinearHashingFile.numberOfRecordsInAdditionalBlock

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
        val splitBlock        = getBlock(actualSplitAddress      / blockByteSize) as LinearHashFileBlock
        val additionalBlocks  = splitBlock.getAdditionalBlocks( invalidateThem = true)
        val additionalRecords = additionalBlocks.flatten().reversed()

        val recordsToMove = (additionalRecords + splitBlock.data)
            .filter (Serializable<T>::isValid)
            .filter { it.hash.getSecondHash() != actualSplitAddress / blockByteSize }
            .reversed()

        val recordsThatStayed = ((additionalRecords + splitBlock.data) - recordsToMove).filter { it.isValid() }.asReversed()

        actualRecordsCount -= splitBlock.data.filter { it.isValid() }.size

        recordsToMove.forEach {
            if(newBlock.add(it))
                actualRecordsCount++
        }

        val recordsToAddToAdditionalForNewBlock = (recordsToMove - newBlock.data).filter { it.isValid() }
        recordsToAddToAdditionalForNewBlock.forEach {
            val addResult = additionalFile.add(newBlock, it)
            when (addResult) {
                is AddResult.RecordAddedToExistingBlock -> {
                    newBlock.additionalRecordCount++
                }
                is AddResult.RecordAddedToNewBlock      -> {
                    newBlock.additionalBlockCount ++
                    newBlock.additionalRecordCount++
                }
                is AddResult.FirstAdditionalBlock       -> {
                    newBlock.additionalBlockAddress = addResult.newBlockAddress
                    newBlock.additionalBlockCount ++
                    newBlock.additionalRecordCount++
                }
                is AddResult.RecordWasNotAdded          -> doNothing()
            }

        }

        val block = LinearHashFileBlock(blockSize = numberOfRecordsInBlock, ofType = instanceOfType, addressInFile = splitBlock.addressInFile)
        recordsThatStayed.forEach {
            if(block.add(it))
                actualRecordsCount++
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
                    block.additionalBlockCount ++
                }
                is AddResult.FirstAdditionalBlock       -> {
                    block.additionalBlockAddress = addResult.newBlockAddress
                    block.additionalRecordCount++
                    block.additionalBlockCount ++
                }
                is AddResult.RecordWasNotAdded          -> doNothing()
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

    internal val currentDensity get() = (((actualRecordsCount + additionalRecordsCount).toDouble()) / (actualBlockCount * numberOfRecordsInBlock + (numberOfRecordsInAdditionalBlock * additionalBlockCount)).toDouble()).roundDown2()

    private fun Int.address() : Int {
        val first = getFirstHash()
        if (first < actualSplitAddress / blockByteSize)
             return getSecondHash()

        else return getFirstHash()
    }

    internal fun allRecords() = allRecordsInFile() + additionalFile.allRecordsInFile()

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

    private fun Block<T>.getAllRecords(invalidateThem: Boolean) = (this.data + getAdditionalBlocks(true).flatten()).filter { it.isValid() }

    private fun Block<T>.getRecordFromAdditional(record: T)                   = additionalFile.getRecord(additionalBlockAddress, record)

    override fun toString() : String {
        val ds = this //cause i copoied it and it's too late
        return """
            ==============================================
            level   : ${ds.currentLevel}
            husto   : ${ds.currentDensity}
            split   : ${ds.actualSplitAddress/ds.blockByteSize}

            zaznamy : ${ds.actualRecordsCount}
            bloky   : ${ds.actualBlockCount}
            ${ds.allBlocksInFile()}
            --------
            zaznamy : ${ds.additionalRecordsCount}
            bloky   : ${ds.additionalBlockCount}
            ${ds.additionalFile.allBlocksInFile()}
           """.trimIndent()
    }
}

fun doNothing(){}
fun Double.roundDown2() = ((this* 1e2).toLong() / 1e2)

