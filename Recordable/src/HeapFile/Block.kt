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
    override val byteSize   : Int get() = recordCount * ofType.byteSize + SizeOfInt * 3
    override val blockSize  : Int get() = recordCount * ofType.byteSize + SizeOfInt * 3
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
        this.data = Collections.nCopies(numberOfRecordsInBlock,instanceOfRecord.apply { invalidate() }).toMutableList()
        this.addressInFile = -1
        this.recordCount = numberOfRecordsInBlock
        this.ofType = instanceOfRecord

        if(numberOfRecordsInBlock <= 0) throw IllegalArgumentException("Number of records in block has to be bigger than 0")
    }

    private constructor(toCopy : HeapFileBlock<T>){
        this.data = toCopy.data
        this.addressInFile = toCopy.addressInFile
        this.recordCount = toCopy.recordCount
        this.ofType = toCopy.ofType
    }

    fun copy() = HeapFileBlock(this)

    override fun toByteArray() = toBytes {
        writeInt(recordCount)
        writeInt(addressInFile)
        writeInt(additionalBlockAddress)
        data.forEach {
            write(it.toByteArray())
        }
    }


    override fun fromByteArray(byteArray: ByteArray): Block<T> {
        val dis                         = DataInputStream(ByteArrayInputStream(byteArray))
        val recordCount                 = dis.readInt()
        val addressInFile               = dis.readInt()
        val additionalAddress           = dis.readInt()
        val readList = emptyMutableList<T>()
        val recordBytes = emptyMutableList<ByteArray>()
        for (i in 0 until recordCount) {
            val bytes = ByteArray(ofType.byteSize)
            System.arraycopy(byteArray,i* ofType.byteSize +  (SizeOfInt*3),bytes,0,ofType.byteSize)
            recordBytes.add(bytes)
        }
        recordBytes.forEach {
            readList.add(ofType.fromByteArray(it))
        }

        return HeapFileBlock(readList, addressInFile, ofType).apply {
            additionalBlockAddress          = additionalAddress
        }

    }

    override var validity: Validity = Valid

}