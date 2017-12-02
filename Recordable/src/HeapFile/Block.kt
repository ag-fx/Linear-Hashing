package HeapFile

import AbstractData.*
import AbstractData.SizeConst.SizeOfInt
import record.Validity
import record.Validity.*
import record.emptyMutableList
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*

class HeapFileBlock<T : Record<T>> : Block<T> {

    override var data           : MutableList<T>
    override var addressInFile  : Int = -1
    override var additionalBlockAddress :Int = -1
    override val byteSize   : Int get() = recordCount * ofType.byteSize + SizeOfInt * 2
    override val blockSize  : Int get() = recordCount * ofType.byteSize + SizeOfInt * 2
    override var recordCount: Int// get() = data.size

    override val ofType: T

    constructor(data: MutableList<T>, addressInFile: Int, ofType: T) {
        this.data = data
        this.addressInFile = addressInFile
        this.recordCount = data.size
        this.ofType = ofType

        if(data.isEmpty()) throw IllegalArgumentException("Data can't be empty!")
    }

    constructor(numberOfRecordsInBlock: Int, instanceOfRecord: T) {
        this.data = Collections.nCopies(numberOfRecordsInBlock,instanceOfRecord.apply { invalidate() })
        this.addressInFile = -1
        this.recordCount = numberOfRecordsInBlock
        this.ofType = instanceOfRecord

        if(numberOfRecordsInBlock <= 0) throw IllegalArgumentException("Number of records in block has to be bigger than 0")
    }

    constructor(toCopy : HeapFileBlock<T>){
        this.data = toCopy.data
        this.addressInFile = toCopy.addressInFile
        this.recordCount = toCopy.recordCount
        this.ofType = toCopy.ofType
    }

    fun copy() = HeapFileBlock(this)

    override fun toByteArray() = toBytes {
        writeInt(recordCount)
        writeInt(addressInFile)
        data.forEach {
            write(it.toByteArray())
        }
    }


    override fun fromByteArray(byteArray: ByteArray): Block<T> {
        val dis = DataInputStream(ByteArrayInputStream(byteArray))
        val recordCount = dis.readInt()
        val addressInFile = dis.readInt()
        val readList = emptyMutableList<T>()

        for (i in 0 until recordCount) {
            val bytes = ByteArray(ofType.byteSize)
            for (j in 0 until ofType.byteSize)
                bytes[j] = dis.readByte()

            readList.add(ofType.fromByteArray(bytes))
        }

        return HeapFileBlock(readList, addressInFile, ofType)
    }

    override var validity: Validity = Valid

}