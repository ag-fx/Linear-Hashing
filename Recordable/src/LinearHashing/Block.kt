@file:Suppress("LeakingThis")

package LinearHashing

import AbstractData.*
import AbstractData.SizeConst.*
import record.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream

       // splitBlock.getAdditionalBlocks(invalidateThem = true)

open class LinearHashFileBlock<T: Record<T>> : Block<T> {

    override var addressInFile: Int
    override var additionalBlockAddress :Int
     var additionalBlockCount :Int
     var additionalRecordCount :Int
    override val blockSize: Int
    override var recordCount: Int
        get() = data.size
    override var data : MutableList<T>
    override val ofType : T
    override fun toString() = data.toString()


    constructor(blockSize:Int, ofType : T,addressInFile :Int = 0) {
        this.blockSize     = blockSize
        this.recordCount   = blockSize
        this.addressInFile = addressInFile
        this.data          = (0 until blockSize).map { ofType.apply { validity=Validity.Invalid } }.toMutableList()
        ofType.validity    = Validity.Invalid
        this.ofType        = ofType
        this.additionalBlockAddress = -1
        this.additionalRecordCount = 0
        this.additionalBlockCount = 0


    }

    constructor(blockSize: Int,ofType: T, data : List<T>){
        this.blockSize      = blockSize
        this.recordCount    = data.size
        this.data           = data.toMutableList()
        this.addressInFile  = 0
        this.ofType         = ofType
        this.additionalBlockAddress = -1
        this.additionalRecordCount = 0
        this.additionalBlockCount = 0

        if(data.size > blockSize) throw IllegalArgumentException("you can't initialize block with more data than block size")

    }

    private constructor(toCopy : LinearHashFileBlock<T>){
        this.blockSize     = toCopy.blockSize
        this.recordCount   = toCopy.recordCount
        this.addressInFile = toCopy.addressInFile
        this.data          = toCopy.data
        this.ofType        = toCopy.ofType
        this.additionalBlockAddress = toCopy.additionalBlockAddress
        this.additionalRecordCount = toCopy.additionalRecordCount
        this.additionalBlockCount  = toCopy.additionalBlockCount
    }

    fun copy() = LinearHashFileBlock(this)

    override fun toByteArray() = toBytes {
        writeValidity(validity)
        writeInt(addressInFile)
        writeInt(blockSize)
        writeInt(recordCount)
        writeInt(additionalBlockAddress)
        writeInt(additionalRecordCount)
        writeInt(additionalBlockCount)
        for(i in 0 until blockSize){
            val record = data.getOrNull(i)
            if(record!=null)
                write(record.toByteArray())
            else
                write(ofType.apply { validity = Validity.Invalid }.toByteArray())
        }
    }

    override fun fromByteArray(byteArray: ByteArray): Block<T> {
        val b = DataInputStream(ByteArrayInputStream(byteArray))

        val valid         = b.readValidity()
        val addressInFile = b.readInt()
        val blockSize     = b.readInt()
        val recordCount   = b.readInt()
        val recordSize    = ofType.byteSize
        val readList      = emptyMutableList<T>()
        val additionalBlockAddress = b.readInt()
        val additionalRecords      = b.readInt()
        val additionalBlocks       = b.readInt()
        for (i in 0 until recordCount) {
            val bytes = ByteArray(recordSize)
            for (j in 0 until recordSize)
                bytes[j] = b.readByte()

            readList.add(ofType.fromByteArray(bytes))
        }

        return LinearHashFileBlock(blockSize, ofType).apply {
            this.validity               = valid
            this.addressInFile          = addressInFile
            this.recordCount            = recordCount
            this.data                   = readList
            this.additionalBlockAddress = additionalBlockAddress
            this.additionalRecordCount  = additionalRecords
            this.additionalBlockCount   = additionalBlocks
        }
    }

    override val byteSize: Int
        get() = SizeOfValidity + SizeOfInt + SizeOfInt + SizeOfInt + SizeOfInt + SizeOfInt + SizeOfInt + (blockSize * ofType.byteSize)
    override var validity: Validity = Validity.Valid

}